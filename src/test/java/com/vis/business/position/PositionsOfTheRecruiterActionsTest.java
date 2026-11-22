package com.vis.business.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentEmailSender;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.business.grouper.JnBusinessGetGroup;
import com.jn.entities.JnEntityGrouper;
import com.jn.entities.JnEntityLoginEmail;
import com.js.services.JsServiceGrouper;
import com.vis.entities.VisEntityPosition;
import com.vis.services.VisServicePosition;

/**
 * 2026-11-21: the actions of the screen "Meu Recrutamento - Vagas" over the positions of the logged-in user (the e-mail
 * of the session is the recruiter), proved through the list the screen reads: the group of the e-mail of the user,
 * through the endpoint of the groups of js.
 */
public class PositionsOfTheRecruiterActionsTest {

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

	private final long now = System.currentTimeMillis();

	private final String userEmail = "positionActions" + this.now + "@empresa.com.br";

	private final String title = "position of the actions " + this.now;

	private static final String DESCRIPTION = "the description of the position of the actions of the screen, long enough to pass the minimum of one hundred characters";

	private static final String OTHER_DESCRIPTION = "another description of the position of the actions of the screen, also long enough to pass the minimum of one hundred characters";

	private static String daysAhead(int days) {
		long instant = System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L;
		return String.valueOf(instant);
	}

