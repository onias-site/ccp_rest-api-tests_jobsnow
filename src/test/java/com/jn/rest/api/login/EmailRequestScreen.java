package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginToken;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusExistsLoginEmail;

public class EmailRequestScreen extends JnTestTemplate{
	
	@Test
	public void invalidEmail() {
		this.checkEmailExists(JsProcessStatusExistsLoginEmail.invalidEmail, TestVariables.INVALID_EMAIL);
	}
	
	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExistsLoginEmail.lockedToken);
	}
	
	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JsProcessStatusExistsLoginEmail.missingEmail);		
	}
	
	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN); 
		this.execute(testVariables, JsProcessStatusExistsLoginEmail.lockedPassword);		
	}
	
	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExistsLoginEmail.loginConflict);		
	}
	
	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExistsLoginEmail.missingPassword);		
	}
	
	@Test
	public void missingPreRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExistsLoginEmail.missingAnswers);		
	}
	
	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusExistsLoginEmail.expectedStatus);		
	}
	
	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		this.checkEmailExists(expectedStatus, testVariables.VALID_EMAIL);
		String producedValue = producer.apply(testVariables);
		return producedValue;
	}	
	
	private void checkEmailExists(CcpProcessStatus expectedStatus, String email) {
		String uri = "login/"
				+ email
				+ "/token";

		this.testEndpoint(uri, expectedStatus);
	}

	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.HEAD;
	}
}
