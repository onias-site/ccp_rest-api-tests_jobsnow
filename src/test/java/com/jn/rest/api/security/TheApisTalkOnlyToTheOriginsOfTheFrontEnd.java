package com.jn.rest.api.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The CORS of the APIs ({@code CcpCorsAndMethodsFilter}): only the origins of the front end receive the CORS headers;
 * the preflight of another site answers 403 and its plain request comes with no {@code Access-Control-Allow-Origin},
 * so the browser of a visitor does not hand the answer to that site. Until 2026-11-10 every origin was allowed
 * ({@code *}). TRACE answers 405 (Jetty echoed the request under 200).
 */
public class TheApisTalkOnlyToTheOriginsOfTheFrontEnd {

	private static final String FRONT_END = "https://onias.site";

	private static final String LOCAL_FRONT_END = "http://localhost:3000";

	private static final String ANOTHER_SITE = "https://evil.example";

	private static final String[][] ROUTES = {
		{"8082", "login/a@teste.com/token"},
		{"8082", "contact-us/a@teste.com"},
		{"8081", "resume/a@teste.com/viewers/202611"},
		{"8081", "statistics/skills/telegramGroups"},
	};

	@BeforeClass
	public static void theApisMustBeUp() {
		SecurityHttp.load();
		SecurityHttp.theApiMustBeUp(SecurityHttp.JS);
		SecurityHttp.theApiMustBeUp(SecurityHttp.VIS);
	}

	private HttpResponse<String> preflight(String[] route, String origin) throws Exception {
		URI uri = URI.create("http://localhost:" + route[0] + "/" + route[1]);
		HttpRequest request = HttpRequest.newBuilder(uri)
				.header("Origin", origin)
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "sessiontoken,content-type")
				.method("OPTIONS", HttpRequest.BodyPublishers.noBody())
				.build();
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> get(String[] route, String origin) throws Exception {
		URI uri = URI.create("http://localhost:" + route[0] + "/" + route[1]);
		HttpRequest request = HttpRequest.newBuilder(uri).header("Origin", origin).GET().build();
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}

	private String allowedOrigin(HttpResponse<String> response) {
		return response.headers().firstValue("Access-Control-Allow-Origin").orElse("");
	}

	@Test
	public void theFrontEndPassesThePreflight() throws Exception {
		for (String origin : new String[] {FRONT_END, LOCAL_FRONT_END}) {
			for (String[] route : ROUTES) {
				HttpResponse<String> response = this.preflight(route, origin);

				assertEquals(route[1] + " " + origin, 200, response.statusCode());
				assertEquals(route[1] + " " + origin, origin, this.allowedOrigin(response));
				String allowedHeaders = response.headers().firstValue("Access-Control-Allow-Headers").orElse("").toLowerCase();
				assertTrue(allowedHeaders, allowedHeaders.contains("sessiontoken") && allowedHeaders.contains("content-type"));
			}
		}
	}

	@Test
	public void anotherSiteFailsThePreflight() throws Exception {
		for (String[] route : ROUTES) {
			HttpResponse<String> response = this.preflight(route, ANOTHER_SITE);

			assertEquals(route[1], 403, response.statusCode());
			assertEquals(route[1], "", this.allowedOrigin(response));
		}
	}

	@Test
	public void anotherSiteGetsNoCorsHeaderOnAPlainRequest() throws Exception {
		for (String[] route : ROUTES) {
			HttpResponse<String> response = this.get(route, ANOTHER_SITE);

			assertEquals(route[1], "", this.allowedOrigin(response));
			assertFalse(route[1], "*".equals(this.allowedOrigin(this.get(route, FRONT_END))));
		}
	}

	@Test
	public void everyAnswerVariesByOriginForTheCdn() throws Exception {
		for (String[] route : ROUTES) {
			HttpResponse<String> response = this.get(route, FRONT_END);
			List<String> vary = response.headers().allValues("Vary");

			assertTrue(route[1] + " " + vary, vary.toString().contains("Origin"));
		}
	}

	@Test
	public void traceIsNotAllowed() throws Exception {
		for (String[] route : ROUTES) {
			URI uri = URI.create("http://localhost:" + route[0] + "/" + route[1]);
			HttpRequest request = HttpRequest.newBuilder(uri).method("TRACE", HttpRequest.BodyPublishers.noBody()).build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

			assertEquals(route[1], 405, response.statusCode());
		}
	}
}
