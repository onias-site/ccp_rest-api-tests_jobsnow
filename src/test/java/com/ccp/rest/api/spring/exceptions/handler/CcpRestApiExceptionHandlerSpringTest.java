package com.ccp.rest.api.spring.exceptions.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

import jakarta.servlet.http.HttpServletResponse;

public class CcpRestApiExceptionHandlerSpringTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void constructorTest() {
		assertNotNull(new CcpRestApiExceptionHandlerSpring());
	}

	@Test
	public void methodNoSupportedTest() {
		new CcpRestApiExceptionHandlerSpring().methodNoSupported();
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void handleValidationErrorNullTest() {
		new CcpRestApiExceptionHandlerSpring().handle((com.ccp.json.validations.global.engine.CcpJsonValidationError) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void handleFlowDisturbErrorNullTest() throws IOException {
		new CcpRestApiExceptionHandlerSpring().handle((com.ccp.flow.CcpErrorFlowDisturb) null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void handleThrowableNullTest() {
		new CcpRestApiExceptionHandlerSpring().handle((Throwable) null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getHandledExceptionToLogThrowableNullTest() {
		CcpRestApiExceptionHandlerSpring.getHandledExceptionToLog((Throwable) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getHandledExceptionToLogJsonNullTest() {
		CcpRestApiExceptionHandlerSpring.getHandledExceptionToLog((CcpJsonRepresentation) null);
	}

	@Test
	public void getHandledExceptionToLogKeepsTheMessageOfEveryCauseTest() {
		IllegalStateException rootCause = new IllegalStateException("root message");
		IllegalArgumentException directCause = new IllegalArgumentException("direct message", rootCause);
		RuntimeException mainException = new RuntimeException("main message", directCause);

		CcpJsonRepresentation handledException = CcpRestApiExceptionHandlerSpring.getHandledExceptionToLog(mainException);

		List<String> causeChain = handledException.getAsStringList(CcpJsonRepresentation.CcpStackTraceFields.cause);
		List<String> expectedCauseChain = Arrays.asList("java.lang.IllegalArgumentException: direct message", "java.lang.IllegalStateException: root message");
		assertEquals(expectedCauseChain, causeChain);
	}

	/** Finding 36: until 2026-10-07 the 500 answered an empty body, with nothing the user could hand to support. */
	@Test
	public void theInternalErrorAnswersTheStatusAndTheHashOfTheRecordedErrorTest() {
		AtomicReference<CcpJsonRepresentation> recordedError = new AtomicReference<>();
		AtomicInteger status = new AtomicInteger();
		HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, (proxy, m, args) -> {
					if ("setStatus".equals(m.getName())) {
						status.set((Integer) args[0]);
					}
					return null;
				});
		CcpBusiness previousHandler = CcpRestApiExceptionHandlerSpring.genericExceptionHandler;
		CcpRestApiExceptionHandlerSpring.genericExceptionHandler = new CcpBusiness() {
			public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
				recordedError.set(json);
				return json;
			}
		};
		try {
			Map<String, Object> body = new CcpRestApiExceptionHandlerSpring().handle(new IllegalStateException("secret detail"), response);

			assertEquals(500, status.get());
			assertEquals("INTERNAL_SERVER_ERROR", body.get(CcpRestApiExceptionHandlerSpring.JsonFieldNames.status.name()));
			String expectedHash = recordedError.get().getAsString(CcpRestApiExceptionHandlerSpring.JsonFieldNames.stackTraceHash);
			assertEquals(expectedHash, body.get(CcpRestApiExceptionHandlerSpring.JsonFieldNames.stackTraceHash.name()));
			assertEquals(2, body.size());
		} finally {
			CcpRestApiExceptionHandlerSpring.genericExceptionHandler = previousHandler;
		}
	}

	@Test
	public void getHandledExceptionToLogWithoutCauseTest() {
		RuntimeException mainException = new RuntimeException("main message");

		CcpJsonRepresentation handledException = CcpRestApiExceptionHandlerSpring.getHandledExceptionToLog(mainException);

		List<String> causeChain = handledException.getAsStringList(CcpJsonRepresentation.CcpStackTraceFields.cause);
		assertTrue(causeChain.isEmpty());
	}
}
