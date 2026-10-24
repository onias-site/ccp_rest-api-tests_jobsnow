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
import com.js.status.login.JsProcessStatusSaveAnswers;

public class PreRegistrationScreen  extends JnTestTemplate{

	@Test
	public void invalidEmail() {
		this.savePreRegistration(TestVariables.INVALID_EMAIL, JsProcessStatusSaveAnswers.invalidEmail);
	}
 
	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusSaveAnswers.lockedToken);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JsProcessStatusSaveAnswers.missingToken);
	}

	@Test
	public void userAlreadyLoggedIn() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusSaveAnswers.loginConflict);
	}

	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusSaveAnswers.missingPassword);
	}

	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusSaveAnswers.lockedPassword);
	}
	
	@Test
	public void happyPath() { 
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusSaveAnswers.expectedStatus); 
	}
	//
	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		this.savePreRegistration(testVariables.VALID_EMAIL, expectedStatus);
		String producedValue = producer.apply(testVariables);
		return producedValue;
	}
	
	private void savePreRegistration(String email, CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation body = CcpOtherConstants
				.EMPTY_JSON
				.put(JsEntityLoginAnswers.Fields.goal, "jobs")
				.put(JsEntityLoginAnswers.Fields.channel, "linkedin")
				;
		String uri = "login/"+ email 	+ "/pre-registration";
		this.testEndpoint(expectedStatus, body, uri,  CcpHttpResponseType.singleRecord);
	}

	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}

}
