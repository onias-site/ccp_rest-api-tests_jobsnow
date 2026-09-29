package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;

public class EmailRequestScreen extends JnTestTemplate{
	
	@Test
	public void invalidEmail() {
		this.checkEmailExists(JnProcessStatusExistsLoginEmail.invalidEmail, TestVariables.INVALID_EMAIL);
	}
	
	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExistsLoginEmail.lockedToken);
	}
	
	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JnProcessStatusExistsLoginEmail.missingEmail);		
	}
	
	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN); 
		this.execute(testVariables, JnProcessStatusExistsLoginEmail.lockedPassword);		
	}
	
	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExistsLoginEmail.loginConflict);		
	}
	
	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExistsLoginEmail.missingPassword);		
	}
	
	@Test
	public void missingPreRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExistsLoginEmail.missingAnswers);		
	}
	
	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusExistsLoginEmail.expectedStatus);		
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
