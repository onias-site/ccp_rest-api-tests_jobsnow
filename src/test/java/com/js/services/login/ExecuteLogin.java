package com.js.services.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.js.services.JsServiceLogin;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.js.entities.JsEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusExecuteLogin;
import com.js.status.login.JsProcessStatusExistsLoginEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class ExecuteLogin extends JsServiceLoginTestTemplate {

	@Test(expected = CcpJsonValidationError.class)
	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.lockedToken);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.missingSavingEmail);
	}

	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		com.js.entities.JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.missingSavePassword);
	}

	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.lockedPassword);
	}

	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.loginConflict);
	}

	/**
	 * A refused login does not answer the session token generated before the checks: it never became a session. Until
	 * 2026-10-14 every 409 answered a new one.
	 */
	@Test
	public void refusedLoginDoesNotAnswerASessionToken() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, testVariables.VALID_EMAIL)
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1");
		try {
			JsServiceLogin.ExecuteLogin.execute(request);
			fail("the login of an open session was accepted");
		} catch (CcpErrorFlowDisturb refused) {
			assertEquals(JsProcessStatusExecuteLogin.loginConflict, refused.status);
			assertFalse(refused.json.containsAllFields(CcpJsonCommonsFields.sessionToken));
			assertFalse(Arrays.asList(refused.fields).contains(CcpJsonCommonsFields.sessionToken));
		}
	}

	/**
	 * The session opened by a password save is the current login of the e-mail: a second login is a conflict. Until
	 * 2026-10-11 the password save deleted the record of the current login right after creating it, and the second login
	 * got in.
	 */
	@Test
	public void sessionOpenedByAPasswordSaveIsTheCurrentLogin() {
		TestVariables testVariables = new TestVariables();
		new SavePassword().expectedFlow(testVariables);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.loginConflict);
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
		JsEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JsProcessStatusExecuteLogin.wrongPassword);
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JsProcessStatusExecuteLogin.wrongPassword);
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JsProcessStatusExecuteLogin.passwordLockedRecently);
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JsProcessStatusExecuteLogin.lockedPassword);
		new ExistsLoginEmail().execute(testVariables, JsProcessStatusExistsLoginEmail.lockedPassword);
	}

	@Test
	public void missPasswordThenGetItRight() {
		TestVariables testVariables = new TestVariables();
		new SavePassword().expectedFlow(testVariables);
		JsEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		for (int k = 1; k < 3; k++) {
			executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JsProcessStatusExecuteLogin.wrongPassword);
		}
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.expectedStatus);
	}

	@Test
	public void recentlyLockedPassword() {
		TestVariables testVariables = new TestVariables();
		SavePassword savePassword = new SavePassword();
		savePassword.expectedFlow(testVariables);
		JsEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		for (int k = 1; k <= 2; k++) {
			executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JsProcessStatusExecuteLogin.wrongPassword);
		}
		executeLogin(testVariables, TestVariables.WRONG_PASSWORD, JsProcessStatusExecuteLogin.passwordLockedRecently);
		executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.lockedPassword);
	}

	public CcpJsonRepresentation expectedFlow(TestVariables testVariables) {
		SavePassword savePassword = new SavePassword();
		savePassword.expectedFlow(testVariables);
		JsEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation loginResponse = this.executeLogin(testVariables, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.expectedStatus);
		return loginResponse;
	}

	private CcpJsonRepresentation executeLogin(TestVariables testVariables, String password, com.ccp.process.CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation json = testVariables.REQUEST_TO_LOGIN.put(JnJsonCommonsFields.password, password);
		CcpJsonRepresentation serviceResponse = this.execute(json, expectedStatus);
		return serviceResponse;
	}
}
