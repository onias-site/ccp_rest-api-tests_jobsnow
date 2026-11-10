package com.jn.rest.api.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.entities.JnEntityGrouper;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.rest.api.commons.TestVariables;
import com.jn.utils.JnGroupers;
import com.js.business.login.JsBusinessExecuteLogin;
import com.js.entities.JsEntityLoginSessionValidation;

/**
 * A logged-in candidate must never read what belongs to another one, even knowing the other's e-mail (the e-mails of
 * the platform are no secret: the recruiters receive them). Two candidates, each with its own session: the attacker uses
 * its own valid session and puts the victim's e-mail, or the id calculated from it, in the request.
 * <p>
 * The group of the login history ({@code jn_login_email} / {@code js_login_session_validation}) and the groups of the
 * tickets keep IP, browser and the texts the user wrote; their {@code parentId} is calculated only from the e-mail
 * ({@code sha1} of the {@code sha1}), as the front end does in {@code accessesReconciliation.ts}.
 */
public class ACandidateCannotReadTheDataOfAnother {

	private static final String ATTACKER_TOKEN = SecurityHttp.token();

	private static final String VICTIM_TOKEN = SecurityHttp.token();

	private final TestVariables attacker = new TestVariables();

	private final TestVariables victim = new TestVariables();

	@BeforeClass
	public static void theApisMustBeUp() {
		SecurityHttp.load();
		SecurityHttp.theApiMustBeUp(SecurityHttp.JS);
		SecurityHttp.theApiMustBeUp(SecurityHttp.VIS);
	}

	@Before
	public void bothCandidatesLogIn() {
		CcpJsonRepresentation victimLogin = this.victim.REQUEST_TO_LOGIN.put(CcpJsonCommonsFields.sessionToken, VICTIM_TOKEN);
		JsBusinessExecuteLogin.INSTANCE.execute(victimLogin);
		JnEntityLoginEmail.ENTITY.save(this.attacker.REQUEST_TO_LOGIN);
		this.attacker.saveCurrentSession(ATTACKER_TOKEN);
	}

	@After
	public void deleteTheHistoryOfTheVictim() {
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(this.parentName(), this.childName(), this.parentIdOf(this.victim));
		JnEntityGrouper.ENTITY.deleteAnyWhere(groupKey);
	}

	private String parentName() {
		return JnEntityLoginEmail.ENTITY.getEntityMetaData().entityName;
	}

	private String childName() {
		return JsEntityLoginSessionValidation.ENTITY.getEntityMetaData().entityName;
	}

	private String parentIdOf(TestVariables user) {
		CcpStringDecorator email = new CcpStringDecorator(user.VALID_EMAIL);
		String emailHash = email.hash().asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(JnEntityLoginEmail.Fields.email, emailHash);
		String parentId = JnEntityLoginEmail.ENTITY.calculateId(parentJson);
		return parentId;
	}

	private HttpResponse<String> attackerReadsTheGroupOf(TestVariables owner, String suffix) {
		String parentId = this.parentIdOf(owner);
		String path = "grouper/" + this.attacker.VALID_EMAIL + "/" + this.parentName() + "/" + this.childName() + "/" + parentId + suffix;
		HttpResponse<String> response = SecurityHttp.send(SecurityHttp.JS, "GET", path, "{}", this.attacker, ATTACKER_TOKEN);
		return response;
	}

	/** The control: the victim reads its own history with its own session, so the history the attacker looks for exists. */
	@Test
	public void theVictimReadsItsOwnLoginHistory() {
		String path = "grouper/" + this.victim.VALID_EMAIL + "/" + this.parentName() + "/" + this.childName() + "/" + this.parentIdOf(this.victim) + "/bothSides";
		HttpResponse<String> response = SecurityHttp.send(SecurityHttp.JS, "GET", path, "{}", this.victim, VICTIM_TOKEN);
		String body = response.body();

		assertTrue(body, response.statusCode() == 200);
		assertTrue("the history of the victim has its login", body.contains("loginDate"));
	}

	@Test
	public void theAttackerDoesNotReadTheLoginHistoryOfTheVictim() {
		HttpResponse<String> response = this.attackerReadsTheGroupOf(this.victim, "/bothSides");
		String body = response.body();

		assertFalse("status " + response.statusCode() + ": the attacker read the IPs and browsers of the victim: " + body, body.contains("loginDate"));
	}

	@Test
	public void theAttackerDoesNotReadTheActiveSideOfTheVictimsGroup() {
		HttpResponse<String> response = this.attackerReadsTheGroupOf(this.victim, "");
		String body = response.body();

		assertFalse("status " + response.statusCode() + ": the attacker read the group of the victim: " + body, body.contains("loginDate"));
	}

	@Test
	public void theSessionOfOneCandidateDoesNotOpenTheResumeRoutesOfAnother() {
		String path = "resume/" + this.victim.VALID_EMAIL + "/viewers/202611";
		HttpResponse<String> response = SecurityHttp.send(SecurityHttp.VIS, "GET", path, "{}", this.attacker, ATTACKER_TOKEN);
		int status = response.statusCode();

		assertTrue("status " + status + ": " + response.body(), status == 401);
	}

	@Test
	public void theSessionOfOneCandidateDoesNotOpenTheLoginRoutesOfAnother() {
		String path = "login/" + this.victim.VALID_EMAIL + "/" + ATTACKER_TOKEN;
		HttpResponse<String> response = SecurityHttp.send(SecurityHttp.JS, "GET", path, "{}", this.attacker, ATTACKER_TOKEN);
		int status = response.statusCode();

		assertTrue("status " + status + ": " + response.body(), status == 401 || status == 404);
	}

	/** The logout carries the token in the path: with its own token and the victim's e-mail, the attacker ends nothing of the victim. */
	@Test
	public void theTokenOfOneCandidateDoesNotLogTheOtherOut() {
		String path = "login/" + this.victim.VALID_EMAIL + "/" + ATTACKER_TOKEN;
		HttpResponse<String> response = SecurityHttp.send(SecurityHttp.JS, "DELETE", path, "{}", this.attacker, ATTACKER_TOKEN);
		int status = response.statusCode();
		String victimPath = "resume/" + this.victim.VALID_EMAIL + "/viewers/202611";
		HttpResponse<String> victimStillIn = SecurityHttp.send(SecurityHttp.VIS, "GET", victimPath, "{}", this.victim, VICTIM_TOKEN);

		assertTrue("the logout by the attacker answered " + status + " and the victim's session answers " + victimStillIn.statusCode(),
				victimStillIn.statusCode() < 300);
	}
}
