package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
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
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * Proves the commands every bot has ({@link JbDefaultBotCommandStep}) through the support bot, the same way the
 * Telegram reader delivers the messages; Telegram is replaced by a test double that keeps what would be sent.
 */
public class DefaultBotCommandsTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	private static final FakeEmailSender EMAIL_INBOX = new FakeEmailSender();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailSender> email = () -> EMAIL_INBOX;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	private static final Long SUPPORT_CHAT = 751717896L;

	@Before
	public void clearTheMessages() {
		TELEGRAM.sentMessages.clear();
	}

	private String answerTo(String typedText) {
		CcpJsonRepresentation message = new CcpJsonRepresentation("{"
				+ "\"botName\": \"support\","
				+ "\"chatId\": " + SUPPORT_CHAT + ","
				+ "\"message\": \"" + typedText + "\","
				+ "\"message_id\": 363,"
				+ "\"sentAt\": 1790121741,"
				+ "\"updateId\": 840339225,"
				+ "\"userName\": \"OniasJr\""
				+ "}");
		JbBotType.support.getBot().execute(message);
		String answer = TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message);
		return answer;
	}

	@Test
	public void chatIdAnswersTheIdOfTheChat() {
		assertEquals("" + SUPPORT_CHAT, this.answerTo("/chatId"));
	}

	@Test
	public void showAllCommandsListsTheVisibleCommandsByTheirNameInTheLanguage() {
		String answer = this.answerTo("/showAllCommands");

		assertTrue(answer, answer.contains("/solucionarTicketsDeTokenDeLogin"));
		assertTrue(answer, answer.contains(", /pendingTickets"));
		assertTrue(answer, answer.contains("/chatId"));
		assertTrue("one space between the commands: " + answer, false == answer.contains(",  "));
		assertTrue(answer, false == answer.contains("/removeSession"));
	}

	/** The notices to the operators carry the canonical name; /showAllCommands lists the name of the language: both work. */
	@Test
	public void aCommandIsRecognizedByItsCanonicalNameAndByItsNameInTheLanguage() {
		BotCommand command = JbBotEngine.INSTANCE.allCommands.get("solveLoginTokenTicket");
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.language, JnLanguage.portuguese.name());

		boolean canonicalDoesNotMatch = command.commandNameDoesNotMatch(session.put(JnJsonCommonsFields.typedValue, "/solveLoginTokenTicket a@b.com"));
		boolean translatedDoesNotMatch = command.commandNameDoesNotMatch(session.put(JnJsonCommonsFields.typedValue, "/solucionarTicketsDeTokenDeLogin a@b.com"));
		boolean otherDoesNotMatch = command.commandNameDoesNotMatch(session.put(JnJsonCommonsFields.typedValue, "/pendingTickets"));

		assertTrue(false == canonicalDoesNotMatch);
		assertTrue(false == translatedDoesNotMatch);
		assertTrue(otherDoesNotMatch);
	}

	@Test
	public void explainThisBotAnswersTheExplanationOfTheBot() {
		String answer = this.answerTo("/explainThisBot");

		assertTrue(answer, answer.startsWith("Bot de rotinas administrativas"));
	}

	/** The session of the support chat, as the engine saves it in the middle of a command. */
	private CcpJsonRepresentation sessionInTheMiddleOf(String commandName) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support.name())
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT)
				.put(JnJsonInstantMessengerFields.commandName, commandName)
				.put(JnJsonInstantMessengerFields.stepName, commandName)
				.put(JnJsonCommonsFields.json, CcpOtherConstants.EMPTY_JSON.content)
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese.name());
	}

	@Test
	public void withoutACommandInProgressExplainThisCommandShowsTheCommands() {
		JbEntityBotCommandStepSession.ENTITY.delete(this.sessionInTheMiddleOf("solveLoginTokenTicket"));

		String answer = this.answerTo("/explainThisCommand");

		assertTrue("nothing to explain, so the bot shows all commands: " + answer, answer.startsWith("/solucionarTicketsDeTokenDeLogin"));
	}

	/** Until 2026-10-06 it could never run: it was invisible without a command in the message, and then explained itself. */
	@Test
	public void explainThisCommandExplainsTheCommandInProgress() {
		CcpJsonRepresentation session = this.sessionInTheMiddleOf("solveLoginTokenTicket");
		JbEntityBotCommandStepSession.ENTITY.save(session);
		try {
			String answer = this.answerTo("/explainThisCommand");

			assertTrue(answer, answer.startsWith("Quando o usu"));
		} finally {
			JbEntityBotCommandStepSession.ENTITY.delete(session);
		}
	}

	private String explanationOfTheTokenTicketCommand() {
		BotCommand command = JbBotEngine.INSTANCE.allCommands.get("solveLoginTokenTicket");
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.language, JnLanguage.portuguese);
		String explanation = command.getExplanation(session);
		return explanation;
	}

	@Test
	public void theExplanationOfACommandIsItsText() {
		assertTrue(this.explanationOfTheTokenTicketCommand().startsWith("Quando o usu"));
	}
}
