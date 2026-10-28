package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkScriptedUpdateFields;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityWriteEventSubscribers;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.js.entities.JsEntityLoginAnswers;
import com.vis.business.recruiter.VisBusinessPrepareRecruiter;
import com.vis.business.recruiter.VisBusinessSaveRecruiterFromLoginAnswers;

/**
 * Proves that the save of the sign-up answers ({@code js_login_answers}) reaches the recruiters ({@code vis_recruiter})
 * through {@code jn_write_event_subscribers}, with js and vis not importing each other: the goal {@code recruiting}
 * creates the recruiter with the source {@code login}, brings an inactive one back with the record it had and leaves an
 * active one as it is; the goal {@code jobs} creates nothing. The messaging is synchronous here, so the subscriber has
 * run when the save returns.
 */
public class OnSavingTheSignUpAnswers {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private final long now = System.currentTimeMillis();

	private final CcpEntity twin = VisEntityRecruiter.ENTITY.getTwinEntity();

	private List<CcpBulkItem> getSubscriptionSeeds() {
		return JnEntityWriteEventSubscribers.getSeeds(VisEntityRecruiter.ListenedEntities.js_login_answers, CcpEntityDecoratorOperationType.save, VisBusinessSaveRecruiterFromLoginAnswers.class);
	}

	/**
	 * The seed of vis, as {@code CcpCreateEntities} writes it: by bulk. Since 2026-10-24 the seed is a scripted update that
	 * adds the subscriber to the ones already there, so its json is the script, not the record (until 2026-10-27 this test
	 * saved that json as the record and every test failed on the validation of the entity).
	 */
	@Before
	public void subscribe() {
		List<CcpBulkItem> subscriptionSeeds = this.getSubscriptionSeeds();
		JnExecuteBulkOperation.INSTANCE.executeBulk(subscriptionSeeds, JnDeleteKeysFromCache.INSTANCE);
	}

	private String email(String prefix) {
		return prefix + this.now + "@recrutadora.com.br";
	}

	private void saveAnswers(String email, String goal) {
		CcpJsonRepresentation answers = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, email)
				.put(JsEntityLoginAnswers.Fields.channel, "linkedin")
				.put(JsEntityLoginAnswers.Fields.goal, goal);
		JsEntityLoginAnswers.ENTITY.save(answers);
	}

	private CcpJsonRepresentation recruiterKey(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, email);
	}

	private CcpJsonRepresentation recruiter(String email, String source) {
		CcpJsonRepresentation input = this.recruiterKey(email).put(VisEntityRecruiter.Fields.source, source);
		return VisBusinessPrepareRecruiter.INSTANCE.execute(input);
	}

	@Test
	public void theSeedSubscribesToTheSaveOfTheLoginAnswers() {
		CcpBulkItem seed = this.getSubscriptionSeeds().get(0);
		assertEquals(CcpBulkEntityOperationType.scriptedUpdate, seed.operation);
		CcpJsonRepresentation eventKey = JnEntityWriteEventSubscribers.getEventKey(JsEntityLoginAnswers.ENTITY, CcpEntityDecoratorOperationType.save);
		// the first seed of the event creates the record as the upsert has it
		CcpJsonRepresentation upsert = seed.json.getInnerJson(CcpBulkScriptedUpdateFields.upsert);
		assertEquals(eventKey.getAsString(JnEntityWriteEventSubscribers.Fields.entityName), upsert.getAsString(JnEntityWriteEventSubscribers.Fields.entityName));
		assertEquals("save", upsert.getAsString(JnEntityWriteEventSubscribers.Fields.writeOperation));
		assertEquals(List.of(VisBusinessSaveRecruiterFromLoginAnswers.class.getName()), upsert.getAsStringList(JnEntityWriteEventSubscribers.Fields.subscribers));
		// written by the bulk of subscribe(): the stored record has the subscriber, along with any other of the event
		CcpJsonRepresentation stored = JnEntityWriteEventSubscribers.ENTITY.getOneById(eventKey);
		List<String> storedSubscribers = stored.getAsStringList(JnEntityWriteEventSubscribers.Fields.subscribers);
		assertTrue(storedSubscribers.contains(VisBusinessSaveRecruiterFromLoginAnswers.class.getName()));
	}

	@Test
	public void theRecruitingGoalCreatesTheRecruiterWithTheSourceLogin() {
		String email = this.email("novo");
		this.saveAnswers(email, "recruiting");

		CcpJsonRepresentation recruiter = VisEntityRecruiter.ENTITY.getOneById(this.recruiterKey(email));
		assertEquals("login", recruiter.getAsString(VisEntityRecruiter.Fields.source));
		assertEquals("recrutadora.com.br", recruiter.getAsString(VisEntityRecruiter.Fields.domain));
	}

	@Test
	public void theRecruitingGoalBringsAnInactiveRecruiterBackWithTheRecordItHad() {
		String email = this.email("inativo");
		this.twin.save(this.recruiter(email, "telegram"));

		this.saveAnswers(email, "recruiting");

		CcpJsonRepresentation recruiter = VisEntityRecruiter.ENTITY.getOneById(this.recruiterKey(email));
		assertEquals("telegram", recruiter.getAsString(VisEntityRecruiter.Fields.source));
		assertFalse(this.twin.exists(this.recruiterKey(email)));
	}

	@Test
	public void theRecruitingGoalLeavesAnActiveRecruiterAsItIs() {
		String email = this.email("ativo");
		VisEntityRecruiter.ENTITY.save(this.recruiter(email, "legacy"));

		this.saveAnswers(email, "recruiting");

		CcpJsonRepresentation recruiter = VisEntityRecruiter.ENTITY.getOneById(this.recruiterKey(email));
		assertEquals("legacy", recruiter.getAsString(VisEntityRecruiter.Fields.source));
	}

	@Test
	public void changingTheGoalToRecruitingCreatesTheRecruiter() {
		String email = this.email("mudou");
		this.saveAnswers(email, "jobs");
		assertFalse(VisEntityRecruiter.ENTITY.exists(this.recruiterKey(email)));

		this.saveAnswers(email, "recruiting");

		CcpJsonRepresentation recruiter = VisEntityRecruiter.ENTITY.getOneById(this.recruiterKey(email));
		assertEquals("login", recruiter.getAsString(VisEntityRecruiter.Fields.source));
	}

	@Test
	public void theJobsGoalCreatesNoRecruiter() {
		String email = this.email("candidato");
		this.saveAnswers(email, "jobs");

		assertFalse(VisEntityRecruiter.ENTITY.exists(this.recruiterKey(email)));
		assertFalse(this.twin.exists(this.recruiterKey(email)));
	}
}
