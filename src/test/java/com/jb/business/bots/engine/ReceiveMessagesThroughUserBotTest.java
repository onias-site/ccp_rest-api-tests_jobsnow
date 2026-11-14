package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.email.CcpEmailProvider;
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
import com.jb.entities.JbEntityBotCommandStepSession;
import com.ccp.especifications.db.utils.entity.decorators.ElasticsearchLocal;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.entities.JnEntityInstantMessengerUser;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.vis.entities.VisEntityLoginTokenAttempts;
import com.vis.entities.VisEntityResume;

/**
 * The commands {@code /receiveMessages <email>} (vis) and {@code /resendLoginToken <email>} (js) of the user bot (since
 * 2026-11-12): the right token saves the chat of the e-mail in {@code jn_instant_messenger_user} (since 2026-11-13; until
 * then in the resume) once the e-mail has a resume, active or inactive, deleting the record of the chat under a previous
 * e-mail; an e-mail already registered with the chat ends the command; a wrong token is counted in
 * {@code vis_login_token_attempts} and, at the third, blocks the chat; the resending answers as the login screen.
 */
public class ReceiveMessagesThroughUserBotTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailProvider> email = () -> new FakeEmailSender();

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
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

	private static final Long USER_CHAT = 987654330L;

	private static final String TOKEN = "TK7Q2M9X";

	private final long run = System.currentTimeMillis();

	private final String email = "receber.mensagens" + this.run + "@teste.com";

	private final List<String> resumeEmails = new ArrayList<>();

	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(JbEntityBot.INSTANCE, JnEntityBotCommand.INSTANCE, JnEntityBotCommandStep.INSTANCE,
				JnEntityBotCommandStepEndMessage.INSTANCE, VisEntityLoginTokenAttempts.INSTANCE, JsEntityLoginSessionValidation.INSTANCE,
				JsEntityLoginTokenRequestResend.INSTANCE);
		for (CcpEntityConfigurator configurator : configurators) {
			List<CcpBulkItem> records = configurator.getFirstRecordsToInsert();
			for (CcpBulkItem record : records) {
				boolean scripted = record.operation != CcpBulkEntityOperationType.create;
				if(scripted) {
					continue;
				}
				record.entity.save(record.json);
			}
		}
	}

	@Before
	public void clearTheChat() {
		this.deleteTheSessionAndTheAttempts();
		TELEGRAM.sentMessages.clear();
	}

	@After
	public void deleteWhatTheTestCreated() {
		this.deleteTheSessionAndTheAttempts();
		for (String resumeEmail : this.resumeEmails) {
			VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, resumeEmail));
			JnEntityInstantMessengerUser.ENTITY.delete(this.instantMessengerUserKey(resumeEmail));
		}
		JnEntityInstantMessengerUser.ENTITY.delete(this.instantMessengerUserKey(this.email));
		CcpJsonRepresentation tokenKey = CcpOtherConstants.EMPTY_JSON.put(JsEntityLoginToken.Fields.email, this.email);
		JsEntityLoginToken.ENTITY.deleteAnyWhere(tokenKey);
		JsEntityLoginTokenRequestResend.ENTITY.deleteAnyWhere(tokenKey);
	}

	/** The right token saves the chat of the e-mail, not in the resume, and clears the wrong tokens typed before. */
	@Test
	public void theRightTokenSavesTheChatInTheResume() {
		this.saveTheLoginToken();
		this.saveResume(this.email);

		String askToken = this.userTypes("/receiveMessages " + this.email);
		assertEquals("Digite o token de login que enviamos ao e-mail " + this.email + ".", askToken);
		String wrong = this.userTypes("ERRADO00");
		assertTrue(wrong, wrong.startsWith("O token informado está errado. Restam 2 tentativa(s)."));
		assertTrue(VisEntityLoginTokenAttempts.ENTITY.exists(this.attemptsKey()));

		String saved = this.userTypes(TOKEN);

		assertEquals("Pronto! A partir de agora você receberá por aqui as mensagens do Onias.Site sobre o seu currículo.", saved);
		assertEquals(USER_CHAT, this.chatOf(this.email));
		CcpJsonRepresentation resume = VisEntityResume.ENTITY.getOneById(this.resumeKey(this.email));
		assertFalse(resume.containsField(VisEntityResume.Fields.chatId));
		assertFalse(VisEntityLoginTokenAttempts.ENTITY.exists(this.attemptsKey()));
	}

	/** An inactive resume also counts: the chat of the e-mail is saved, and the resume stays inactive. */
	@Test
	public void theChatGoesToTheInactiveResume() {
		this.saveTheLoginToken();
		this.saveResume(this.email);
		VisEntityResume.ENTITY.delete(this.resumeKey(this.email));
		CcpEntity inactiveResumes = VisEntityResume.ENTITY.getTwinEntity();
		assertTrue(inactiveResumes.exists(this.resumeKey(this.email)));

		this.userTypes("/receiveMessages " + this.email);
		String saved = this.userTypes(TOKEN);

		assertTrue(saved, saved.startsWith("Pronto!"));
		assertFalse(VisEntityResume.ENTITY.exists(this.resumeKey(this.email)));
		assertTrue(inactiveResumes.exists(this.resumeKey(this.email)));
		assertEquals(USER_CHAT, this.chatOf(this.email));
	}

	/** The e-mail already registered with this chat ends the command with that notice, before asking the token. */
	@Test
	public void anEmailAlreadyRegisteredWithTheChatEndsTheCommand() {
		this.saveTheLoginToken();
		this.saveResume(this.email);
		this.saveTheChat(this.email, USER_CHAT);

		String answer = this.userTypes("/receiveMessages " + this.email);

		assertEquals("O e-mail " + this.email + " já está cadastrado para receber por aqui as mensagens do Onias.Site sobre o seu currículo. Não é preciso fazer mais nada.", answer);
		String afterTheEnd = this.userTypes(TOKEN);
		assertFalse(afterTheEnd, afterTheEnd.startsWith("Pronto!"));
		assertEquals(USER_CHAT, this.chatOf(this.email));
	}

	/** The e-mail registered with another chat is not a repetition: the right token moves it to this chat. */
	@Test
	public void theRightTokenMovesTheEmailFromAnotherChat() {
		this.saveTheLoginToken();
		this.saveResume(this.email);
		this.saveTheChat(this.email, USER_CHAT + 1);

		String askToken = this.userTypes("/receiveMessages " + this.email);
		assertTrue(askToken, askToken.startsWith("Digite o token de login"));
		String saved = this.userTypes(TOKEN);

		assertTrue(saved, saved.startsWith("Pronto!"));
		assertEquals(USER_CHAT, this.chatOf(this.email));
	}

	/** A chat answers for one e-mail: registering another e-mail deletes the record of the previous one. */
	@Test
	public void changingTheEmailDeletesThePreviousOne() {
		String previousEmail = "receber.mensagens.anterior" + this.run + "@teste.com";
		this.saveResume(previousEmail);
		this.saveTheChat(previousEmail, USER_CHAT);
		this.saveTheLoginToken();
		this.saveResume(this.email);

		this.userTypes("/receiveMessages " + this.email);
		String saved = this.userTypes(TOKEN);

		assertTrue(saved, saved.startsWith("Pronto!"));
		assertEquals(USER_CHAT, this.chatOf(this.email));
		assertFalse(JnEntityInstantMessengerUser.ENTITY.exists(this.instantMessengerUserKey(previousEmail)));
	}

	/** Without a resume, active or inactive, the user is asked to register one. */
	@Test
	public void withoutResumeTheUserIsAskedToRegisterOne() {
		this.saveTheLoginToken();

		this.userTypes("/receiveMessages " + this.email);
		String answer = this.userTypes(TOKEN);

		assertEquals("Não encontramos currículo cadastrado para o e-mail " + this.email + ". Cadastre o seu currículo no Onias.Site e depois use de novo o comando /receiveMessages " + this.email, answer);
	}

	/**
	 * Each wrong token tells the attempts left (3 - attempts) and the command to resend the token; the third blocks the
	 * chat, which can no longer use the command, even with the right token.
	 */
	@Test
	public void theThirdWrongTokenBlocksTheChat() {
		this.saveTheLoginToken();
		this.saveResume(this.email);
		this.userTypes("/receiveMessages " + this.email);

		String first = this.userTypes("ERRADO01");
		String second = this.userTypes("ERRADO02");
		String third = this.userTypes("ERRADO03");

		String resend = " Se você não recebeu ou perdeu o token original, use o comando /resendLoginToken " + this.email;
		assertEquals("O token informado está errado. Restam 2 tentativa(s)." + resend, first);
		assertEquals("O token informado está errado. Restam 1 tentativa(s)." + resend, second);
		String blocked = "Você excedeu o número de tentativas e está bloqueado: não poderá mais usar o comando /receiveMessages por este bot.";
		assertEquals(blocked, third);
		CcpJsonRepresentation attempts = VisEntityLoginTokenAttempts.ENTITY.getOneById(this.attemptsKey());
		assertEquals(Integer.valueOf(3), attempts.getAsIntegerNumber(VisEntityLoginTokenAttempts.Fields.attempts));

		String again = this.userTypes("/receiveMessages " + this.email);
		assertEquals(blocked, again);
		String token = this.userTypes(TOKEN);
		assertFalse(token, token.startsWith("Pronto!"));
		assertFalse(JnEntityInstantMessengerUser.ENTITY.exists(this.instantMessengerUserKey(this.email)));
	}

	/** An invalid e-mail ends the command before asking the token. */
	@Test
	public void anInvalidEmailEndsTheCommand() {
		String answer = this.userTypes("/receiveMessages nao-e-email");

		assertEquals("O e-mail 'nao-e-email' informado é inválido. Uso: /receiveMessages seu@email.com", answer);
	}

	/** /resendLoginToken answers as the login screen: no token, request made, request already made. */
	@Test
	public void theResendingAnswersAsTheLoginScreen() {
		String withoutToken = this.userTypes("/resendLoginToken " + this.email);
		assertEquals("O e-mail '" + this.email + "' não tem token de login. Para receber um, entre no Onias.Site com este e-mail.", withoutToken);

		this.saveTheLoginToken();
		String requested = this.userTypes("/resendLoginToken " + this.email);
		assertEquals("A solicitação de reenvio do token para o e-mail '" + this.email + "' foi efetuada com sucesso, por favor, verifique a caixa de entrada, spam / lixo eletrônico deste e-mail para localizar o token que enviamos.", requested);

		String alreadyRequested = this.userTypes("/resendLoginToken " + this.email);
		String expected = "A solicitação de reenvio do token para o e-mail '" + this.email + "' já foi feita na data \\S.+, se necessário, poderá ser refeita na data \\S.+";
		assertTrue(alreadyRequested, alreadyRequested.matches(expected));

		String invalid = this.userTypes("/resendLoginToken nao-e-email");
		assertEquals("O e-mail 'nao-e-email' informado é inválido, por favor, informe um e-mail válido. Uso: /resendLoginToken seu@email.com", invalid);
	}

	private void saveTheLoginToken() {
		CcpJsonRepresentation loginToken = CcpOtherConstants.EMPTY_JSON
				.put(JsEntityLoginToken.Fields.email, this.email)
				.put(JnJsonCommonsFields.originalToken, TOKEN);
		JsEntityLoginToken.ENTITY.save(loginToken);
	}

	private void saveResume(String owner) {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, owner)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(21))
				.put(VisEntityResume.Fields.desiredJob, "Analista mensagens " + this.run)
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 6)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/receber-mensagens-" + this.run)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.clt, 8_000)
				.put(VisEntityResume.Fields.pj, 12_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[]}")))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(new CcpJsonRepresentation("{\"name\":\"Inglês\",\"level\":2,\"id\":0}")));
		VisEntityResume.ENTITY.save(resume);
		this.resumeEmails.add(owner);
	}

	private CcpJsonRepresentation resumeKey(String owner) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, owner);
	}

	private CcpJsonRepresentation instantMessengerUserKey(String owner) {
		return CcpOtherConstants.EMPTY_JSON.put(JnEntityInstantMessengerUser.Fields.email, owner);
	}

	/** Saves the chat of the e-mail and makes it visible to the search by chat right away. */
	private void saveTheChat(String owner, Long chatId) {
		CcpJsonRepresentation instantMessengerUser = this.instantMessengerUserKey(owner).put(JnEntityInstantMessengerUser.Fields.chatId, chatId);
		JnEntityInstantMessengerUser.ENTITY.save(instantMessengerUser);
		ElasticsearchLocal.refresh("jn_instant_messenger_user");
	}

	private Long chatOf(String owner) {
		CcpJsonRepresentation stored = JnEntityInstantMessengerUser.ENTITY.getOneById(this.instantMessengerUserKey(owner));
		return stored.getAsLongNumber(JnEntityInstantMessengerUser.Fields.chatId);
	}

	private CcpJsonRepresentation attemptsKey() {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityLoginTokenAttempts.Fields.chatId, USER_CHAT);
	}

	private void deleteTheSessionAndTheAttempts() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.user)
				.put(JnJsonInstantMessengerFields.chatId, USER_CHAT);
		JbEntityBotCommandStepSession.ENTITY.delete(session);
		VisEntityLoginTokenAttempts.ENTITY.delete(this.attemptsKey());
	}

	private String userTypes(String text) {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.user)
				.put(JnJsonInstantMessengerFields.chatId, USER_CHAT)
				.put(JnJsonCommonsFields.message, text)
				.put(JbBotEngine.Fields.message_id, 1);
		JbBotType.user.getBot().execute(message);
		CcpJsonRepresentation reply = TELEGRAM.lastMessageFor(USER_CHAT);
		String replyText = reply.getAsString(JnJsonCommonsFields.message);
		return replyText;
	}
}
