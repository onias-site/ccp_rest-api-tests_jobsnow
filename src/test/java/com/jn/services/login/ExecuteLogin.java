package com.jn.services.login;

import org.junit.Test;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class ExecuteLogin extends JnServiceLoginTestTemplate {

	@Test(expected = CcpJsonValidationError.class)
	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.lockedToken);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.missingSavingEmail);
	}

	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		com.jn.entities.JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.missingSavePassword);
	}

	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.lockedPassword);
	}

	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.loginConflict);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		this.expectedFlow(testVariables);
	}

	@Test
	public void lockPassword() {
		TestVariables testVariables = new TestVariables();
		new SavePassword().expectedFlow(testVariables);
		JnEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JnProcessStatusExecuteLogin.wrongPassword);
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JnProcessStatusExecuteLogin.wrongPassword);
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JnProcessStatusExecuteLogin.passwordLockedRecently);
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JnProcessStatusExecuteLogin.lockedPassword);
		new ExistsLoginEmail().execute(testVariables, JnProcessStatusExistsLoginEmail.lockedPassword);
	}

	@Test
	public void missPasswordThenGetItRight() {
		TestVariables testVariables = new TestVariables();
		new SavePassword().expectedFlow(testVariables);
		JnEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		for (int k = 1; k < 3; k++) {
			executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JnProcessStatusExecuteLogin.wrongPassword);
		}
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.expectedStatus);
	}

	@Test
	public void recentlyLockedPassword() {
		TestVariables testVariables = new TestVariables();
		SavePassword savePassword = new SavePassword();
		savePassword.expectedFlow(testVariables);
		JnEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		for (int k = 1; k <= 2; k++) {
			executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JnProcessStatusExecuteLogin.wrongPassword);
		}
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JnProcessStatusExecuteLogin.passwordLockedRecently);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.lockedPassword);
	}

	public CcpJsonRepresentation expectedFlow(TestVariables testVariables) {
		SavePassword savePassword = new SavePassword();
		savePassword.expectedFlow(testVariables);
		JnEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation loginResponse = this.executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.expectedStatus);
		return loginResponse;
	}

	private CcpJsonRepresentation executeLogin(TestVariables testVariables, String password, com.ccp.process.CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation json = testVariables.REQUEST_TO_LOGIN.put(JnJsonCommonsFields.password, password);
		CcpJsonRepresentation serviceResponse = this.execute(json, expectedStatus);
		return serviceResponse;
	}
}
