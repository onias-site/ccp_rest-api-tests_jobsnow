package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.business.bots.login.token.JbSupportLoginTokenTypes;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.business.messages.JnMessages;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Exercises the handling of a token unlock ticket from start to finish: the message arrives the way
 * the Telegram reader delivers it, the {@code Bot} identifies the command and the
 * {@code JbSupportLoginToken} engine solves the ticket.
 *
 * <p>The database is the real Elasticsearch, as in the other flow tests, because that is where
 * {@code JbBotEngine} reads the whole bot configuration from. Telegram and the e-mail provider, on the
 * other hand, are replaced by test doubles: the support bot token configured in the environment is
 * real, and the test needs to read back the token that went to the user to check that it is the same
 * one that went to support.
 */
public class UnlockTokenThroughSupportBotTest {

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
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	private static final String USER_EMAIL = "onias85@gmail.com";

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final String MESSAGE_RECEIVED_BY_THE_BOT = "{"
			+ "\"botName\": \"support\","
			+ "\"chatId\": 751717896,"
			+ "\"message\": \"/solveLoginTokenTicket unlockToken " + USER_EMAIL + "\","
			+ "\"message_id\": 363,"
			+ "\"sentAt\": 1790121741,"
			+ "\"updateId\": 840339225,"
			+ "\"userName\": \"OniasJr\""
			+ "}";

	/**
	 * The text is the one registered in {@code JnEntityInstantMessengerTemplateMessage}, with
	 * {@code {email}} resolved. Only the token is left out: it is drawn at each handling, and the test
	 * reads it from the message itself to check it against what was sent by e-mail.
	 */
	private static final String SUPPORT_NOTICE_UP_TO_THE_TOKEN = "Ao endereço " + USER_EMAIL
			+ ", envie a seguinte mensagem:\n\n\n"
			+ "Você solicitou o desbloqueio de seu token para (re) cadastro / desbloqueio de senha."
			+ " Atendendo ao seu pedido. O token que você deve informar no campo de token é ";

	@Before
	public void setUpScenario() {

		this.resaveMessageTemplates();

		CcpJsonRepresentation user = this.user();

		// saving to the twin deletes the record from the main entity: that is how the token gets locked
		JnEntityLoginToken.ENTITY.getTwinEntity().save(user);

		this.openUnlockTicket(user);

		TELEGRAM.sentMessages.clear();
		EMAIL_INBOX.sentEmails.clear();
	}

	/**
	 * Leaves the ticket open in the main entity by saving it — saving to a twin entity writes to the
	 * main one and deletes from the twin, so the save covers both the case of there being no ticket at
	 * all and the case of one handled in a previous run.
	 *
	 * <p>Saving is what notifies support, and that is why the record of that notice has to be cleared
	 * out of the way: the text is fixed, so it would be refused as a repetition of the notice from the
	 * previous run.
	 *
	 * <p>A previous version queried {@code exists} to decide between saving and transferring back from
	 * the twin. That does not work: the entity is disposable by day, and the {@code exists} of a
	 * disposable entity answers based on the copy in {@code disposable_record}, which remains valid after
	 * the document has already changed buckets. At the turn of the day it said the record existed and the
	 * transfer could not find it.
	 */
	private void openUnlockTicket(CcpJsonRepresentation user) {

		this.deleteAlreadySentPendingTicketNotice();

		JnEntityLoginTokenRequestUnlock.ENTITY.save(user);
	}

	@Test
	public void ticketIsHandled() {

		CcpJsonRepresentation message = new CcpStringDecorator(MESSAGE_RECEIVED_BY_THE_BOT).json();

		JbBotType.support.getBot().execute(message);

		CcpJsonRepresentation supportNotice = TELEGRAM.lastMessageFor(SUPPORT_CHAT);
		String noticeText = supportNotice.getAsString(JnJsonCommonsFields.message);

		boolean noticeStartsWithExpectedText = noticeText.startsWith(SUPPORT_NOTICE_UP_TO_THE_TOKEN);
		assertTrue(noticeText, noticeStartsWithExpectedText);

		int prefixLength = SUPPORT_NOTICE_UP_TO_THE_TOKEN.length();
		String token = noticeText.substring(prefixLength);

		// 4) support receives the notice of the handled ticket, with the token resolved
		assertEquals(SUPPORT_NOTICE_UP_TO_THE_TOKEN + token, noticeText);
		assertEquals("support", supportNotice.getAsString(JnJsonInstantMessengerFields.botName));

		// 1) the user receives the same token by e-mail, through the JnNotifyUserAboutLoginToken template
		CcpJsonRepresentation email = this.emailSentToUser();
		String emailBody = email.getAsString(JnJsonCommonsFields.message);
		List<String> sentRecipients = email.getAsStringList(JnJsonCommonsFields.email);

		assertTrue(sentRecipients.contains(USER_EMAIL));
		assertTrue(emailBody, emailBody.contains(token));

		CcpJsonRepresentation user = this.user();

		// 2) the previous token left login_token and its twin; what is there now is the new token
		CcpEntity lockedToken = JnEntityLoginToken.ENTITY.getTwinEntity();
		assertFalse(lockedToken.exists(user));
		assertTrue(JnEntityLoginToken.ENTITY.exists(user));

		// 3) the ticket left the main entity and is in the twin
		CcpEntity handledTicket = JnEntityLoginTokenRequestUnlock.ENTITY.getTwinEntity();
		assertFalse(JnEntityLoginTokenRequestUnlock.ENTITY.exists(user));
		assertTrue(handledTicket.exists(user));
	}

	private CcpJsonRepresentation emailSentToUser() {

		String tokenTemplateId = JnMessages.JnNotifyUserAboutLoginToken.class.getName();

		for (CcpJsonRepresentation sentEmail : EMAIL_INBOX.sentEmails) {
			String templateId = sentEmail.getAsString(JnJsonCommonsFields.templateId);
			boolean isAnotherTemplate = false == templateId.equals(tokenTemplateId);
			if (isAnotherTemplate) {
				continue;
			}
			return sentEmail;
		}
		throw new AssertionError("No email was sent with the template " + tokenTemplateId);
	}

	private CcpJsonRepresentation user() {
		CcpJsonRepresentation user = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, USER_EMAIL)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		return user;
	}

	/**
	 * Saves the initial records of the template entity again so that the index follows the text
	 * declared in java. Without this the test would read whatever text has been in the database since the
	 * tables were last created, and would end up measuring the state of the environment instead of the
	 * code.
	 *
	 * <p>The initial records come as bulk items, but they are saved one by one through the entity: the
	 * bulk operation declared in them is the create one, which does not overwrite the record already
	 * there.
	 */
	private void resaveMessageTemplates() {
		JnEntityInstantMessengerTemplateMessage configurator = new JnEntityInstantMessengerTemplateMessage();
		List<CcpBulkItem> templates = configurator.getFirstRecordsToInsert();
		for (CcpBulkItem template : templates) {
			template.entity.save(template.json);
		}
	}

	/**
	 * The pending ticket notice has a fixed text, so the "message already sent" record always has the
	 * same key. Without deleting it, opening the ticket would be refused as a repetition of the previous
	 * notice, and the refusal interrupts saving the ticket.
	 *
	 * <p>The chatId goes in as a {@code double} because that is how it comes back from the database in
	 * the send that saved the record, and the primary key is calculated over the text of the values: a
	 * {@code long} would produce {@code 751717896} where the saved record has {@code 7.51717896E8}, and
	 * the key would not match.
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
