package com.jn.rest.api.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.rest.api.commons.TestVariables;
import com.js.business.login.JsBusinessExecuteLogin;
import com.js.business.login.JsBusinessExecuteLogout;
import com.js.entities.JsEntityLoginSessionValidation;

/**
 * A token stops working when its session ends: after the logout, after a newer login of the same e-mail (only the
 * current session is accepted) and when it is used from another browser (the session key holds the user agent, so a
 * stolen token does not travel alone).
 */
public class AnEndedSessionNoLongerOpensAnything {

	private final TestVariables user = new TestVariables();

	@BeforeClass
	public static void theApisMustBeUp() {
		SecurityHttp.load();
		SecurityHttp.theApiMustBeUp(SecurityHttp.VIS);
	}

	private void login(String token) {
		CcpJsonRepresentation login = this.user.REQUEST_TO_LOGIN.put(CcpJsonCommonsFields.sessionToken, token);
		JsBusinessExecuteLogin.INSTANCE.execute(login);
	}

	private int readTheResumeWith(TestVariables client, String token) {
		String path = "resume/" + this.user.VALID_EMAIL + "/viewers/202611";
		HttpResponse<String> response = SecurityHttp.send(SecurityHttp.VIS, "GET", path, "{}", client, token);
		return response.statusCode();
	}

	@Test
	public void theTokenWorksWhileTheSessionIsOpen() {
		String token = SecurityHttp.token();
		this.login(token);

		int status = this.readTheResumeWith(this.user, token);

		assertTrue("the open session was refused with " + status, status < 300);
	}

	@Test
	public void theTokenStopsWorkingAfterTheLogout() {
		String token = SecurityHttp.token();
		this.login(token);
		CcpJsonRepresentation logout = this.user.REQUEST_TO_LOGIN.put(JsEntityLoginSessionValidation.Fields.token, token);
		JsBusinessExecuteLogout.INSTANCE.execute(logout);

		int status = this.readTheResumeWith(this.user, token);

		assertEquals(401, status);
	}

	@Test
	public void theOldTokenStopsWorkingAfterANewLogin() {
		String oldToken = SecurityHttp.token();
		this.login(oldToken);
		String newToken = SecurityHttp.token();
		this.login(newToken);

		int status = this.readTheResumeWith(this.user, oldToken);

		assertEquals(401, status);
	}

	@Test
	public void theTokenDoesNotWorkFromAnotherBrowser() {
		String token = SecurityHttp.token();
		this.login(token);
		TestVariables otherBrowser = new TestVariables(this.user.VALID_EMAIL);
		otherBrowser.REQUEST_TO_LOGIN = otherBrowser.REQUEST_TO_LOGIN.put(JnJsonCommonsFields.userAgent, "Mozilla/5.0 (X11; Linux x86_64) Stolen/1.0");

		int status = this.readTheResumeWith(otherBrowser, token);

		assertEquals(401, status);
	}
}
