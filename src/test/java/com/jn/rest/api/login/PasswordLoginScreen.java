package com.jn.rest.api.login;

import java.util.function.Function;
import org.junit.Test;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.process.CcpProcessStatus;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginToken;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusExecuteLogin;
import com.js.status.login.JsProcessStatusExistsLoginEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class PasswordLoginScreen extends JnTestTemplate{

	@Test
	public void invalidEmail() {
		this.executeLogin(TestVariables.INVALID_EMAIL, TestVariables.CORRECT_PASSWORD, JsProcessStatusExecuteLogin.invalidEmail);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExecuteLogin.lockedToken, x -> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JsProcessStatusExecuteLogin.missingSavingEmail, x -> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		this.execute(testVariables, JsProcessStatusExecuteLogin.missingSavePassword, x-> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExecuteLogin.lockedPassword, x -> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExecuteLogin.loginConflict, x -> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		this.expectedFlow(testVariables);
	}

	public String expectedFlow(TestVariables testVariables) {
		new PasswordRegistrationScreen().expectedFlow(testVariables);;
		JsEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		String result = this.execute(testVariables, JsProcessStatusExecuteLogin.expectedStatus, x -> TestVariables.CORRECT_PASSWORD);
		return result;
	}
 
	
	@Test
	public void lockPassword() {
		TestVariables testVariables = new TestVariables();
		PasswordRegistrationScreen passwordRegistrationScreen = new PasswordRegistrationScreen();
		passwordRegistrationScreen.expectedFlow(testVariables);
		JsEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExecuteLogin.wrongPassword, x -> TestVariables.WRONG_PASSWORD);
		this.execute(testVariables, JsProcessStatusExecuteLogin.wrongPassword, x -> TestVariables.WRONG_PASSWORD);
		this.execute(testVariables, JsProcessStatusExecuteLogin.passwordLockedRecently, x -> TestVariables.WRONG_PASSWORD);
		this.execute(testVariables, JsProcessStatusExecuteLogin.lockedPassword, x -> TestVariables.WRONG_PASSWORD);
		EmailRequestScreen emailRequestScreen = new EmailRequestScreen();
		emailRequestScreen.execute(testVariables, JsProcessStatusExistsLoginEmail.lockedPassword);
	}
	
	@Test
	public void missPasswordThenGetItRight() {
		TestVariables testVariables = new TestVariables();
		new PasswordRegistrationScreen().expectedFlow(testVariables);
		JsEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		
		for(int k = 1; k < 3; k++) {
			this.execute(testVariables, JsProcessStatusExecuteLogin.wrongPassword, x -> TestVariables.WRONG_PASSWORD);
		}
		this.execute(testVariables, JsProcessStatusExecuteLogin.expectedStatus, x -> TestVariables.CORRECT_PASSWORD);
	}
	
	@Test
	public void recentlyLockedPassword() {
		TestVariables testVariables = new TestVariables();
		new PasswordRegistrationScreen().expectedFlow(testVariables);;
		JsEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		
		for(int k = 1; k <= 2; k++) {
			this.execute(testVariables, JsProcessStatusExecuteLogin.wrongPassword, x -> TestVariables.WRONG_PASSWORD);
		}
		this.execute(testVariables, JsProcessStatusExecuteLogin.passwordLockedRecently, x -> TestVariables.WRONG_PASSWORD);
		this.execute(testVariables, JsProcessStatusExecuteLogin.lockedPassword, x -> TestVariables.CORRECT_PASSWORD);
	}

	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		String password = producer.apply(testVariables);
		String loginResponse = this.executeLogin(testVariables.VALID_EMAIL, password, expectedStatus);
		producer.apply(testVariables);
		return loginResponse;
	}
	
	private String executeLogin(String email, String password, CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation body = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.password, password);
		String uri = "login/"
		+ email;
		CcpJsonRepresentation responseJson = this.testEndpoint(expectedStatus, body, uri, CcpHttpResponseType.singleRecord);
		return responseJson.toString();
	}

	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}
	
}
