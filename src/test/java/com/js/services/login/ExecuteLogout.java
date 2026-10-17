package com.js.services.login;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusExecuteLogout;

public class ExecuteLogout extends JsServiceLoginTestTemplate {

	@Test(expected = CcpJsonValidationError.class)
	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		this.execute(testVariables.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void userNotLoggedIn() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables.REQUEST_TO_LOGIN, JnProcessStatusExecuteLogout.missingLogin);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables("onias85@gmail.com");
		this.expectedFlow(testVariables);
	}

	public void expectedFlow(TestVariables testVariables) {
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation withToken = testVariables.saveCurrentSession("12345678");
		this.execute(withToken, JnProcessStatusExecuteLogout.expectedStatus);
	}

	/**
	 * The logout of a session replaced by a newer one of the same e-mail answers "not logged in" and keeps the current
	 * session. Until 2026-10-11 it went through and deleted the record of the current login.
	 */
	@Test
	public void replacedSessionDoesNotEndTheCurrentOne() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation replacedSession = testVariables.saveCurrentSession("11111111");
		CcpJsonRepresentation currentSession = testVariables.saveCurrentSession("22222222");

		this.execute(replacedSession, JnProcessStatusExecuteLogout.missingLogin);

		boolean currentLoginStillExists = JnEntityLoginSessionConflict.ENTITY.exists(currentSession);
		assertTrue(currentLoginStillExists);
		this.execute(currentSession, JnProcessStatusExecuteLogout.expectedStatus);
	}
}
