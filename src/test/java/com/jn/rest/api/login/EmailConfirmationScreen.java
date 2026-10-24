package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
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
import com.js.status.login.JsProcessStatusCreateLoginEmail;

public class EmailConfirmationScreen  extends JnTestTemplate{

	@Test
	public void invalidEmail() {
		this.confirmEmail(TestVariables.INVALID_EMAIL, JsProcessStatusCreateLoginEmail.invalidEmail);
	}
	
	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusCreateLoginEmail.lockedToken);
	}
	
	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusCreateLoginEmail.lockedPassword);
	}
	
	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusCreateLoginEmail.loginConflict);
	}
	
	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusCreateLoginEmail.missingSavePassword);
	}
	
	@Test
	public void missingPreRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusCreateLoginEmail.missingSaveAnswers);
	}
	
	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		this.execute(testVariables, JsProcessStatusCreateLoginEmail.expectedStatus);
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
