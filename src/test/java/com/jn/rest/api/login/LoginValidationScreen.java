package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;

public class LoginValidationScreen  extends JnTestTemplate{
	private static enum Fields implements CcpJsonFieldName{
		sessionToken
		
	}
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.GET;
	}

	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		
		CcpJsonRepresentation body = this.registerPasswordToEnterTheSystem(testVariables);
				;
		String sessionToken = producer.apply(testVariables);
		String uri = "login/"
				+ testVariables.VALID_EMAIL
				+ "/" + sessionToken
				;
		CcpJsonRepresentation responseJson = this.testEndpoint(expectedStatus, body, uri,  CcpHttpResponseType.singleRecord);
		return responseJson.toString();
	}

	private CcpJsonRepresentation registerPasswordToEnterTheSystem(TestVariables testVariables) {
		PasswordRegistrationScreen passwordRegistrationScreen = new PasswordRegistrationScreen();
		String flowResult = passwordRegistrationScreen.expectedFlow(testVariables);
		CcpJsonRepresentation body =  new CcpJsonRepresentation(flowResult);
		return body;
	}
	private CcpJsonRepresentation executeLogin(TestVariables testVariables) {
		PasswordLoginScreen passwordLoginScreen = new PasswordLoginScreen();
		String flowResult = passwordLoginScreen.expectedFlow(testVariables);
		CcpJsonRepresentation body =  new CcpJsonRepresentation(flowResult);
		return body;
	}
	
	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, CcpProcessStatusDefault.OK, x -> this.registerPasswordToEnterTheSystem(x).getAsString(Fields.sessionToken));
		this.execute(testVariables, CcpProcessStatusDefault.OK, x -> this.executeLogin(x).getAsString(Fields.sessionToken));
	}

	@Test
	public void tokenInInvalidFormat() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY, x -> "123456789");
		this.execute(testVariables, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY, x -> "1234567");
	}
	
	
	@Test
	public void invalidToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, CcpProcessStatusDefault.UNHAUTHORIZED, x -> "A8B7C6D5");

	}
}
