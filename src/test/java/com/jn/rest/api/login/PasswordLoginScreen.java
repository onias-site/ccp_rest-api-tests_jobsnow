package com.jn.rest.api.login;

import java.util.function.Function;
import org.junit.Test;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class PasswordLoginScreen extends JnTestTemplate{

	@Test
	public void invalidEmail() {
		this.executeLogin(TestVariables.INVALID_EMAIL, TestVariables.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.invalidEmail);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExecuteLogin.lockedToken, x -> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JnProcessStatusExecuteLogin.missingSavingEmail, x -> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		this.execute(testVariables, JnProcessStatusExecuteLogin.missingSavePassword, x-> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExecuteLogin.lockedPassword, x -> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExecuteLogin.loginConflict, x -> TestVariables.CORRECT_PASSWORD);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		this.expectedFlow(testVariables);
	}

	public String expectedFlow(TestVariables testVariables) {
		new PasswordRegistrationScreen().expectedFlow(testVariables);;
		JnEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		String result = this.execute(testVariables, JnProcessStatusExecuteLogin.expectedStatus, x -> TestVariables.CORRECT_PASSWORD);
		return result;
	}
 
	
	@Test
	public void lockPassword() {
		TestVariables testVariables = new TestVariables();
		PasswordRegistrationScreen passwordRegistrationScreen = new PasswordRegistrationScreen();
		passwordRegistrationScreen.expectedFlow(testVariables);
		JnEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExecuteLogin.wrongPassword, x -> TestVariables.WRONG_PASSWORD);
		this.execute(testVariables, JnProcessStatusExecuteLogin.wrongPassword, x -> TestVariables.WRONG_PASSWORD);
		this.execute(testVariables, JnProcessStatusExecuteLogin.passwordLockedRecently, x -> TestVariables.WRONG_PASSWORD);
		this.execute(testVariables, JnProcessStatusExecuteLogin.lockedPassword, x -> TestVariables.WRONG_PASSWORD);
		EmailRequestScreen emailRequestScreen = new EmailRequestScreen();
		emailRequestScreen.execute(testVariables, JnProcessStatusExistsLoginEmail.lockedPassword);
	}
	
	@Test
	public void missPasswordThenGetItRight() {
		TestVariables testVariables = new TestVariables();
		new PasswordRegistrationScreen().expectedFlow(testVariables);
		JnEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		
		for(int k = 1; k < 3; k++) {
			this.execute(testVariables, JnProcessStatusExecuteLogin.wrongPassword, x -> TestVariables.WRONG_PASSWORD);
		}
		this.execute(testVariables, JnProcessStatusExecuteLogin.expectedStatus, x -> TestVariables.CORRECT_PASSWORD);
	}
	
	@Test
	public void recentlyLockedPassword() {
		TestVariables testVariables = new TestVariables();
		new PasswordRegistrationScreen().expectedFlow(testVariables);;
		JnEntityLoginSessionConflict.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		
		for(int k = 1; k <= 2; k++) {
			this.execute(testVariables, JnProcessStatusExecuteLogin.wrongPassword, x -> TestVariables.WRONG_PASSWORD);
		}
		this.execute(testVariables, JnProcessStatusExecuteLogin.passwordLockedRecently, x -> TestVariables.WRONG_PASSWORD);
		this.execute(testVariables, JnProcessStatusExecuteLogin.lockedPassword, x -> TestVariables.CORRECT_PASSWORD);
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
