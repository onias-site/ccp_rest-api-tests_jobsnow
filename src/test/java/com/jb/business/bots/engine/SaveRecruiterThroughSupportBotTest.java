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
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandName;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.business.recruiter.VisBusinessPrepareRecruiter;
import com.vis.entities.VisEntityRecruiter;
import com.vis.entities.VisEntityRecruiterSignUpReturn;

/**
 * {@code /saveRecruiter <email>} of the support bot (since 2026-11-14): the operator saves an e-mail as a recruiter in
 * {@code vis_recruiter}, normalized and with the source {@code support}; an invalid address writes nothing, a recruiter
 * already active is only reported and an inactive one goes back to the recruiters.
 */
public class SaveRecruiterThroughSupportBotTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	private static final FakeEmailSender EMAIL_INBOX = new FakeEmailSender();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailProvider> email = () -> EMAIL_INBOX;

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

	private static final Long SUPPORT_CHAT = 751717896L;

	private final long run = System.currentTimeMillis();

	private final String recruiter = "recrutador.suporte" + this.run + "@empresateste.com.br";

	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(JbEntityBot.INSTANCE, JnEntityBotCommand.INSTANCE, JnEntityBotCommandStep.INSTANCE, JnEntityBotCommandStepEndMessage.INSTANCE, JnEntityBotCommandName.INSTANCE, VisEntityRecruiterSignUpReturn.INSTANCE);
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
		// the engine finds the commands of the bot by a query when it starts: the new command must be visible to it
		String commandsIndex = JnEntityBotCommand.ENTITY.getEntityMetaData().entityName;
		ElasticsearchLocal.refresh(commandsIndex);
	}

	@Before
	public void clearTheBotSession() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		JbEntityBotCommandStepSession.ENTITY.delete(session);
		TELEGRAM.sentMessages.clear();
	}

	@After
	public void deleteWhatTheTestCreated() {
		VisEntityRecruiter.ENTITY.deleteAnyWhere(this.recruiterKey());
		CcpJsonRepresentation signUpReturn = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterSignUpReturn.Fields.email, this.recruiter);
		VisEntityRecruiterSignUpReturn.ENTITY.delete(signUpReturn);
	}

	private CcpJsonRepresentation recruiterKey() {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, this.recruiter);
	}

	/** A new e-mail, typed with capitals, is saved normalized, with the source support and its domain. */
	@Test
	public void aNewRecruiterIsSaved() {
		String typed = this.recruiter.toUpperCase();
		String reply = this.operatorTypes("/saveRecruiter " + typed);

		assertEquals("O recrutador " + this.recruiter + " foi cadastrado.", reply);
		CcpJsonRepresentation saved = VisEntityRecruiter.ENTITY.getOneById(this.recruiterKey());
		assertEquals(this.recruiter, saved.getAsString(VisEntityRecruiter.Fields.email));
		assertEquals(VisEntityRecruiter.Sources.support.name(), saved.getAsString(VisEntityRecruiter.Fields.source));
		assertEquals("empresateste.com.br", saved.getAsString(VisEntityRecruiter.Fields.domain));
	}

	/** The name in Portuguese runs the same command. */
	@Test
	public void theNameInPortugueseSavesTheRecruiter() {
		String reply = this.operatorTypes("/salvarRecrutador " + this.recruiter);

		assertEquals("O recrutador " + this.recruiter + " foi cadastrado.", reply);
		assertTrue(VisEntityRecruiter.ENTITY.exists(this.recruiterKey()));
	}

	/** A recruiter already active is only reported: the record keeps its source. */
	@Test
	public void anActiveRecruiterIsOnlyReported() {
		CcpJsonRepresentation loginRecruiter = this.recruiterKey().put(VisEntityRecruiter.Fields.source, VisEntityRecruiter.Sources.login.name());
		VisEntityRecruiter.ENTITY.save(VisBusinessPrepareRecruiter.INSTANCE.execute(loginRecruiter));

		String reply = this.operatorTypes("/saveRecruiter " + this.recruiter);

		assertEquals("O e-mail " + this.recruiter + " já está cadastrado como recrutador.", reply);
		CcpJsonRepresentation stored = VisEntityRecruiter.ENTITY.getOneById(this.recruiterKey());
		assertEquals(VisEntityRecruiter.Sources.login.name(), stored.getAsString(VisEntityRecruiter.Fields.source));
	}

	/** An inactive recruiter goes back to the recruiters, with the record they had, and the return is kept. */
	@Test
	public void anInactiveRecruiterIsReactivated() {
		CcpJsonRepresentation loginRecruiter = this.recruiterKey().put(VisEntityRecruiter.Fields.source, VisEntityRecruiter.Sources.login.name());
		VisEntityRecruiter.ENTITY.save(VisBusinessPrepareRecruiter.INSTANCE.execute(loginRecruiter));
		VisEntityRecruiter.ENTITY.delete(this.recruiterKey());
		assertTrue(VisEntityRecruiter.ENTITY.getTwinEntity().exists(this.recruiterKey()));

		String reply = this.operatorTypes("/saveRecruiter " + this.recruiter);

		assertEquals("O recrutador " + this.recruiter + " estava inativo e foi reativado.", reply);
		assertTrue(VisEntityRecruiter.ENTITY.exists(this.recruiterKey()));
		assertFalse(VisEntityRecruiter.ENTITY.getTwinEntity().exists(this.recruiterKey()));
		CcpJsonRepresentation stored = VisEntityRecruiter.ENTITY.getOneById(this.recruiterKey());
		assertEquals(VisEntityRecruiter.Sources.login.name(), stored.getAsString(VisEntityRecruiter.Fields.source));
		CcpJsonRepresentation signUpReturn = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterSignUpReturn.Fields.email, this.recruiter);
		assertTrue(VisEntityRecruiterSignUpReturn.ENTITY.exists(signUpReturn));
	}

	/** An address that is not valid writes nothing. */
	@Test
	public void anInvalidEmailIsRefused() {
		for (String invalid : Arrays.asList("recrutador.sem.arroba" + this.run, "recrutador" + this.run + "@empresa", "a@b@empresa.com.br", "recrutador" + this.run + "@empresa.coom.br")) {
			this.clearTheBotSession();
			String reply = this.operatorTypes("/saveRecruiter " + invalid);
			assertEquals(invalid, "O e-mail " + invalid + " não é válido. Nenhum recrutador foi cadastrado.", reply);
			CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, invalid.toLowerCase());
			assertFalse(invalid, VisEntityRecruiter.ENTITY.exists(key));
		}
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
}
