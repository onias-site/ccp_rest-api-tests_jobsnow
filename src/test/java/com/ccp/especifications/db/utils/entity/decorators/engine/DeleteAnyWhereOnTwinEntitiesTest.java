package com.ccp.especifications.db.utils.entity.decorators.engine;

import com.vis.business.position.VisPositionOfTheRecruiter;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
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
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.js.entities.JsEntityLoginTokenRequestUnlock;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
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
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> SilentEmailSender.INSTANCE;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
	}

	private final String email = "twin" + System.currentTimeMillis() + "@jobsnow.com";

	// ── entities without side effects on save ────────────────────────────────

	@Test
	public void loginPassword() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JnJsonCommonsFields.password, "Senha#12345");

		this.shouldDeleteFromBothIndexes(JsEntityLoginPassword.ENTITY, record);
	}

	@Test
	public void loginSessionValidation() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JsEntityLoginSessionValidation.Fields.token, "TOKEN123")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				.put(JnJsonCommonsFields.userAgent, "junit");

		this.shouldDeleteFromBothIndexes(JsEntityLoginSessionValidation.ENTITY, record);
	}

	// ── entities that notify someone on save ─────────────────────────────────

	@Test
	public void loginToken() {
		this.shouldDeleteFromBothIndexes(JsEntityLoginToken.ENTITY, this.withEmail());
	}

	@Test
	public void loginTokenRequestResend() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		this.shouldDeleteFromBothIndexes(JsEntityLoginTokenRequestResend.ENTITY, record);
	}

	@Test
	public void loginTokenRequestUnlock() {
		CcpJsonRepresentation record = this.withEmail()
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		this.shouldDeleteFromBothIndexes(JsEntityLoginTokenRequestUnlock.ENTITY, record);
	}

	// ── entities with many required fields ───────────────────────────────────

	@Test
	public void position() {
		CcpJsonRepresentation salaryRange = new CcpJsonRepresentation("{\"min\":2000,\"max\":9000}");
		CcpJsonRepresentation expiration = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("min"), this.daysAhead(1)).put(new CcpFieldName("max"), this.daysAhead(30));
		CcpJsonRepresentation sort = new CcpJsonRepresentation("{\"type\":\"desc\",\"field\":[\"clt\"]}");
		CcpJsonRepresentation contact = new CcpJsonRepresentation("{\"type\":\"email\",\"value\":\"selecao@jobsnow.com\"}");
		String description = "position used to test deleteAnyWhere, with a description long enough to pass the minimum of one hundred characters";
		CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.recruiter, this.email)
				.put(VisEntityPosition.Fields.clt, salaryRange)
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11))
				.put(VisEntityPosition.Fields.description, description)
				.put(VisEntityPosition.Fields.disponibility, 5)
				.put(VisEntityPosition.Fields.expiration, expiration)
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.seniority, List.of("senior"))
				.put(VisEntityPosition.Fields.sort, Arrays.asList(sort))
				.put(VisEntityPosition.Fields.contact, Arrays.asList(contact))
				.put(VisEntityPosition.Fields.temporallyJobTime, 0)
				.put(VisEntityPosition.Fields.yearMonth, VisPositionOfTheRecruiter.getCurrentYearMonth())
				.put(VisEntityPosition.Fields.title, "test position to exercise delete any where");

		this.shouldDeleteFromBothIndexes(VisEntityPosition.ENTITY, record);
	}

	@Test
	public void resume() {
		CcpJsonRepresentation record = this.withEmail()
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 5)
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
	 * Timestamp of that many days ahead. The expiration of the position asks for moments from the current
	 * date on.
	 *
	 * <p>It goes as text because the validation requires the value to be a long integer, and a number
	 * put in the json reaches it as a {@code Double} — in scientific notation, which it does not
	 * recognize.
	 */
	private String daysAhead(int daysAhead) {
		long oneDayInMillis = 24L * 60L * 60L * 1000L;
		long now = System.currentTimeMillis();
		long instant = now + (daysAhead * oneDayInMillis);
		String instantAsText = String.valueOf(instant);
		return instantAsText;
	}
}
