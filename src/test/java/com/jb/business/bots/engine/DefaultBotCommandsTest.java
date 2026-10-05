package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
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
import com.jn.json.fields.validation.JnJsonCommonsFields;
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
	public void showAllCommandsCurrentlyListsTheCanonicalNames() {
		String answer = this.answerTo("/showAllCommands");

		assertTrue(answer, answer.startsWith("/solveLoginTokenTicket,  /fixSkillHierarchy,  /allowCommandToUser,  /pendingTickets"));
		assertTrue(answer, answer.contains("/chatId"));
	}

	@Ignore("finding 48: getIdentifier must use the name of the command in the language (field message), the list must be separated by ', ' and must not show removeSession")
	@Test
	public void showAllCommandsListsTheVisibleCommandsByTheirNameInTheLanguage() {
		String answer = this.answerTo("/showAllCommands");

		assertTrue(answer, answer.contains("/solucionarTicketsDeTokenDeLogin"));
		assertTrue(answer, answer.contains(", /pendingTickets"));
		assertTrue(answer, false == answer.contains("/removeSession"));
	}

	@Test
	public void explainThisBotAnswersTheExplanationOfTheBot() {
		String answer = this.answerTo("/explainThisBot");

		assertTrue(answer, answer.startsWith("Bot de rotinas administrativas"));
	}

	@Test
	public void explainThisCommandIsCurrentlyUnreachableAndShowsTheCommands() {
		String answer = this.answerTo("/explainThisCommand");

		assertTrue("finding 48: the command is invisible without a current command, so the bot shows all commands: " + answer, answer.startsWith("/solveLoginTokenTicket"));
	}

	private String explanationOfTheTokenTicketCommand() {
		BotCommand command = JbBotEngine.INSTANCE.allCommands.get("solveLoginTokenTicket");
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.language, JnLanguage.portuguese);
		String explanation = command.getExplanation(session);
		return explanation;
	}

	@Test
	public void theExplanationOfACommandIsCurrentlyTheNameOfTheLanguage() {
		assertEquals("portuguese", this.explanationOfTheTokenTicketCommand());
	}

	@Ignore("finding 48: BotCommand.getExplanation must return the text of the explanation, not its language")
	@Test
	public void theExplanationOfACommandIsItsText() {
		assertTrue(this.explanationOfTheTokenTicketCommand().startsWith("Quando o usu"));
	}
}
