package com.ccp.rest.api.spring.servlet.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.services.JnServiceLogin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Proves that {@link CcpPutSessionValuesAndExecuteTaskFilter} runs its task (the session validation) for every request,
 * before the controller: until 2026-10-06 the task ran only when the controller read a non-empty JSON body, so a request
 * without body, with an empty JSON, with another media type, or to an endpoint that does not read the body went through
 * without any session.
 */
public class SessionValidationTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final String URL = "http://localhost:8080/resume/someone@example.com";

	/** A task that counts its runs and refuses every request with 401. */
	static class RefusingTask implements CcpBusiness {
		final AtomicInteger runs = new AtomicInteger();

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			this.runs.incrementAndGet();
			throw new CcpErrorFlowDisturb(json, CcpProcessStatusDefault.UNHAUTHORIZED, "invalid session");
		}
	}

	/** A task that counts its runs and accepts every request. */
	static class AcceptingTask implements CcpBusiness {
		final AtomicInteger runs = new AtomicInteger();

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			this.runs.incrementAndGet();
			return json;
		}
	}

	/** What the filter answered and whether the request reached the chain (the controller). */
	static class Outcome {
		final AtomicInteger status = new AtomicInteger(200);
		final StringWriter body = new StringWriter();
		final AtomicReference<String> bodySeenByController = new AtomicReference<>();
		boolean reachedController;
	}

	private static ServletInputStream streamOf(byte[] bytes) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		return new ServletInputStream() {
			public int read() {
				return inputStream.read();
			}

			public boolean isFinished() {
				return inputStream.available() == 0;
			}

			public boolean isReady() {
				return true;
			}

			public void setReadListener(ReadListener listener) {
			}
		};
	}

	private static HttpServletRequest request(String method, String contentType, String body, Map<String, String> headers) {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		Map<String, String> allHeaders = new HashMap<>(headers);
		allHeaders.put("Host", "localhost:8080");
		allHeaders.put("User-Agent", "test");
		return (HttpServletRequest) Proxy.newProxyInstance(SessionValidationTest.class.getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, (proxy, m, args) -> {
					switch (m.getName()) {
					case "getMethod":
						return method;
					case "getContentType":
						return contentType.isEmpty() ? null : contentType;
					case "getInputStream":
						return streamOf(bytes);
					case "getRequestURL":
						return new StringBuffer(URL);
					case "getHeader":
						return allHeaders.get(args[0]);
					default:
						return null;
					}
				});
	}

	private static HttpServletResponse response(Outcome outcome) {
		PrintWriter writer = new PrintWriter(outcome.body, true);
		return (HttpServletResponse) Proxy.newProxyInstance(SessionValidationTest.class.getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, (proxy, m, args) -> {
					switch (m.getName()) {
					case "setStatus":
						outcome.status.set((Integer) args[0]);
						return null;
					case "getStatus":
						return outcome.status.get();
					case "getWriter":
						return writer;
					default:
						return null;
					}
				});
	}

	private static Outcome run(CcpBusiness task, HttpServletRequest request, boolean controllerReadsBody) {
		Outcome outcome = new Outcome();
		FilterChain chain = (ServletRequest req, jakarta.servlet.ServletResponse res) -> {
			outcome.reachedController = true;
			if (controllerReadsBody) {
				try {
					byte[] read = req.getInputStream().readAllBytes();
					outcome.bodySeenByController.set(new String(read, StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		};
		new CcpPutSessionValuesAndExecuteTaskFilter(task).doFilter(request, response(outcome), chain);
		return outcome;
	}

	private void assertRefused(HttpServletRequest request, boolean controllerReadsBody) {
		RefusingTask task = new RefusingTask();

		Outcome outcome = run(task, request, controllerReadsBody);

		assertEquals(1, task.runs.get());
		assertFalse("the request reached the controller without a valid session", outcome.reachedController);
		assertEquals(401, outcome.status.get());
		assertTrue(outcome.body.toString(), outcome.body.toString().contains("UNHAUTHORIZED"));
	}

	@Test
	public void aRequestWithoutBodyIsValidated() {
		this.assertRefused(request("GET", "", "", Map.of()), false);
	}

	@Test
	public void aRequestToAnEndpointThatDoesNotReadTheBodyIsValidated() {
		this.assertRefused(request("POST", "application/json", "{\"name\":\"x\"}", Map.of()), false);
	}

	@Test
	public void aRequestWithAnEmptyJsonIsValidated() {
		this.assertRefused(request("POST", "application/json", "{}", Map.of()), true);
	}

	@Test
	public void aRequestWithAnotherMediaTypeIsValidated() {
		this.assertRefused(request("POST", "text/plain", "anything", Map.of()), true);
	}

	@Test
	public void aRequestWithAJsonBodyIsValidated() {
		this.assertRefused(request("POST", "application/json", "{\"name\":\"x\"}", Map.of()), true);
	}

	@Test
	public void anAcceptedRequestReachesTheControllerWithTheSessionValuesAndTheTaskRunsOnce() {
		AcceptingTask task = new AcceptingTask();

		Outcome outcome = run(task, request("POST", "application/json", "{\"name\":\"x\"}", Map.of("sessionToken", "ABCD1234")), true);

		assertTrue(outcome.reachedController);
		assertEquals(1, task.runs.get());
		CcpJsonRepresentation body = new CcpJsonRepresentation(outcome.bodySeenByController.get());
		assertEquals("x", body.getAsString(new com.ccp.decorators.CcpFieldName("name")));
		assertEquals("ABCD1234", body.getAsString(new com.ccp.decorators.CcpFieldName("sessionToken")));
	}

	@Test
	public void withoutBodyTheEmailOfTheUrlArrivesAsText() {
		AcceptingTask task = new AcceptingTask();

		Outcome outcome = run(task, request("GET", "", "", Map.of()), true);

		assertTrue(outcome.reachedController);
		CcpJsonRepresentation body = new CcpJsonRepresentation(outcome.bodySeenByController.get());
		assertEquals("someone@example.com", body.getAsString(new com.ccp.decorators.CcpFieldName("email")));
	}

	@Test
	public void anInvalidJsonAnswers400WithoutReachingTheController() {
		AcceptingTask task = new AcceptingTask();

		Outcome outcome = run(task, request("POST", "application/json", "{not json", Map.of()), true);

		assertFalse(outcome.reachedController);
		assertEquals(400, outcome.status.get());
		assertEquals(0, task.runs.get());
	}

	@Test
	public void theRealSessionValidationRefusesARequestWithoutSessionToken() {
		Outcome outcome = run(JnServiceLogin.ValidateLogin, request("GET", "", "", Map.of()), false);

		assertFalse("the request reached the controller without a session token", outcome.reachedController);
		assertTrue(String.valueOf(outcome.status.get()), outcome.status.get() >= 400 && outcome.status.get() < 500);
	}
}
