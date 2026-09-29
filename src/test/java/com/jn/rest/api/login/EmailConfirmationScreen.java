package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
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
import com.jn.status.login.JnProcessStatusCreateLoginEmail;

public class EmailConfirmationScreen  extends JnTestTemplate{

	@Test
	public void invalidEmail() {
		this.confirmEmail(TestVariables.INVALID_EMAIL, JnProcessStatusCreateLoginEmail.invalidEmail);
	}
	
	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusCreateLoginEmail.lockedToken);
	}
	
	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusCreateLoginEmail.lockedPassword);
	}
	
	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusCreateLoginEmail.loginConflict);
	}
	
	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusCreateLoginEmail.missingSavePassword);
	}
	
	@Test
	public void missingPreRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusCreateLoginEmail.missingSaveAnswers);
	}
	
	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		this.execute(testVariables, JnProcessStatusCreateLoginEmail.expectedStatus);
	}
	//
	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		this.confirmEmail(testVariables.VALID_EMAIL, expectedStatus);
		String producedValue = producer.apply(testVariables);
		return producedValue;
	}
	
	private CcpJsonRepresentation confirmEmail(String email, CcpProcessStatus expectedStatus) {
		
		String uri = "/login/"
		+ email
		+ "/token";
		CcpJsonRepresentation responseJson = this.testEndpoint(uri, expectedStatus);
		return responseJson;
	}


	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}

}
