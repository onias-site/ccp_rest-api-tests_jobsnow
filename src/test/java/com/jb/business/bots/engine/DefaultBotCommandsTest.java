package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
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
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
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
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotChatLanguage;
import com.jb.entities.JbEntityBotCommandExplanation;
import com.jb.entities.JbEntityBotCommandName;
import com.jb.entities.JbEntityBotExplanation;
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

	/**
	 * The names of the commands in the languages (/sair) and the texts of the default commands are seeds: they are
	 * saved again so that the index follows what is declared in java.
	 */
	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(new JbEntityBot(), new JbEntityBotCommandName(), new JbEntityBotExplanation(), new JbEntityBotCommandExplanation());
		for (CcpEntityConfigurator configurator : configurators) {
			List<CcpBulkItem> records = configurator.getFirstRecordsToInsert();
			for (CcpBulkItem record : records) {
				record.entity.save(record.json);
			}
		}
	}

	/** The chat talks in the language of the system (Portuguese) unless a test chooses another one. */
	@Before
	public void clearTheMessagesAndTheLanguageOfTheChat() {
		TELEGRAM.sentMessages.clear();
		this.clearTheLanguageOfTheChat();
	}

	@After
	public void clearTheLanguageOfTheChat() {
		CcpJsonRepresentation chatLanguageKey = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support.name())
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		JbEntityBotChatLanguage.ENTITY.delete(chatLanguageKey);
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
		assertTrue(answer, answer.contains(", /ticketsPendentes"));
		assertTrue(answer, answer.contains("/idDoChat"));
		assertTrue("one space between the commands: " + answer, false == answer.contains(",  "));
		assertTrue(answer, false == answer.contains("/removeSession"));
		assertTrue("nothing to leave, so /sair is not listed: " + answer, false == answer.contains("/sair"));
	}

	/** Until 2026-10-10 only /solucionarTicketsDeTokenDeLogin was listed in Portuguese: every command is now. */
	@Test
	public void inPortugueseNoCommandIsListedByItsEnglishName() {
		String answer = this.answerTo("/showAllCommands");

		assertEquals("/solucionarTicketsDeTokenDeLogin, /ajustarHierarquiaDeHabilidades, /permitirComandoAoUsuario, /ticketsPendentes, "
				+ "/avaliarSugestaoDeHabilidade, /idDoChat, /mostrarTodosOsComandos, /explicarEsteBot, /definirIdioma", answer);
	}

	/** The name in Portuguese runs the command, and the canonical one keeps working. */
	@Test
	public void theDefaultCommandsRunByTheirNameInPortuguese() {
		assertEquals("" + SUPPORT_CHAT, this.answerTo("/idDoChat"));
		assertTrue(this.answerTo("/explicarEsteBot").startsWith("Bot de rotinas administrativas"));
	}

	@Test
	public void setLanguageAloneShowsTheCurrentLanguageAndTheOptions() {
		String answer = this.answerTo("/definirIdioma");

		assertEquals("Seu idioma atual é português. Para trocar, digite /definirIdioma seguido de um destes idiomas: português, inglês", answer);
	}

	/** After choosing English, the bot answers and lists the commands in English, until Portuguese is chosen again. */
	@Test
	public void setLanguageChangesTheLanguageOfTheChat() {
		assertEquals("Language set: English.", this.answerTo("/definirIdioma inglês"));

		String commandsInEnglish = this.answerTo("/showAllCommands");
		assertEquals("/solveLoginTokenTicket, /fixSkillHierarchy, /allowCommandToUser, /pendingTickets, "
				+ "/reviewSkillSuggestion, /chatId, /showAllCommands, /explainThisBot, /setLanguage", commandsInEnglish);
		assertTrue(this.answerTo("/explainThisBot").startsWith("Bot of administrative routines"));

		assertEquals("Idioma definido: português.", this.answerTo("/setLanguage Portugues"));
		assertTrue(this.answerTo("/showAllCommands").startsWith("/solucionarTicketsDeTokenDeLogin"));
	}

	/** Spanish has no texts of the bot, so it is not offered. */
	@Test
	public void setLanguageRefusesALanguageTheBotWasNotWrittenIn() {
		String answer = this.answerTo("/definirIdioma espanhol");

		assertEquals("Idioma não reconhecido: espanhol. Digite /definirIdioma seguido de um destes idiomas: português, inglês", answer);
	}

	/** A command in progress goes on in the new language. */
	@Test
	public void setLanguageMovesTheCommandInProgressToTheNewLanguage() {
		CcpJsonRepresentation session = this.sessionInTheMiddleOf("solveLoginTokenTicket");
		JbEntityBotCommandStepSession.ENTITY.save(session);
		try {
			this.answerTo("/definirIdioma english");

			String answer = this.answerTo("/exit");
			assertEquals("You left the command /solveLoginTokenTicket. Now you can run another command.", answer);
		} finally {
			JbEntityBotCommandStepSession.ENTITY.delete(session);
		}
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

	private boolean hasSavedSession(CcpJsonRepresentation session) {
		CcpJsonRepresentation sessionKey = session.getJsonPiece(JnJsonInstantMessengerFields.botName, JnJsonInstantMessengerFields.chatId);
		boolean exists = JbEntityBotCommandStepSession.ENTITY.exists(sessionKey);
		return exists;
	}

	/** In the middle of a command, /sair (the name in Portuguese) ends its session and tells which command was left. */
	@Test
	public void exitLeavesTheCommandInProgress() {
		CcpJsonRepresentation session = this.sessionInTheMiddleOf("solveLoginTokenTicket");
		JbEntityBotCommandStepSession.ENTITY.save(session);
		try {
			String answer = this.answerTo("/sair");

			assertEquals("Você saiu do comando /solucionarTicketsDeTokenDeLogin. Agora você pode executar outro comando.", answer);
			assertTrue("the session of the command left was ended", false == this.hasSavedSession(session));
		} finally {
			JbEntityBotCommandStepSession.ENTITY.delete(session);
		}
	}

	/** The canonical name works too, and the reply comes in the language of the session. */
	@Test
	public void exitAnswersInTheLanguageOfTheSession() {
		CcpJsonRepresentation session = this.sessionInTheMiddleOf("solveLoginTokenTicket").put(JnJsonCommonsFields.language, JnLanguage.english.name());
		JbEntityBotCommandStepSession.ENTITY.save(session);
		try {
			String answer = this.answerTo("/exit");

			assertEquals("You left the command /solveLoginTokenTicket. Now you can run another command.", answer);
			assertTrue("the session of the command left was ended", false == this.hasSavedSession(session));
		} finally {
			JbEntityBotCommandStepSession.ENTITY.delete(session);
		}
	}

	/** After leaving, the next text typed is no longer the answer to the step of the command left: it is a new command. */
	@Test
	public void afterExitAnotherCommandRuns() {
		CcpJsonRepresentation session = this.sessionInTheMiddleOf("solveLoginTokenTicket");
		JbEntityBotCommandStepSession.ENTITY.save(session);
		try {
			this.answerTo("/sair");

			assertEquals("" + SUPPORT_CHAT, this.answerTo("/chatId"));
		} finally {
			JbEntityBotCommandStepSession.ENTITY.delete(session);
		}
	}

	/** With a command in progress, /showAllCommands offers /sair. */
	@Test
	public void withACommandInProgressShowAllCommandsListsExit() {
		CcpJsonRepresentation session = this.sessionInTheMiddleOf("solveLoginTokenTicket");
		JbEntityBotCommandStepSession.ENTITY.save(session);
		try {
			String answer = this.answerTo("/showAllCommands");

			assertTrue(answer, answer.contains("/sair"));
		} finally {
			JbEntityBotCommandStepSession.ENTITY.delete(session);
		}
	}

	/** Without a command in progress there is nothing to leave: the bot shows the commands. */
	@Test
	public void withoutACommandInProgressExitShowsTheCommands() {
		JbEntityBotCommandStepSession.ENTITY.delete(this.sessionInTheMiddleOf("solveLoginTokenTicket"));

		String answer = this.answerTo("/sair");

		assertTrue("nothing to leave, so the bot shows all commands: " + answer, answer.startsWith("/solucionarTicketsDeTokenDeLogin"));
	}
}
