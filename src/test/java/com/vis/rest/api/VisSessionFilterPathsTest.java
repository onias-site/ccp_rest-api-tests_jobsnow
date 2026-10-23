package com.vis.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.Test;

/**
 * Finding 49, corrected in 2026-10-22: the e-mail filter and the session filter of the vis API (port 8081, which must be
 * up) cover the routes of the positions, {@code recruiters/{email}/positions/{title}}. Until then they covered
 * {@code /recruiter/*} (no "s") and a {@code /position/*} that no controller serves, so a request reached the controller
 * with no login and with any text in place of the e-mail (it answered 500).
 */
public class VisSessionFilterPathsTest {

	private HttpResponse<String> post(String email, String sessionToken) throws Exception {
		URI uri = URI.create("http://localhost:8081/recruiters/" + email + "/positions/developer");
		HttpRequest request = HttpRequest.newBuilder(uri)
				.header("Content-Type", "application/json")
				.header("sessionToken", sessionToken)
				.POST(HttpRequest.BodyPublishers.ofString("{}"))
				.build();
		HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		return response;
	}

	@Test
	public void aPositionWithNoSessionIsStoppedByTheSessionValidation() throws Exception {
		HttpResponse<String> response = this.post("onias@jobsnow.com", "");

		assertEquals(422, response.statusCode());
		assertTrue(response.body(), response.body().contains("JsBusinessValidateLogin"));
	}

	@Test
	public void aPositionWithAFakeSessionIsUnauthorized() throws Exception {
		assertEquals(401, this.post("onias@jobsnow.com", "ABCD1234").statusCode());
	}

	@Test
	public void aPositionWithAnInvalidEmailIsABadRequest() throws Exception {
		assertEquals(400, this.post("oniasjobsnow.com", "ABCD1234").statusCode());
	}
}
