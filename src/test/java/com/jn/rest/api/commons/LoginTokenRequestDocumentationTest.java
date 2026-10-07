package com.jn.rest.api.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Proves that the published OpenAPI document describes each token request path with its own action (finding 49): until
 * 2026-10-07 the documentation of the unlock and of the resend sat on each other's method names in
 * {@code JnOpenApiLogin}, and springdoc pairs it with the controller by the method name, so {@code /unlocking} was
 * documented as a resend and {@code /resending} as an unlock. Requires the jn API up at {@code localhost:8080}.
 */
public class LoginTokenRequestDocumentationTest {

	private static final String API_DOCS = "http://localhost:8080/api-docs";

	private final HttpClient client = HttpClient.newHttpClient();

	private JsonObject operationOf(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(API_DOCS)).GET().build();
		String document = this.client.send(request, BodyHandlers.ofString()).body();
		JsonObject paths = new JsonParser().parse(document).getAsJsonObject().getAsJsonObject("paths");
		JsonObject operation = paths.getAsJsonObject(path).getAsJsonObject("post");
		return operation;
	}

	@Test
	public void theUnlockPathIsDocumentedAsAnUnlock() throws Exception {
		JsonObject operation = this.operationOf("/login/{email}/token/request/unlocking");

		assertEquals("Request token unlock", operation.get("summary").getAsString());
		assertEquals("unlockLoginToken", operation.get("operationId").getAsString());
	}

	@Test
	public void theResendPathIsDocumentedAsAResend() throws Exception {
		JsonObject operation = this.operationOf("/login/{email}/token/request/resending");

		String summary = operation.get("summary").getAsString();
		assertTrue(summary, summary.startsWith("Request token resend"));
		assertEquals("resendLoginToken", operation.get("operationId").getAsString());
	}
}
