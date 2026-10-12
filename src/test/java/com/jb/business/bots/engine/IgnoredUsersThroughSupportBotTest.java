package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import com.jb.entities.JbEntityBotCommand;
import com.jb.entities.JbEntityBotCommandName;
import com.jb.entities.JbEntityBotCommandStep;
import com.jb.entities.JbEntityBotCommandStepEndMessage;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jn.entities.decorators.ElasticsearchLocal;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.entities.VisEntityCommandNotAllowedToUser;
import com.vis.entities.VisEntitySkillPending;
import com.vis.json.fields.validation.VisUserRequestCommands;

/**
 * Exercises the {@code /ignoredUsers} command ({@code /listarUsuariosIgnorados}), which goes through the users ignored
 * by the support with the same interaction of {@code /pendingTickets}: the count and one user at a time, {@code 2} goes
 * to the next one, {@code 1} runs {@code /allowCommandToUser} for the user shown and then shows the list again, without
 * that user. Other tests may leave ignored users behind, so the test looks for its own users in the list instead of
 * counting them.
 *
 * <p>The database is the real Elasticsearch; Telegram and the e-mail provider are test doubles.
 */
public class IgnoredUsersThroughSupportBotTest {

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

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final String FIRST_USER = "ignorado.lista.a@teste.com";

	private static final String SECOND_USER = "ignorado.lista.b@teste.com";

	/** Saves the bot configuration and its texts again, so that the index follows what is declared in java. */
	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(new JbEntityBot(), new JbEntityBotCommand(), new JbEntityBotCommandName(), new JbEntityBotCommandStep(), new JbEntityBotCommandStepEndMessage());
		for (CcpEntityConfigurator configurator : configurators) {
			List<CcpBulkItem> records = configurator.getFirstRecordsToInsert();
			for (CcpBulkItem record : records) {
				record.entity.save(record.json);
			}
		}
	}

	@Before
	public void ignoreTheUsers() {
		this.clearTheBotSession();
		this.ignore(FIRST_USER, "TESTE IGNORADO A");
		this.ignore(SECOND_USER, "TESTE IGNORADO B");
		ElasticsearchLocal.refresh("vis_command_not_allowed_to_user");
		TELEGRAM.sentMessages.clear();
	}

	@After
	public void allowTheUsers() {
		this.clearTheBotSession();
		for (String email : Arrays.asList(FIRST_USER, SECOND_USER)) {
			CcpJsonRepresentation ignoredUser = CcpOtherConstants.EMPTY_JSON.put(VisEntityCommandNotAllowedToUser.Fields.email, email);
			VisEntityCommandNotAllowedToUser.ENTITY.delete(ignoredUser);
		}
		ElasticsearchLocal.refresh("vis_command_not_allowed_to_user");
	}

	/**
	 * The list shows one user at a time, with the command in which they were ignored and what they had asked; {@code 1}
	 * stops ignoring the user shown (the record goes to the twin) and the list comes back without them. Works with
	 * the Portuguese name of the command too.
	 */
	@Test
	public void theOperatorStopsIgnoringAUserFromTheList() {

		this.type("/listarUsuariosIgnorados");
		String header = this.lastMessage();
		assertTrue(header, header.startsWith("O suporte está ignorando "));
		assertTrue(header, header.contains("Usuário 1 de "));

		String prompt = this.goTo(FIRST_USER);
		assertTrue(prompt, prompt.contains("=> " + FIRST_USER + "\n"));
		assertTrue(prompt, prompt.contains("Ignorado no comando reviewSkillSuggestion (TESTE IGNORADO A)"));
		assertTrue(prompt, prompt.contains("Digite \"1\" para voltar a atendê-lo ou \"2\" para ir ao próximo"));

		TELEGRAM.sentMessages.clear();
		this.type("1");

		List<String> replies = this.allMessages();
		assertTrue(replies.toString(), replies.contains("Voltando a atender o usuário " + FIRST_USER));
		assertTrue(replies.toString(), replies.stream().anyMatch(reply -> reply.startsWith("O usuário " + FIRST_USER + " não é mais ignorado pelo suporte")));
		CcpJsonRepresentation firstUser = CcpOtherConstants.EMPTY_JSON.put(VisEntityCommandNotAllowedToUser.Fields.email, FIRST_USER);
		assertFalse(VisEntityCommandNotAllowedToUser.ENTITY.exists(firstUser));
		assertTrue(VisEntityCommandNotAllowedToUser.ENTITY.getTwinEntity().exists(firstUser));

		// the list is shown again, and the user just allowed is no longer in it
		String listAgain = this.lastMessage();
		assertTrue(listAgain, listAgain.startsWith("O suporte está ignorando "));
		assertFalse(this.listContains(FIRST_USER));
		assertTrue(this.listContains(SECOND_USER));
	}

	/** An answer that is neither of the options shows the same user again. */
	@Test
	public void anAnswerNotUnderstoodShowsTheSameUser() {

		this.type("/ignoredUsers");
		String prompt = this.goTo(SECOND_USER);

		this.type("talvez");
		assertEquals("Não entendi a resposta.\n\n" + prompt, this.lastMessage());
	}

	/**
	 * Goes through the list with {@code 2} until the given user is shown.
	 * @return the prompt of the user
	 */
	private String goTo(String email) {
		for (int answers = 0; answers < 100; answers++) {
			String lastMessage = this.lastMessage();
			boolean userShown = lastMessage.contains("=> " + email + "\n");
			if (userShown) {
				String prompt = lastMessage.substring(lastMessage.indexOf("Usuário "));
				return prompt;
			}
			this.type("2");
		}
		throw new AssertionError("The user " + email + " is not in the list: " + this.lastMessage());
	}

	/** Lists the ignored users again, in a new session, and tells whether the user is in the list. */
	private boolean listContains(String email) {
		this.clearTheBotSession();
		this.type("/ignoredUsers");
		try {
			this.goTo(email);
			return true;
		} catch (AssertionError notInTheList) {
			return false;
		} finally {
			this.clearTheBotSession();
		}
	}

	private void ignore(String email, String skill) {
		CcpJsonRepresentation description = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillPending.Fields.skill, skill)
				.put(VisEntitySkillPending.Fields.description, "Pedido que levou o operador a ignorar o usuário")
				.put(JnJsonCommonsFields.email, email);
		CcpJsonRepresentation ignoredUser = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityCommandNotAllowedToUser.Fields.email, email)
				.put(VisEntityCommandNotAllowedToUser.Fields.commandName, VisUserRequestCommands.reviewSkillSuggestion)
				.put(VisEntityCommandNotAllowedToUser.Fields.description, description);
		VisEntityCommandNotAllowedToUser.ENTITY.save(ignoredUser);
	}

	private void clearTheBotSession() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		JbEntityBotCommandStepSession.ENTITY.delete(session);
	}

	private void type(String text) {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT)
				.put(JnJsonCommonsFields.message, text)
				.put(JbBotEngine.Fields.message_id, 1);
		JbBotType.support.getBot().execute(message);
	}

	private String lastMessage() {
		CcpJsonRepresentation reply = TELEGRAM.lastMessageFor(SUPPORT_CHAT);
		String replyText = reply.getAsString(JnJsonCommonsFields.message);
		return replyText;
	}

	private List<String> allMessages() {
		List<String> messages = TELEGRAM.sentMessages.stream().map(sent -> sent.getAsString(JnJsonCommonsFields.message)).collect(Collectors.toList());
		return messages;
	}
}
