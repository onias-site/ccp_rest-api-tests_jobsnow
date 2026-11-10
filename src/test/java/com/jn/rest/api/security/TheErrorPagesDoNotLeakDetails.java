package com.jn.rest.api.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The requests that Jetty refuses by itself, before any filter or controller (an encoded slash in a path variable, an
 * encoded control character), answer 400 with no stack trace and no Java class. Until 2026-11-10 the page of Jetty
 * carried the whole stack trace (found by the local dynamic scan, script 50 of documentation/deploy).
 */
public class TheErrorPagesDoNotLeakDetails {

	private static final String[] PATHS = {
		"companies/search/autocomplete/..%2F..%2Fetc%2Fpasswd",
		"statistics/skills/..%2F..%2Fwindows/11/months",
		"resume/a@teste.com/language/%00",
	};

	@BeforeClass
	public static void theApisMustBeUp() {
		SecurityHttp.load();
		SecurityHttp.theApiMustBeUp(SecurityHttp.JS);
		SecurityHttp.theApiMustBeUp(SecurityHttp.VIS);
	}

	private HttpResponse<String> get(int port, String path) throws Exception {
		URI uri = URI.create("http://localhost:" + port + "/" + path);
		HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response;
	}

	private void assertNoDetails(HttpResponse<String> response) {
		String body = response.body();
		assertFalse(body, body.contains("org.eclipse.jetty"));
		assertFalse(body, body.contains("Exception"));
		assertFalse(body, body.contains("\tat "));
	}

	@Test
	public void theVisApiAnswersTheRefusedPathsWithNoDetails() throws Exception {
		for (String path : PATHS) {
			HttpResponse<String> response = this.get(SecurityHttp.VIS, path);

			assertEquals(path, 400, response.statusCode());
			this.assertNoDetails(response);
		}
	}

	@Test
	public void theJsApiAnswersTheRefusedPathsWithNoDetails() throws Exception {
		for (String path : new String[] {"login/a@teste.com/..%2F..%2Fetc", "grouper/a@teste.com/x/y/..%2Fz"}) {
			HttpResponse<String> response = this.get(SecurityHttp.JS, path);

			assertEquals(path, 400, response.statusCode());
			this.assertNoDetails(response);
		}
	}
}
