package com.js.services.login;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusExistsLoginEmail;

public class ExistsLoginEmail extends JsServiceLoginTestTemplate {

	@Test(expected = CcpJsonValidationError.class)
	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		this.execute(testVariables.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.lockedToken);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.missingEmail);
	}

	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.lockedPassword);
	}

	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.loginConflict);
	}

	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.missingPassword);
	}

	@Test
	public void missingPreRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.missingAnswers);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.expectedStatus);
	}

	public void execute(TestVariables testVariables, CcpProcessStatus expectedStatus) {
		this.execute(testVariables.REQUEST_TO_LOGIN, expectedStatus);
	}
}