	private static CcpJsonRepresentation range(String min, String max) {
		return CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("min"), min).put(new CcpFieldName("max"), max);
	}

	/** What the screen sends: the e-mail of the session and the fields of the position, no recruiter. */
	private CcpJsonRepresentation input(String description) {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisPositionKeyFields.email, this.userEmail)
				.put(VisEntityPosition.Fields.title, this.title)
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.seniority, "junior")
				.put(VisEntityPosition.Fields.clt, new CcpJsonRepresentation("{\"min\":3000,\"max\":5000}"))
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11))
				.put(VisEntityPosition.Fields.disponibility, 0)
				.put(VisEntityPosition.Fields.temporallyJobTime, 0)
				.put(VisEntityPosition.Fields.skill, Arrays.asList(java))
				.put(VisEntityPosition.Fields.expiration, range(daysAhead(0), daysAhead(30)))
				.put(VisEntityPosition.Fields.description, description)
				.put(VisEntityPosition.Fields.sort, Arrays.asList(new CcpJsonRepresentation("{\"type\":\"asc\",\"field\":[\"clt\"]}")))
				.put(VisEntityPosition.Fields.contact, Arrays.asList(new CcpJsonRepresentation("{\"type\":\"whatsapp\",\"value\":\"+5511999998888\"}")));
		// as the request body arrives: parsed from the text
		return new CcpJsonRepresentation(input.asUgglyJson());
	}

	private CcpJsonRepresentation key() {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisPositionKeyFields.email, this.userEmail)
				.put(VisPositionKeyFields.title, this.title);
	}

	@After
	public void deleteThePosition() {
		CcpJsonRepresentation position = this.input(DESCRIPTION).removeFields(VisPositionKeyFields.email).put(VisEntityPosition.Fields.recruiter, this.userEmail);
		VisEntityPosition.ENTITY.deleteAnyWhere(position);
	}

	private List<CcpJsonRepresentation> positionsOnThe(String side) {
		String emailHash = new CcpStringDecorator(this.userEmail).hash().asString(CcpHashAlgorithm.SHA1);
		String parentId = JnEntityLoginEmail.ENTITY.calculateId(CcpOtherConstants.EMPTY_JSON.put(JnEntityLoginEmail.Fields.email, emailHash));
		CcpJsonRepresentation asked = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginEmail.Fields.email, this.userEmail)
				.put(JnBusinessGetGroup.Fields.parentName, "jn_login_email")
				.put(JnBusinessGetGroup.Fields.childName, "vis_position")
				.put(JnBusinessGetGroup.Fields.parentId, parentId)
				.put(new CcpFieldName("side"), side);
		try {
			CcpJsonRepresentation group = JsServiceGrouper.GetGroup.execute(asked);
			return group.getAsStringList(JnEntityGrouper.Fields.child).stream().map(CcpJsonRepresentation::new).toList();
		} catch (CcpErrorFlowDisturb notFound) {
			return List.of();
		}
	}

	private CcpJsonRepresentation theOnlyActivePosition() {
		List<CcpJsonRepresentation> positions = this.positionsOnThe("mainEntity");
		assertEquals(1, positions.size());
		return positions.get(0);
	}

	private static long maxOf(CcpJsonRepresentation position) {
		CcpJsonRepresentation expiration = position.getInnerJson(VisEntityPosition.Fields.expiration);
		return expiration.getAsDoubleNumber(new CcpFieldName("max")).longValue();
	}

	private static void assertStatus(CcpProcessStatus expected, Runnable action) {
		try {
			action.run();
			fail("expected " + expected);
		} catch (CcpErrorFlowDisturb e) {
			assertEquals(expected.asNumber(), e.status.asNumber());
		}
	}

	@Test
	public void theUserCreatesEditsInactivatesReactivatesAndDeletesTheirPosition() {
		VisServicePosition.CreatePosition.execute(this.input(DESCRIPTION));
		CcpJsonRepresentation created = this.theOnlyActivePosition();
		assertEquals(this.title, created.getAsString(VisEntityPosition.Fields.title));
		assertEquals("the default value", "daily", created.getAsString(VisEntityPosition.Fields.frequencyType));
		assertStatus(CcpProcessStatusDefault.CONFLICT, () -> VisServicePosition.CreatePosition.execute(this.input(DESCRIPTION)));

		VisServicePosition.EditPosition.execute(this.input(OTHER_DESCRIPTION));
		assertEquals(OTHER_DESCRIPTION, this.theOnlyActivePosition().getAsString(VisEntityPosition.Fields.description));

		CcpJsonRepresentation inactivated = VisServicePosition.InactivatePosition.execute(this.key());
		long now = System.currentTimeMillis();
		assertTrue("the answer brings the new expiration", maxOf(inactivated) < now);
		CcpJsonRepresentation expired = this.theOnlyActivePosition();
		assertTrue("expired one minute before now", maxOf(expired) < now);
		assertEquals("the edition was kept", OTHER_DESCRIPTION, expired.getAsString(VisEntityPosition.Fields.description));

		String newMax = daysAhead(10);
		VisServicePosition.ReactivatePosition.execute(this.key().put(VisBusinessReactivatePosition.Fields.max, newMax));
		assertEquals(Long.parseLong(newMax), maxOf(this.theOnlyActivePosition()));

		VisServicePosition.DeletePosition.execute(this.key());
		assertEquals("deleted: out of both tabs", 0, this.positionsOnThe("mainEntity").size());
		assertEquals("in the twin groups", 1, this.positionsOnThe("twinEntity").size());
		assertStatus(CcpProcessStatusDefault.NOT_FOUND, () -> VisServicePosition.EditPosition.execute(this.input(DESCRIPTION)));
		assertStatus(CcpProcessStatusDefault.NOT_FOUND, () -> VisServicePosition.InactivatePosition.execute(this.key()));
	}

	@Test
	public void aNewPositionDoesNotStartInThePastAndTheReactivationDoesNotEndInThePast() {
		CcpJsonRepresentation startedInThePast = this.input(DESCRIPTION).put(VisEntityPosition.Fields.expiration, range(daysAhead(-2), daysAhead(30)));
		try {
			VisServicePosition.CreatePosition.execute(startedInThePast);
			fail("a new position that started in the past was accepted");
		} catch (CcpJsonValidationError expected) {
		}
		VisServicePosition.CreatePosition.execute(this.input(DESCRIPTION));
		try {
			VisServicePosition.ReactivatePosition.execute(this.key().put(VisBusinessReactivatePosition.Fields.max, daysAhead(-2)));
			fail("a reactivation that ends in the past was accepted");
		} catch (CcpJsonValidationError expected) {
		}
	}

	@Test
	public void aPositionWithoutSalaryRangeIsRefusedBeforeTheQueue() {
		CcpJsonRepresentation noSalary = this.input(DESCRIPTION).removeFields(VisEntityPosition.Fields.clt);
		try {
			VisServicePosition.CreatePosition.execute(noSalary);
			fail("a position with no salary range was accepted");
		} catch (CcpJsonValidationError expected) {
		}
	}
}
