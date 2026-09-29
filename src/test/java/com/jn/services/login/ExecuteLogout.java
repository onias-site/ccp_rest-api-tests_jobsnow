package com.jn.services.login;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusExecuteLogout;

public class ExecuteLogout extends JnServiceLoginTestTemplate {

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
		JnEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation withToken = testVariables.REQUEST_TO_LOGIN
				.put(JnEntityLoginSessionValidation.Fields.token, "12345678");
		JnEntityLoginSessionValidation.ENTITY.save(withToken);
		this.execute(withToken, JnProcessStatusExecuteLogout.expectedStatus);
	}
}
