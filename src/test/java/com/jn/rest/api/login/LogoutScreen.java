package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusExecuteLogout;
enum LogoutScreenConstants implements CcpJsonFieldName{
	originalToken
}
public class LogoutScreen extends JnTestTemplate {

	@Test
	public void invalidEmail() {
		TestVariables testVariables = new TestVariables(TestVariables.INVALID_EMAIL);
		this.execute(testVariables, JnProcessStatusExecuteLogout.invalidEmail, variables -> "anyToken");
	}

	@Test
	public void userNotLoggedIn() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JnProcessStatusExecuteLogout.missingLogin, variables -> "anyToken");
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		// saves the raw json: the token is chosen here, and the transformer stores its hash. Saving the
		// pre-transformed json (email hashed) was rejected by the validator.
		String token = "12345678";
		Function<TestVariables, String> producer = variables -> {
			CcpJsonRepresentation sessionJson = variables.REQUEST_TO_LOGIN
					.put(JnEntityLoginSessionValidation.Fields.token, token)
					;
			JnEntityLoginSessionValidation.ENTITY.save(sessionJson);
			return token;
		};
		this.execute(testVariables, JnProcessStatusExecuteLogout.expectedStatus, producer);
	}
	
	protected CcpJsonRepresentation getHeaders() { 
		CcpJsonRepresentation headers = CcpOtherConstants.EMPTY_JSON
				;
		return headers;
	}
	
	
	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.DELETE;
	}

	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus,
			Function<TestVariables, String> producer) {
		String producedToken = producer.apply(testVariables);
		String encodedToken = new CcpStringDecorator(producedToken).url().asEnconded();
		String sessionToken = encodedToken;
		String uri = "/login/" + testVariables.VALID_EMAIL + "/" + sessionToken;
		this.testEndpoint(uri, expectedStatus);
		return sessionToken;
		
	}

}
