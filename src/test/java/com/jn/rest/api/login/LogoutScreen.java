package com.jn.rest.api.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
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
import com.jn.json.fields.validation.JnJsonCommonsFields;
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

	/**
	 * A logout carrying a JSON body ends the session. Until 2026-10-09 it answered 500 with "invalid json": the filter
	 * swaps the body for a larger JSON with the session values but kept the original {@code Content-Length}, and Spring
	 * read only that many bytes of the new body. The Apache client of {@link #execute} sends no body in a DELETE, so this
	 * test sends it through {@link HttpClient}.
	 */
	@Test
	public void happyPathWithJsonBody() throws IOException, InterruptedException {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		String token = "12345678";
		CcpJsonRepresentation sessionJson = testVariables.REQUEST_TO_LOGIN.put(JnEntityLoginSessionValidation.Fields.token, token);
		JnEntityLoginSessionValidation.ENTITY.save(sessionJson);

		String url = this.ENDPOINT_URL + "login/" + testVariables.VALID_EMAIL + "/" + token;
		URI uri = URI.create(url);
		BodyPublisher jsonBody = HttpRequest.BodyPublishers.ofString("{}");
		// the session key holds the user agent, so the request repeats the one saved above
		String userAgent = testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.userAgent);
		HttpRequest request = HttpRequest.newBuilder(uri)
				.header("Content-Type", "application/json")
				.header("User-Agent", userAgent)
				.method("DELETE", jsonBody)
				.build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		int expectedStatus = JnProcessStatusExecuteLogout.expectedStatus.asNumber();
		String body = response.body();
		assertEquals(body, expectedStatus, response.statusCode());
		boolean sessionStillExists = JnEntityLoginSessionValidation.ENTITY.exists(sessionJson);
		assertFalse(sessionStillExists);
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
