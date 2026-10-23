package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
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
import com.jn.entities.JnEntityWriteEventSubscribers;
import com.jn.json.fields.validation.JnJsonCommonsFields;
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

	/** The seed of vis, as {@code CcpCreateEntities} writes it. */
	@Before
	public void subscribe() {
		CcpBulkItem subscription = this.getSubscriptionSeeds().get(0);
		JnEntityWriteEventSubscribers.ENTITY.save(subscription.json);
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
		CcpJsonRepresentation seed = this.getSubscriptionSeeds().get(0).json;
		CcpJsonRepresentation eventKey = JnEntityWriteEventSubscribers.getEventKey(JsEntityLoginAnswers.ENTITY, CcpEntityDecoratorOperationType.save);
		assertEquals(eventKey.getAsString(JnEntityWriteEventSubscribers.Fields.entityName), seed.getAsString(JnEntityWriteEventSubscribers.Fields.entityName));
		assertEquals("save", seed.getAsString(JnEntityWriteEventSubscribers.Fields.writeOperation));
		List<String> subscribers = seed.getAsStringList(JnEntityWriteEventSubscribers.Fields.subscribers);
		assertEquals(List.of(VisBusinessSaveRecruiterFromLoginAnswers.class.getName()), subscribers);
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
