package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.ElasticsearchLocal;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatus;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jb.entities.JbEntityPendingTickets;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.entities.JnEntityTicketDailyLimit;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.json.fields.validation.JnTicketQuotaGroups;
import com.jn.json.fields.validation.JnUserRequestCommands;
import com.js.entities.JsEntityContactUs;
import com.js.json.fields.validation.JsContactUsTypes;
import com.js.messages.JsNotifySupportAndUserAboutContactUs;
import com.js.messages.JsNotifyUserAboutAnsweredContactUs;
import com.js.services.JsServiceContactUs;
import com.js.status.contact.us.JsProcessStatusSaveContactUs;

/**
 * The "contact us" from the opening of the ticket by the service to its handling by the support operator through
 * {@code /answerContactUs <type> <email>}: the answer, the skip to the next ticket and the ignoring of the user, plus the
 * daily quotas of the ticket kinds.
 */
public class ContactUsThroughSupportBotTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	private static final FakeEmailSender EMAIL_INBOX = new FakeEmailSender();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailSender> email = () -> EMAIL_INBOX;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
	}

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final String ANSWERED_USER = "faleconosco.respondido@teste.com";

	private static final String SKIPPED_USER = "faleconosco.pulado@teste.com";

	private static final String IGNORED_USER = "faleconosco.ignorado@teste.com";

	private static final String QUOTA_USER = "faleconosco.cota@teste.com";

	/**
	 * Saves the bot, its commands, their steps, their end messages and the message templates of the contact us again
	 * so that the index follows what is declared in java (the create operation of the initial records does not
	 * overwrite what is already there).
	 */
	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(JbEntityBot.INSTANCE, JnEntityBotCommand.INSTANCE, JnEntityBotCommandStep.INSTANCE, JnEntityBotCommandStepEndMessage.INSTANCE, JsEntityContactUs.INSTANCE);
		for (CcpEntityConfigurator configurator : configurators) {
			List<CcpBulkItem> records = configurator.getFirstRecordsToInsert();
			for (CcpBulkItem record : records) {
				// the subscribers of a write event are appended by a script, not written as a record
				boolean scripted = record.operation != CcpBulkEntityOperationType.create;
				if(scripted) {
					continue;
				}
				record.entity.save(record.json);
			}
		}
	}

	@Before
	public void clearTheBotSession() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		JbEntityBotCommandStepSession.ENTITY.delete(session);
		TELEGRAM.sentMessages.clear();
		EMAIL_INBOX.sentEmails.clear();
	}

	/**
	 * Leaves no ticket of the test users behind: neither in the contact us (and its group) nor in the inbox of
	 * {@code /pendingTickets} of the real support chat.
	 */
	@After
	public void discardTheTicketsOfTheTest() {
		for (String email : Arrays.asList(ANSWERED_USER, SKIPPED_USER, IGNORED_USER, QUOTA_USER)) {
			for (JsContactUsTypes type : JsContactUsTypes.values()) {
				this.discardTicket(email, type);
				CcpJsonRepresentation pendingCommand = this.pendingCommand("/answerContactUs " + type + " " + email);
				JnEntitySupportPendingCommand.ENTITY.delete(pendingCommand);
			}
		}
	}

	/**
	 * A bug goes to the operator as {@code /answerContactUs reportBug <email>}, with priority 0, and the user gets the
	 * e-mail of the opening; the operator answers it, the ticket moves to {@code js_contact_us_solved} with the answer
	 * and the user gets it by e-mail.
	 */
	@Test
	public void operatorAnswersTheTicket() {

		this.openTicket(ANSWERED_USER, JsContactUsTypes.reportBug, "Erro ao salvar", "A tela de currículo trava ao clicar em salvar");

		String ticket = "/answerContactUs reportBug " + ANSWERED_USER;
		String notice = TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message);
		assertEquals(ticket, notice);
		assertTrue(this.isPendingTicket(ticket));
		CcpJsonRepresentation pendingCommand = JnEntitySupportPendingCommand.ENTITY.getOneById(this.pendingCommand(ticket));
		assertEquals(0, pendingCommand.getAsIntegerNumber(JnEntitySupportPendingCommand.Fields.priority).intValue());
		this.emailSentTo(ANSWERED_USER, JsNotifySupportAndUserAboutContactUs.class);

		String request = this.operatorTypes(ticket);
		assertTrue(request, request.contains("Fale conosco de " + ANSWERED_USER));
		assertTrue(request, request.contains("Título: Erro ao salvar"));
		assertTrue(request, request.contains("A tela de currículo trava ao clicar em salvar"));
		assertTrue(request, request.contains("responder <resposta>"));

		String tooShort = this.operatorTypes("responder ok");
		assertTrue(tooShort, tooShort.startsWith("Não entendi a resposta"));

		String answered = this.operatorTypes("responder o defeito foi corrigido, tente novamente");
		assertTrue(answered, answered.startsWith("O ticket"));
		assertTrue(answered, answered.contains("de " + ANSWERED_USER + " foi respondido"));
		assertFalse(this.isPendingTicket(ticket));

		CcpJsonRepresentation ticketKey = this.ticketKey(ANSWERED_USER, JsContactUsTypes.reportBug);
		assertFalse(JsEntityContactUs.ENTITY.exists(ticketKey));
		assertTrue(JsEntityContactUs.ENTITY.getTwinEntity().exists(ticketKey));
		CcpJsonRepresentation solved = JsEntityContactUs.ENTITY.getTwinEntity().getOneById(ticketKey);
		assertEquals("o defeito foi corrigido, tente novamente", solved.getAsString(JsEntityContactUs.Fields.supportResponse));
		this.emailSentTo(ANSWERED_USER, JsNotifyUserAboutAnsweredContactUs.class);
	}

	/**
	 * "Next" leaves the ticket open, in {@code js_contact_us} and in {@code /pendingTickets}, and sends no e-mail.
	 */
	@Test
	public void operatorSkipsTheTicket() {

		this.openTicket(SKIPPED_USER, JsContactUsTypes.makeSuggestion, "Busca por cidade", "Seria bom filtrar as vagas por cidade");
		String ticket = "/answerContactUs makeSuggestion " + SKIPPED_USER;
		EMAIL_INBOX.sentEmails.clear();

		this.operatorTypes(ticket);
		this.operatorTypes("próximo");

		assertTrue(this.isPendingTicket(ticket));
		assertTrue(JsEntityContactUs.ENTITY.exists(this.ticketKey(SKIPPED_USER, JsContactUsTypes.makeSuggestion)));
		assertTrue(EMAIL_INBOX.sentEmails.isEmpty());
	}

	/**
	 * Ignoring the user (after confirming) discards their open tickets without e-mailing them, and their next ticket is
	 * refused with 403; allowing the user again lets the tickets in.
	 */
	@Test
	public void operatorIgnoresTheUser() {

		this.allowUser(IGNORED_USER);
		this.openTicket(IGNORED_USER, JsContactUsTypes.reportBug, "Erro qualquer", "Mensagem sem sentido para testar o ignorar");
		this.openTicket(IGNORED_USER, JsContactUsTypes.clarifyDoubts, "Dúvida qualquer", "Outra mensagem sem sentido do mesmo usuário");
		String ticket = "/answerContactUs reportBug " + IGNORED_USER;
		String otherTicket = "/answerContactUs clarifyDoubts " + IGNORED_USER;

		try {
			// the open tickets of the user are found by a query, which sees a write only after a refresh
			ElasticsearchLocal.refresh("js_contact_us");
			EMAIL_INBOX.sentEmails.clear();

			this.operatorTypes(ticket);
			String confirmation = this.operatorTypes("ignorar");
			assertTrue(confirmation, confirmation.startsWith("Confirma que o usuário " + IGNORED_USER + " será ignorado pelo suporte"));

			String canceled = this.operatorTypes("não");
			assertTrue(canceled, canceled.startsWith("O usuário não será ignorado."));

			this.operatorTypes("ignorar");
			String ignored = this.operatorTypes("sim");
			assertTrue(ignored, ignored.startsWith("O usuário " + IGNORED_USER + " foi ignorado"));

			for (JsContactUsTypes type : Arrays.asList(JsContactUsTypes.reportBug, JsContactUsTypes.clarifyDoubts)) {
				CcpJsonRepresentation ticketKey = this.ticketKey(IGNORED_USER, type);
				assertFalse(type.name(), JsEntityContactUs.ENTITY.exists(ticketKey));
				assertFalse(type.name(), JsEntityContactUs.ENTITY.getTwinEntity().exists(ticketKey));
			}
			assertFalse(this.isPendingTicket(ticket));
			assertFalse(this.isPendingTicket(otherTicket));
			assertTrue(EMAIL_INBOX.sentEmails.isEmpty());

			CcpJsonRepresentation newTicket = this.newTicket(IGNORED_USER, JsContactUsTypes.reportBug, "Erro de novo", "Tentando de novo depois de ignorado");
			CcpProcessStatus refusal = this.statusOf(() -> JsServiceContactUs.SaveContactUs.execute(newTicket));
			assertEquals(JsProcessStatusSaveContactUs.userNotAllowed, refusal);
		} finally {
			this.allowUser(IGNORED_USER);
		}

		CcpJsonRepresentation newTicket = this.newTicket(IGNORED_USER, JsContactUsTypes.reportBug, "Erro de novo", "Tentando de novo depois de liberado");
		JsServiceContactUs.SaveContactUs.execute(newTicket);
		assertTrue(JsEntityContactUs.ENTITY.exists(this.ticketKey(IGNORED_USER, JsContactUsTypes.reportBug)));
		this.discardTicket(IGNORED_USER, JsContactUsTypes.reportBug);
	}

	/**
	 * The kinds other than the bug and the question share one ticket a day (with the skill suggestion and the skill
	 * hierarchy fix); the question has its own quota; the bug has none.
	 */
	@Test
	public void dailyQuotasOfTheTicketKinds() {

		this.openTicket(QUOTA_USER, JsContactUsTypes.makeCompliment, "Parabéns", "Gostei muito do sistema de vocês");
		assertTrue(JnEntityTicketDailyLimit.ENTITY.exists(JnEntityTicketDailyLimit.getKey(QUOTA_USER, JnTicketQuotaGroups.sharedTickets)));

		this.discardTicket(QUOTA_USER, JsContactUsTypes.makeCriticism);
		CcpJsonRepresentation criticism = this.newTicket(QUOTA_USER, JsContactUsTypes.makeCriticism, "Crítica", "A tela de vagas está lenta demais");
		CcpProcessStatus sharedRefusal = this.statusOf(() -> JsServiceContactUs.SaveContactUs.execute(criticism));
		assertEquals(JsProcessStatusSaveContactUs.dailyLimitReached, sharedRefusal);
		assertFalse(JsEntityContactUs.ENTITY.exists(this.ticketKey(QUOTA_USER, JsContactUsTypes.makeCriticism)));

		// the question has its own quota: the shared one already spent does not refuse it
		this.openTicket(QUOTA_USER, JsContactUsTypes.clarifyDoubts, "Dúvida", "Como faço para mudar o meu e-mail?");
		assertTrue(JnEntityTicketDailyLimit.ENTITY.exists(JnEntityTicketDailyLimit.getKey(QUOTA_USER, JnTicketQuotaGroups.clarifyDoubts)));
		this.discardTicket(QUOTA_USER, JsContactUsTypes.clarifyDoubts);
		CcpJsonRepresentation secondQuestion = this.newTicket(QUOTA_USER, JsContactUsTypes.clarifyDoubts, "Outra dúvida", "Como faço para apagar a minha conta?");
		CcpProcessStatus questionRefusal = this.statusOf(() -> JsServiceContactUs.SaveContactUs.execute(secondQuestion));
		assertEquals(JsProcessStatusSaveContactUs.dailyLimitReached, questionRefusal);

		// the bug spends no quota, so it is accepted once and again
		this.openTicket(QUOTA_USER, JsContactUsTypes.reportBug, "Erro um", "Primeiro erro encontrado hoje");
		CcpJsonRepresentation secondBug = this.newTicket(QUOTA_USER, JsContactUsTypes.reportBug, "Erro dois", "Segundo erro encontrado hoje");
		JsServiceContactUs.SaveContactUs.execute(secondBug);
		CcpJsonRepresentation bug = JsEntityContactUs.ENTITY.getOneById(this.ticketKey(QUOTA_USER, JsContactUsTypes.reportBug));
		assertEquals("Erro dois", bug.getAsString(JsEntityContactUs.Fields.title));
		assertFalse(JnEntityTicketDailyLimit.ENTITY.exists(JnEntityTicketDailyLimit.getKey(QUOTA_USER, JnTicketQuotaGroups.unlimited)));

		this.discardTicket(QUOTA_USER, JsContactUsTypes.makeCompliment);
		this.discardTicket(QUOTA_USER, JsContactUsTypes.clarifyDoubts);
		this.discardTicket(QUOTA_USER, JsContactUsTypes.reportBug);
	}

	/**
	 * Leaves only the new ticket: removes whatever a previous run left (the ticket, solved or not, its quota of the day
	 * and the "already sent" records of the notices), then saves it through the service.
	 */
	private void openTicket(String email, JsContactUsTypes type, String title, String description) {
		this.discardTicket(email, type);
		CcpJsonRepresentation dailyLimit = JnEntityTicketDailyLimit.getKey(email, type.quotaGroup);
		JnEntityTicketDailyLimit.ENTITY.delete(dailyLimit);
		CcpJsonRepresentation ticket = this.newTicket(email, type, title, description);
		JsServiceContactUs.SaveContactUs.execute(ticket);
	}

	private void discardTicket(String email, JsContactUsTypes type) {
		CcpJsonRepresentation ticketKey = this.ticketKey(email, type);
		JsEntityContactUs.ENTITY.deleteAnyWhere(ticketKey);
	}

	private CcpJsonRepresentation ticketKey(String email, JsContactUsTypes type) {
		CcpJsonRepresentation ticketKey = CcpOtherConstants.EMPTY_JSON
				.put(JsEntityContactUs.Fields.email, email)
				.put(JsEntityContactUs.Fields.type, type);
		return ticketKey;
	}

	private CcpJsonRepresentation newTicket(String email, JsContactUsTypes type, String title, String description) {
		CcpJsonRepresentation ticketKey = this.ticketKey(email, type);
		CcpJsonRepresentation newTicket = ticketKey
				.put(JsEntityContactUs.Fields.title, title)
				.put(JsEntityContactUs.Fields.description, description)
				.put(JsEntityContactUs.Fields.stackTrace, "TypeError: undefined is not a function\n    at onClick (resume.tsx:10:5)");
		return newTicket;
	}

	private void allowUser(String email) {
		CcpJsonRepresentation ignoredUser = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityCommandNotAllowedToUser.Fields.email, email)
				.put(JnEntityCommandNotAllowedToUser.Fields.commandName, JnUserRequestCommands.answerContactUs);
		JnEntityCommandNotAllowedToUser.ENTITY.delete(ignoredUser);
	}

	private CcpJsonRepresentation pendingCommand(String command) {
		CcpJsonRepresentation pendingCommand = CcpOtherConstants.EMPTY_JSON
				.put(JnEntitySupportPendingCommand.Fields.botName, JbBotType.support)
				.put(JnEntitySupportPendingCommand.Fields.chatId, SUPPORT_CHAT)
				.put(JnEntitySupportPendingCommand.Fields.command, command);
		return pendingCommand;
	}

	/**
	 * Tells whether the command is still a ticket of the operator: in the jn inbox, where the sending of the notice
	 * records it, or in the jb list, where {@code /pendingTickets} moves it.
	 */
	private boolean isPendingTicket(String command) {
		CcpJsonRepresentation pendingCommand = this.pendingCommand(command);
		CcpJsonRepresentation pendingTicket = CcpOtherConstants.EMPTY_JSON
				.put(JbEntityPendingTickets.Fields.botName, JbBotType.support)
				.put(JbEntityPendingTickets.Fields.chatId, SUPPORT_CHAT)
				.put(JbEntityPendingTickets.Fields.ticket, command);
		boolean inTheInbox = JnEntitySupportPendingCommand.ENTITY.exists(pendingCommand);
		boolean inTheList = JbEntityPendingTickets.ENTITY.exists(pendingTicket);
		boolean pending = inTheInbox || inTheList;
		return pending;
	}

	private CcpProcessStatus statusOf(Runnable call) {
		try {
			call.run();
		} catch (CcpErrorFlowDisturb refusal) {
			return refusal.status;
		}
		throw new AssertionError("The call was accepted");
	}

	private String operatorTypes(String text) {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT)
				.put(JnJsonCommonsFields.message, text)
				.put(JbBotEngine.Fields.message_id, 1);
		JbBotType.support.getBot().execute(message);
		CcpJsonRepresentation reply = TELEGRAM.lastMessageFor(SUPPORT_CHAT);
		String replyText = reply.getAsString(JnJsonCommonsFields.message);
		return replyText;
	}

	private CcpJsonRepresentation emailSentTo(String recipient, Class<?> template) {
		String templateId = template.getName();

		for (CcpJsonRepresentation sentEmail : EMAIL_INBOX.sentEmails) {
			String sentTemplateId = sentEmail.getAsString(JnJsonCommonsFields.templateId);
			List<String> recipients = sentEmail.getAsStringList(JnJsonCommonsFields.email);
			boolean isThisEmail = sentTemplateId.equals(templateId) && recipients.contains(recipient);

			if(isThisEmail) {
				return sentEmail;
			}
		}
		throw new AssertionError("No e-mail " + templateId + " was sent to " + recipient + ": " + EMAIL_INBOX.sentEmails);
	}
}
