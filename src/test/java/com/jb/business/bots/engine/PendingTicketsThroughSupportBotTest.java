package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.business.bots.login.token.JbSupportLoginTokenTypes;
import com.jb.business.bots.pending.tickets.JbSupportPendingTicketsFields;
import com.jb.business.bots.pending.tickets.JbSupportPendingTicketsSteps;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jb.entities.JbEntityPendingTickets;
import com.jn.business.messages.JnBusinessRegisterSupportPendingCommand;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.entities.decorators.ElasticsearchLocal;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Exercises the {@code /pendingTickets} command of the support bot, from the moment a command reaches the
 * operator to the moment it leaves the list:
 * <ul>
 * <li>the notice of a token unlock ticket, sent by the real message sending, opens a ticket in the inbox
 * ({@link JnEntitySupportPendingCommand});</li>
 * <li>the list shows the tickets oldest first, one at a time, and {@code 2} goes to the next one;</li>
 * <li>{@code 1} runs the command of the ticket, which leaves the list, and the list is shown again;</li>
 * <li>a command typed outside the list also takes its ticket out of it;</li>
 * <li>the session of the list is in the database, so it is not lost with the engine or with the turn of the day.</li>
 * </ul>
 *
 * <p>As in {@code UnlockTokenThroughSupportBotTest}, the database is the real Elasticsearch, and Telegram and the
 * e-mail provider are test doubles.
 */
public class PendingTicketsThroughSupportBotTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	private static final FakeEmailSender EMAIL_INBOX = new FakeEmailSender();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailSender> email = () -> EMAIL_INBOX;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpElasticSearchQueryExecutor(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	private static final String USER_EMAIL = "onias85@gmail.com";

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final String UNLOCK_TICKET = "/solveLoginTokenTicket unlockToken " + USER_EMAIL;

	/** A request that does not exist: running it ends right away, telling the operator there is nothing to review. */
	private static final String SKILL_TICKET = "/fixSkillHierarchy JVM add pending.tickets@jobsnow.com";

	@Before
	public void setUpScenario() {

		// a ticket left open by a previous test would only be updated by the save below, and only an insert
		// notifies the operator: solving it through the bot closes it, whatever state it is in
		this.type(UNLOCK_TICKET);

		this.deleteTheTicketsAndTheSessionOfTheSupportChat();
		this.resaveMessageTemplates();

		CcpJsonRepresentation user = this.user();

		// saving to the twin deletes the record from the main entity: that is how the token gets locked
		JnEntityLoginToken.ENTITY.getTwinEntity().save(user);

		this.deleteAlreadySentPendingTicketNotice();
		JnEntityLoginTokenRequestUnlock.ENTITY.save(user);

		TELEGRAM.sentMessages.clear();
		EMAIL_INBOX.sentEmails.clear();
	}

	@Test
	public void theCommandSentToTheOperatorOpensATicket() {
		CcpJsonRepresentation unlockTicket = this.pendingCommand(UNLOCK_TICKET);
		assertTrue(JnEntitySupportPendingCommand.ENTITY.exists(unlockTicket));
	}

	@Test
	public void theListShowsOneTicketAtATime() {

		this.openSkillTicket();

		this.type("/pendingTickets");
		assertEquals("Você tem 2 tickets para resolver\n" + this.prompt(1, 2, UNLOCK_TICKET), this.lastMessage());

		this.type("2");
		assertEquals(this.prompt(2, 2, SKILL_TICKET), this.lastMessage());

		// after the last one, the first one again
		this.type("2");
		assertEquals(this.prompt(1, 2, UNLOCK_TICKET), this.lastMessage());

		this.type("talvez");
		assertEquals("Não entendi a resposta.\n\n" + this.prompt(1, 2, UNLOCK_TICKET), this.lastMessage());

		// the tickets left the inbox for the list of the operator
		assertFalse(JnEntitySupportPendingCommand.ENTITY.exists(this.pendingCommand(UNLOCK_TICKET)));
		assertTrue(JbEntityPendingTickets.ENTITY.exists(this.pendingTicket(UNLOCK_TICKET)));
	}

	@Test
	public void choosingTheTicketSolvesItAndShowsTheListAgain() {

		this.openSkillTicket();

		this.type("/pendingTickets");
		this.type("1");

		assertTrue(this.messagesToTheSupport().contains("Resolvendo o ticket " + UNLOCK_TICKET));

		// the command of the ticket really ran: the ticket of the user went to the twin
		assertFalse(JnEntityLoginTokenRequestUnlock.ENTITY.exists(this.user()));
		assertFalse(JbEntityPendingTickets.ENTITY.exists(this.pendingTicket(UNLOCK_TICKET)));

		assertEquals("Você tem 1 ticket para resolver\n" + this.prompt(1, 1, SKILL_TICKET), this.lastMessage());

		// the skill ticket has no request behind it: running it ends the command, and so the ticket, and the list is empty
		this.type("1");
		assertFalse(JbEntityPendingTickets.ENTITY.exists(this.pendingTicket(SKILL_TICKET)));
		assertEquals("Você não tem tickets para resolver.", this.lastMessage());
		assertFalse(JbEntityBotCommandStepSession.ENTITY.exists(this.session()));
	}

	@Test
	public void aCommandTypedOutsideTheListAlsoSolvesItsTicket() {

		this.openSkillTicket();

		// with two spaces, as an operator may type it
		this.type("/solveLoginTokenTicket  unlockToken " + USER_EMAIL);

		assertFalse(JnEntityLoginTokenRequestUnlock.ENTITY.exists(this.user()));
		assertFalse(JnEntitySupportPendingCommand.ENTITY.exists(this.pendingCommand(UNLOCK_TICKET)));

		this.type("/pendingTickets");
		assertEquals("Você tem 1 ticket para resolver\n" + this.prompt(1, 1, SKILL_TICKET), this.lastMessage());
	}

	@Test
	public void aTicketSolvedWhileTheListWasOpenIsNotRunAgain() {

		this.openSkillTicket();

		this.type("/pendingTickets");

		// solved by another way while the list was open on it (the session of the list is kept)
		JbEntityPendingTickets.ENTITY.delete(this.pendingTicket(UNLOCK_TICKET));

		this.type("1");
		assertEquals("O ticket " + UNLOCK_TICKET + " já foi resolvido.\n\n" + this.prompt(1, 1, SKILL_TICKET), this.lastMessage());
		// the ticket was not run: the ticket of the user is still open
		assertTrue(JnEntityLoginTokenRequestUnlock.ENTITY.exists(this.user()));
	}

	@Test
	public void theSessionOfTheListIsInTheDatabase() {

		this.type("/pendingTickets");

		CcpJsonRepresentation savedSession = JbEntityBotCommandStepSession.ENTITY.getOneById(this.session());
		String stepName = savedSession.getAsString(JnJsonInstantMessengerFields.stepName);
		CcpJsonRepresentation sessionJson = savedSession.getInnerJson(JnJsonCommonsFields.json);
		String currentTicket = sessionJson.getAsString(JbSupportPendingTicketsFields.currentTicket);

		assertEquals(JbSupportPendingTicketsSteps.pendingTicketsChoose.name(), stepName);
		assertEquals(UNLOCK_TICKET, currentTicket);
	}

	@Test
	public void withoutTicketsTheOperatorIsTold() {

		this.type(UNLOCK_TICKET);
		this.type("/pendingTickets");

		assertEquals("Você não tem tickets para resolver.", this.lastMessage());
		assertFalse(JbEntityBotCommandStepSession.ENTITY.exists(this.session()));
	}

	/**
	 * The list is read by a query, which sees a write only after Elasticsearch refreshes the index (up to a second).
	 * The operator takes longer than that between receiving a command and asking for the list; the test does not,
	 * so it refreshes the indices before each message.
	 */
	private void type(String text) {
		ElasticsearchLocal.refresh("jn_support_pending_command", "jb_pending_tickets");
		CcpJsonRepresentation messageWithBotName = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.botName, JbBotType.support);
		CcpJsonRepresentation messageWithChatId = messageWithBotName.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		CcpJsonRepresentation messageWithMessageId = messageWithChatId.put(JbBotEngine.Fields.message_id, 363);
		CcpJsonRepresentation message = messageWithMessageId.put(JnJsonInstantMessengerFields.message, text);
		JbBotType.support.getBot().execute(message);
	}

	private String prompt(int ticketNumber, int ticketsCount, String ticket) {
		String prompt = "Ticket " + ticketNumber + " de " + ticketsCount + " => " + ticket + "\n"
				+ "Digite \"1\" para resolver ou \"2\" para ir ao próximo";
		return prompt;
	}

	private String lastMessage() {
		CcpJsonRepresentation lastMessage = TELEGRAM.lastMessageFor(SUPPORT_CHAT);
		String text = lastMessage.getAsString(JnJsonCommonsFields.message);
		return text;
	}

	private List<String> messagesToTheSupport() {
		Stream<CcpJsonRepresentation> sentMessagesStream = TELEGRAM.sentMessages.stream();
		Stream<String> textsStream = sentMessagesStream.map(message -> message.getAsString(JnJsonCommonsFields.message));
		List<String> messages = textsStream.collect(Collectors.toList());
		return messages;
	}

	/**
	 * A second ticket, recorded the way the message sending records any command delivered to the support bot.
	 */
	private void openSkillTicket() {
		CcpJsonRepresentation sentMessageWithBotName = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.botName, JbBotType.support);
		CcpJsonRepresentation sentMessageWithChatId = sentMessageWithBotName.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT.doubleValue());
		CcpJsonRepresentation sentMessage = sentMessageWithChatId.put(JnJsonCommonsFields.message, SKILL_TICKET + " ");
		// a later timestamp than the unlock ticket, so the order of the list is known
		this.waitAMillisecond();
		JnBusinessRegisterSupportPendingCommand.INSTANCE.execute(sentMessage);
	}

	private void waitAMillisecond() {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() == start) {
			Thread.onSpinWait();
		}
	}

	private CcpJsonRepresentation pendingCommand(String command) {
		CcpJsonRepresentation pendingCommandWithBotName = CcpOtherConstants.EMPTY_JSON.put(JnEntitySupportPendingCommand.Fields.botName, JbBotType.support);
		CcpJsonRepresentation pendingCommandWithChatId = pendingCommandWithBotName.put(JnEntitySupportPendingCommand.Fields.chatId, SUPPORT_CHAT);
		CcpJsonRepresentation pendingCommand = pendingCommandWithChatId.put(JnEntitySupportPendingCommand.Fields.command, command);
		return pendingCommand;
	}

	private CcpJsonRepresentation pendingTicket(String ticket) {
		CcpJsonRepresentation pendingTicketWithBotName = CcpOtherConstants.EMPTY_JSON.put(JbEntityPendingTickets.Fields.botName, JbBotType.support);
		CcpJsonRepresentation pendingTicketWithChatId = pendingTicketWithBotName.put(JbEntityPendingTickets.Fields.chatId, SUPPORT_CHAT);
		CcpJsonRepresentation pendingTicket = pendingTicketWithChatId.put(JbEntityPendingTickets.Fields.ticket, ticket);
		return pendingTicket;
	}

	private CcpJsonRepresentation session() {
		CcpJsonRepresentation sessionWithBotName = CcpOtherConstants.EMPTY_JSON.put(JbEntityBotCommandStepSession.Fields.botName, JbBotType.support);
		CcpJsonRepresentation session = sessionWithBotName.put(JbEntityBotCommandStepSession.Fields.chatId, SUPPORT_CHAT);
		return session;
	}

	private CcpJsonRepresentation user() {
		CcpJsonRepresentation user = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, USER_EMAIL)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		return user;
	}

	/**
	 * Other tests send commands to the same support chat, so every ticket of the chat is discarded, as well as a
	 * session left by a previous run. Each record is deleted by its id: a delete by query conflicts with a record
	 * rewritten a moment ago (the previous test), which the query still sees in its older version. The tickets of
	 * this test are deleted by their keys as well, since the query may not see them yet.
	 */
	private void deleteTheTicketsAndTheSessionOfTheSupportChat() {
		ElasticsearchLocal.refresh("jn_support_pending_command", "jb_pending_tickets");
		var query = CcpQueryOptions.INSTANCE
				.startQuery();
		var bool = query
				.startBool();
		var mustWithTheChatId = bool
				.startMust()
				.term(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		var boolWithTheChatId = mustWithTheChatId
				.endMustAndBackToBool();
		var queryWithTheChatId = boolWithTheChatId
				.endBoolAndBackToQuery();
		CcpQueryOptions requestWithTheChatId = queryWithTheChatId
				.endQueryAndBackToRequest()
				.maxResults();

		List<CcpJsonRepresentation> inbox = requestWithTheChatId.selectFrom(JnEntitySupportPendingCommand.ENTITY).getResultAsList();
		for (CcpJsonRepresentation pendingCommand : inbox) {
			String command = pendingCommand.getAsString(JnEntitySupportPendingCommand.Fields.command);
			JnEntitySupportPendingCommand.ENTITY.delete(this.pendingCommand(command));
		}

		List<CcpJsonRepresentation> tickets = requestWithTheChatId.selectFrom(JbEntityPendingTickets.ENTITY).getResultAsList();
		for (CcpJsonRepresentation ticket : tickets) {
			String command = ticket.getAsString(JbEntityPendingTickets.Fields.ticket);
			JbEntityPendingTickets.ENTITY.delete(this.pendingTicket(command));
		}

		for (String ticket : new String[] {UNLOCK_TICKET, SKILL_TICKET}) {
			JnEntitySupportPendingCommand.ENTITY.delete(this.pendingCommand(ticket));
			JbEntityPendingTickets.ENTITY.delete(this.pendingTicket(ticket));
		}

		JbEntityBotCommandStepSession.ENTITY.delete(this.session());
	}

	private void resaveMessageTemplates() {
		JnEntityInstantMessengerTemplateMessage configurator = new JnEntityInstantMessengerTemplateMessage();
		List<CcpBulkItem> templates = configurator.getFirstRecordsToInsert();
		for (CcpBulkItem template : templates) {
			template.entity.save(template.json);
		}
	}

	/**
	 * The notice of the pending ticket has a fixed text; without deleting the record of the previous sending,
	 * opening the ticket would be refused as a repetition. See {@code UnlockTokenThroughSupportBotTest}.
	 */
	private void deleteAlreadySentPendingTicketNotice() {

		String noticeText = "/" + JbSupportBotCommands.solveLoginTokenTicket + " "
				+ JbSupportLoginTokenTypes.unlockToken + " " + USER_EMAIL + " ";

		double chatIdAsTheDatabaseReturnsIt = SUPPORT_CHAT.doubleValue();

		CcpJsonRepresentation notice = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, chatIdAsTheDatabaseReturnsIt)
				.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text)
				.put(JnJsonCommonsFields.message, noticeText);

		JnEntityInstantMessengerMessageSent.ENTITY.delete(notice);
	}
}
