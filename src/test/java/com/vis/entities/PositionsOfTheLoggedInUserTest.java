package com.vis.entities;

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
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.business.grouper.JnBusinessGetGroup;
import com.jn.entities.JnEntityGrouper;
import com.jn.entities.JnEntityLoginEmail;
import com.js.services.JsServiceGrouper;

/**
 * 2026-11-21: the logged-in user sees the positions they registered. The {@code recruiter} of the position is the e-mail
 * of the user, so each position is a child of the group of that e-mail ({@code jn_login_email}) in {@code jn_grouper},
 * as a JSON text with what the user filled in and the id; a new save replaces the child, the deactivation moves it to
 * {@code jn_inactive_grouper} and the deleteAnyWhere takes it out of both. The read goes through the endpoint of the
 * groups of js ({@code JsServiceGrouper}), which serves only the groups of the e-mail of the session.
 */
public class PositionsOfTheLoggedInUserTest {

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

	private final String userEmail = "myPositions" + this.now + "@empresa.com.br";

	private final String title = "my registered position " + this.now;

	private static String daysAhead(int days) {
		long instant = System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L;
		return String.valueOf(instant);
	}

	private CcpJsonRepresentation position(String description) {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		CcpJsonRepresentation expiration = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("min"), daysAhead(1)).put(new CcpFieldName("max"), daysAhead(30));
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.recruiter, this.userEmail)
				.put(VisEntityPosition.Fields.title, this.title)
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.seniority, List.of("pleno"))
				.put(VisEntityPosition.Fields.pj, new CcpJsonRepresentation("{\"min\":9000,\"max\":13000}"))
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11, 21))
				.put(VisEntityPosition.Fields.disponibility, 0)
				.put(VisEntityPosition.Fields.temporallyJobTime, 0)
				.put(VisEntityPosition.Fields.skill, Arrays.asList(java))
				.put(VisEntityPosition.Fields.expiration, expiration)
				.put(VisEntityPosition.Fields.description, description)
				.put(VisEntityPosition.Fields.sort, Arrays.asList(new CcpJsonRepresentation("{\"type\":\"desc\",\"field\":[\"pj\"]}")))
				.put(VisEntityPosition.Fields.contact, Arrays.asList(new CcpJsonRepresentation("{\"type\":\"link\",\"value\":\"https://empresa.com.br/vagas\"}")));
	}

	private static final String FIRST_DESCRIPTION = "the first description of the position of the logged-in user, long enough to pass the minimum of one hundred characters";

	private static final String SECOND_DESCRIPTION = "the second description of the position of the logged-in user, also long enough to pass the minimum of one hundred characters";

	@After
	public void deleteThePosition() {
		VisEntityPosition.ENTITY.deleteAnyWhere(this.position(FIRST_DESCRIPTION));
	}

	/** The id of the group of the session owner, calculated as the endpoint of the groups does (the SHA-1 of the e-mail). */
	private String parentIdOf(String email) {
		String emailHash = new CcpStringDecorator(email).hash().asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(JnEntityLoginEmail.Fields.email, emailHash);
		return JnEntityLoginEmail.ENTITY.calculateId(parentJson);
	}

	private CcpJsonRepresentation askFor(String sessionEmail, String side) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginEmail.Fields.email, sessionEmail)
				.put(JnBusinessGetGroup.Fields.parentName, "jn_login_email")
				.put(JnBusinessGetGroup.Fields.childName, "vis_position")
				.put(JnBusinessGetGroup.Fields.parentId, this.parentIdOf(this.userEmail))
				.put(new CcpFieldName("side"), side);
	}

	/** The children of the group of the user on one side, through the endpoint of the groups; none when it answers 404. */
	private List<CcpJsonRepresentation> positionsOnThe(String side) {
		try {
			CcpJsonRepresentation group = JsServiceGrouper.GetGroup.execute(this.askFor(this.userEmail, side));
			List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
			return children.stream().map(CcpJsonRepresentation::new).toList();
		} catch (CcpErrorFlowDisturb notFound) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, notFound.status);
			return List.of();
		}
	}

	@Test
	public void theUserSeesThePositionsTheyRegistered() {
		VisEntityPosition.ENTITY.save(this.position(FIRST_DESCRIPTION));

		List<CcpJsonRepresentation> active = this.positionsOnThe("mainEntity");
		assertEquals("the position in the group of the user", 1, active.size());
		CcpJsonRepresentation child = active.get(0);
		assertEquals(this.title, child.getAsString(VisEntityPosition.Fields.title));
		assertEquals(FIRST_DESCRIPTION, child.getAsString(VisEntityPosition.Fields.description));
		assertEquals(List.of("pleno"), child.getAsStringList(VisEntityPosition.Fields.seniority));
		assertEquals("the default value", "daily", child.getAsString(VisEntityPosition.Fields.frequencyType));
		assertTrue("the id of the position", child.containsAllFields(new CcpFieldName("id")));
		assertTrue("no hash of the searches", false == child.containsAllFields(VisEntityPosition.Fields.hash));
		assertTrue("no e-mail of the user", false == child.containsAllFields(VisEntityPosition.Fields.recruiter));

		VisEntityPosition.ENTITY.save(this.position(SECOND_DESCRIPTION));
		active = this.positionsOnThe("mainEntity");
		assertEquals("a new save replaces the child", 1, active.size());
		assertEquals(SECOND_DESCRIPTION, active.get(0).getAsString(VisEntityPosition.Fields.description));

		VisEntityPosition.ENTITY.delete(this.position(SECOND_DESCRIPTION));
		assertEquals("the inactive position leaves the active ones", 0, this.positionsOnThe("mainEntity").size());
		assertEquals("and goes to the inactive ones", 1, this.positionsOnThe("twinEntity").size());

		this.deleteThePosition();
		assertEquals(0, this.positionsOnThe("mainEntity").size());
		assertEquals(0, this.positionsOnThe("twinEntity").size());
	}

	@Test
	public void anotherUserDoesNotSeeThePositions() {
		VisEntityPosition.ENTITY.save(this.position(FIRST_DESCRIPTION));
		try {
			JsServiceGrouper.GetGroup.execute(this.askFor("another" + this.now + "@empresa.com.br", "mainEntity"));
			fail("the group of another user must answer 404");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
	}
}
