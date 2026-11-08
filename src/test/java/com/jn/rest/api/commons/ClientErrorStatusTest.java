package com.jn.rest.api.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.Test;

/**
 * A client error must come out with the client's status, and not as a system error. Until 2026-09-27
 * the {@code @ExceptionHandler(Throwable)} of {@code CcpRestApiExceptionHandlerSpring} turned a
 * nonexistent route into 500 and also saved it in {@code JnEntityJobsnowError}, notifying support the
 * first time within the hour. Requires the js API up at {@code localhost:8082}, like the other login REST tests.
 */
public class ClientErrorStatusTest {

	private static final String API = "http://localhost:8082/";

	private final HttpClient client = HttpClient.newHttpClient();

	@Test
	public void nonexistentRouteReturns404() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(API + "route/that/does/not/exist/" + System.nanoTime())).GET().build();
		int status = this.client.send(request, BodyHandlers.discarding()).statusCode();
		assertEquals("nonexistent route", 404, status);
	}

	/** Until 2026-09-27 the malformed body became an empty json and the operation happened (201, token created). */
	@Test
	public void malformedJsonReturns400() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(API + "login/malformed" + System.nanoTime() + "@jobsnow.com/token"))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString("{\"x\":"))
				.build();
		int status = this.client.send(request, BodyHandlers.discarding()).statusCode();
		assertEquals("malformed json", 400, status);
	}

	/** Endpoints that only use the e-mail from the URL send no body: that must not become 400. */
	@Test
	public void postWithoutBodyIsStillAccepted() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(API + "login/nobody" + System.nanoTime() + "@jobsnow.com/token"))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.noBody())
				.build();
		int status = this.client.send(request, BodyHandlers.discarding()).statusCode();
		assertNotEquals("POST without body was refused as invalid json", 400, status);
		assertNotEquals("POST without body caused a system error", 500, status);
	}

	/**
	 * Jetty rejects the protected paths before any controller and dispatches to {@code /error} with no status: until
	 * 2026-11-08 Spring answered status 999 and Jetty turned it into 500 (a URL scan got it for {@code /WEB-INF/web.xml}).
	 */
	@Test
	public void protectedPathsOfTheServletContainerReturn404() throws Exception {
		for (String path : new String[] {"WEB-INF/web.xml", "WEB-INF/", "META-INF/MANIFEST.MF", "error"}) {
			HttpRequest request = HttpRequest.newBuilder(URI.create(API + path)).GET().build();
			int status = this.client.send(request, BodyHandlers.discarding()).statusCode();
			assertEquals(path, 404, status);
		}
	}

	/** Until 2026-11-08 the actuator answered 200 in {@code /actuator} and {@code /actuator/health}; nothing used it. */
	@Test
	public void theActuatorIsNotExposed() throws Exception {
		for (String path : new String[] {"actuator", "actuator/health", "actuator/info"}) {
			HttpRequest request = HttpRequest.newBuilder(URI.create(API + path)).GET().build();
			int status = this.client.send(request, BodyHandlers.discarding()).statusCode();
			assertEquals(path, 404, status);
		}
	}

	@Test
	public void unsupportedContentTypeReturns415() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(API + "login/teste@teste.com/token"))
				.header("Content-Type", "text/plain")
				.POST(BodyPublishers.ofString("x"))
				.build();
		int status = this.client.send(request, BodyHandlers.discarding()).statusCode();
		assertEquals("unsupported content type", 415, status);
	}
}
