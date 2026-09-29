package com.ccp.especifications.db.utils.entity.decorators.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.text.extractor.apache.tika.CcpApacheTikaTextExtractor;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityContactUs;
import com.jn.entities.JnEntityContactUsIgnored;
import com.jn.entities.JnEntityJobsnowPenddingError;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.entities.VisEntityDeniedViewToCompany;
import com.vis.entities.VisEntityPosition;
import com.vis.entities.VisEntityResume;

/**
 * Covers {@code deleteAnyWhere} of every entity annotated with {@code @CcpEntityTwin}. The method's
 * promise is to delete the record wherever it is, so each entity is exercised twice: with the record
 * in the main entity and with the record in the twin. In both cases the method must report that the
 * record existed and must leave both indexes without it.
 *
 * <p>Each run uses a new e-mail, derived from the clock. The entities have the e-mail in the primary
 * key, so this gives a clean scenario without depending on cleanup, and prevents fixed-text messages
 * — the one that notifies support about a pending ticket, for example — from being rejected as a
 * repetition of the previous run.
 *
 * <p>The database is the real Elasticsearch. Telegram and the email provider are test doubles, because
 * saving some of these entities triggers a notice to support or a message to the user.
 */
public class DeleteAnyWhereOnTwinEntitiesTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> new SilentInstantMessenger();
		CcpInstanceProvider<CcpEmailSender> email = () -> new SilentEmailSender();

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				new CcpApacheTikaTextExtractor(),
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

	private final String email = "twin" + System.currentTimeMillis() + "@jobsnow.com";

	// ── entities without side effects on save ────────────────────────────────

	@Test
	public void contactUs() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JnJsonCommonsFields.subjectType, "question")
				.put(JnJsonCommonsFields.subject, "test subject")
				.put(JnJsonInstantMessengerFields.chatId, 751717896L)
				.put(JnJsonCommonsFields.sender, "devs.jobsnow@gmail.com");

		this.shouldDeleteFromBothIndexes(JnEntityContactUs.ENTITY, record);
	}

	@Test
	public void contactUsIgnored() {
		this.shouldDeleteFromBothIndexes(JnEntityContactUsIgnored.ENTITY, this.withEmail());
	}

	@Test
	public void jobsnowPenddingError() {
		CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityJobsnowPenddingError.Fields.stackTraceHash, this.email)
				.put(JnEntityJobsnowPenddingError.Fields.type, "java.lang.RuntimeException")
				.put(JnJsonCommonsFields.message, "test error");

		this.shouldDeleteFromBothIndexes(JnEntityJobsnowPenddingError.ENTITY, record);
	}

	@Test
	public void loginPassword() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JnJsonCommonsFields.password, "Senha#12345");

		this.shouldDeleteFromBothIndexes(JnEntityLoginPassword.ENTITY, record);
	}

	@Test
	public void loginSessionValidation() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JnEntityLoginSessionValidation.Fields.token, "TOKEN123")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				.put(JnJsonCommonsFields.userAgent, "junit");

		this.shouldDeleteFromBothIndexes(JnEntityLoginSessionValidation.ENTITY, record);
	}

	@Test
	public void deniedViewToCompany() {
		CcpJsonRepresentation record = this.withEmail()
				.put(VisEntityDeniedViewToCompany.Fields.domain, "jobsnow.com")
				.put(VisEntityDeniedViewToCompany.Fields.reasonType, "test")
				.put(VisEntityDeniedViewToCompany.Fields.reasonText, "test reason");

		this.shouldDeleteFromBothIndexes(VisEntityDeniedViewToCompany.ENTITY, record);
	}

	// ── entities that notify someone on save ─────────────────────────────────

	@Test
	public void loginToken() {
		this.shouldDeleteFromBothIndexes(JnEntityLoginToken.ENTITY, this.withEmail());
	}

	@Test
	public void loginTokenRequestResend() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		this.shouldDeleteFromBothIndexes(JnEntityLoginTokenRequestResend.ENTITY, record);
	}

	@Test
	public void loginTokenRequestUnlock() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		this.shouldDeleteFromBothIndexes(JnEntityLoginTokenRequestUnlock.ENTITY, record);
	}

	// ── entities with many required fields ───────────────────────────────────

	@Test
	public void position() {
		CcpJsonRepresentation record = this.withEmail()
				.put(VisEntityPosition.Fields.channel, Arrays.asList("email"))
				.put(VisEntityPosition.Fields.contactChannel, "selecao@jobsnow.com")
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11))
				.put(VisEntityPosition.Fields.description, "position used to test deleteAnyWhere")
				.put(VisEntityPosition.Fields.disponibility, 5)
				.put(VisEntityPosition.Fields.expireDate, this.daysAgo(30))
				.put(VisEntityPosition.Fields.frequency, "daily")
				.put(VisEntityPosition.Fields.requiredSkill, Arrays.asList("java"))
				.put(VisEntityPosition.Fields.seniority, "SR")
				.put(VisEntityPosition.Fields.sortFields, Arrays.asList("seniority"))
				.put(VisEntityPosition.Fields.title, "test position to exercise delete any where")
				.put(VisEntityPosition.Fields.showSalaryExpectation, true)
				.put(VisEntityPosition.Fields.minClt, 2_000)
				.put(VisEntityPosition.Fields.maxClt, 9_000);

		this.shouldDeleteFromBothIndexes(VisEntityPosition.ENTITY, record);
	}

	@Test
	public void resume() {
		CcpJsonRepresentation record = this.withEmail()
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, this.daysAgo(5 * 365))
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/onias")
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 6)
				.put(VisEntityResume.Fields.clt, 3_000);

		this.shouldDeleteFromBothIndexes(VisEntityResume.ENTITY, record);
	}

	// ── what is expected from every deleteAnyWhere ───────────────────────────

	/**
	 * Exercises both possible scenarios. In each one, the save is made on the side where the record
	 * should be born — and saving on one side deletes from the other, which is the twin entity
	 * contract, so setting up the second scenario does not need to undo the first.
	 */
	private void shouldDeleteFromBothIndexes(CcpEntity entityUnderTest, CcpJsonRepresentation record) {

		CcpEntity twin = entityUnderTest.getTwinEntity();

		String mainIndex = this.indexName(entityUnderTest);
		String twinIndex = this.indexName(twin);

		entityUnderTest.save(record);
		assertTrue("did not save in " + mainIndex, entityUnderTest.exists(record));

		boolean existedInMain = entityUnderTest.deleteAnyWhere(record);

		assertTrue("deleteAnyWhere denied that the record existed in " + mainIndex, existedInMain);
		assertFalse("record left over in " + mainIndex, entityUnderTest.exists(record));
		assertFalse("record left over in " + twinIndex, twin.exists(record));

		twin.save(record);
		assertTrue("did not save in " + twinIndex, twin.exists(record));

		boolean existedInTwin = entityUnderTest.deleteAnyWhere(record);

		assertTrue("deleteAnyWhere denied that the record existed in " + twinIndex, existedInTwin);
		assertFalse("record left over in " + twinIndex, twin.exists(record));
		assertFalse("record left over in " + mainIndex, entityUnderTest.exists(record));
	}

	private String indexName(CcpEntity entityUnderTest) {
		CcpEntityMetaData entityMetaData = entityUnderTest.getEntityMetaData();
		return entityMetaData.entityName;
	}

	private CcpJsonRepresentation withEmail() {
		CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, this.email);
		return record;
	}

	/**
	 * Timestamp of that many days ago. The time validations of these entities ask for a moment earlier
	 * than now, within a window measured in years.
	 *
	 * <p>It goes as text because the validation requires the value to be a long integer, and a number
	 * put in the json reaches it as a {@code Double} — in scientific notation, which it does not
	 * recognize.
	 */
	private String daysAgo(int daysBack) {
		long oneDayInMillis = 24L * 60L * 60L * 1000L;
		long now = System.currentTimeMillis();
		long instant = now - (daysBack * oneDayInMillis);
		String instantAsText = String.valueOf(instant);
		return instantAsText;
	}
}
