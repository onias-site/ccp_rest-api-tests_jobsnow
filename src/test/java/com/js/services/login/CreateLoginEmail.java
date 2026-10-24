package com.js.services.login;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusCreateLoginEmail;

public class CreateLoginEmail extends JsServiceLoginTestTemplate {

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
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusCreateLoginEmail.lockedToken);
	}

	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusCreateLoginEmail.lockedPassword);
	}

	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusCreateLoginEmail.loginConflict);
	}

	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusCreateLoginEmail.missingSavePassword);
	}

	@Test
	public void missingPreRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusCreateLoginEmail.missingSaveAnswers);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusCreateLoginEmail.expectedStatus);
	}
}
