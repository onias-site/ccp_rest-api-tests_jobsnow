package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusCreateLoginToken;
import com.jn.utils.JnLanguage;

public class OnEnteringPasswordRegistrationScreen extends JnTestTemplate{

	@Test
	public void invalidEmail() {
		this.createLoginToken(TestVariables.INVALID_EMAIL, JnProcessStatusCreateLoginToken.statusInvalidEmail);
	} 
	 
	@Test
	public void lockedToken() { 
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusCreateLoginToken.statusLockedToken);
	}
	
	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JnProcessStatusCreateLoginToken.statusMissingEmail);
	}
	
	
	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		this.execute(testVariables, JnProcessStatusCreateLoginToken.expectedStatus);
	} 

	
	
	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		this.createLoginToken(testVariables.VALID_EMAIL, expectedStatus);
		String producedValue = producer.apply(testVariables);
		return producedValue;
	}
	
	private void createLoginToken(String email, CcpProcessStatus expectedStatus) {
		String uri = "login/"
				+ email
				+ "/token/language/"+ JnLanguage.portuguese.name();
		this.testEndpoint(uri, expectedStatus);
	}
	
	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}

}
