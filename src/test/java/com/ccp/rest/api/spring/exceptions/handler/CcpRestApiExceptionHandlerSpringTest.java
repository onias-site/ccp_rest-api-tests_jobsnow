package com.ccp.rest.api.spring.exceptions.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatusDefault;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

import jakarta.servlet.http.HttpServletResponse;

public class CcpRestApiExceptionHandlerSpringTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void constructorTest() {
		assertNotNull(new CcpRestApiExceptionHandlerSpring());
	}

	@Test
	public void methodNoSupportedTest() {
		new CcpRestApiExceptionHandlerSpring().methodNoSupported();
	}

	/**
	 * A body the controller cannot read (a GET without Content-Type to a controller that requires the body) answers 400
	 * through its own handler, and not 500 through the one of {@code Throwable}, which records a system error.
	 */
	@Test
	public void unreadableBodyAnswers400Test() throws NoSuchMethodException {
		Method unreadableBody = CcpRestApiExceptionHandlerSpring.class.getMethod("unreadableBody");
		ExceptionHandler exceptionHandler = unreadableBody.getAnnotation(ExceptionHandler.class);
		List<Class<? extends Throwable>> handledExceptions = Arrays.asList(exceptionHandler.value());
		assertTrue(handledExceptions.contains(HttpMessageNotReadableException.class));
		ResponseStatus responseStatus = unreadableBody.getAnnotation(ResponseStatus.class);
		assertEquals(HttpStatus.BAD_REQUEST, responseStatus.code());
		new CcpRestApiExceptionHandlerSpring().unreadableBody();
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
				new Class<?>[] { HttpServletResponse.class }, new StatusRecordingResponseHandler(status));
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

	/**
	 * Finding 25: until 2026-10-15 a business error answered, in {@code message}, the whole JSON of the request, with the
	 * session token, the IP and the user agent. Now the body has only the name of the status and the fields the exception
	 * lists.
	 */
	@Test
	public void aBusinessErrorDoesNotEchoTheRequestTest() throws IOException {
		AtomicInteger status = new AtomicInteger();
		HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, new StatusRecordingResponseHandler(status));
		CcpFieldName skill = new CcpFieldName("skill");
		CcpJsonRepresentation request = new CcpJsonRepresentation(
				"{\"skill\":\"JAVA\",\"sessionToken\":\"SECRET\",\"ip\":\"10.0.0.1\",\"userAgent\":\"Chrome\",\"email\":\"someone@jobsnow.com\"}");
		CcpErrorFlowDisturb businessError = new CcpErrorFlowDisturb(request, CcpProcessStatusDefault.CONFLICT, skill);

		Map<String, Object> body = new CcpRestApiExceptionHandlerSpring().handle(businessError, response);

		assertEquals(409, status.get());
		assertEquals("CONFLICT", body.get(CcpRestApiExceptionHandlerSpring.JsonFieldNames.status.name()));
		assertEquals("CONFLICT", body.get(CcpRestApiExceptionHandlerSpring.JsonFieldNames.message.name()));
		assertEquals("JAVA", body.get("skill"));
		assertEquals(body.toString(), 3, body.size());
		assertFalse(body.toString(), body.toString().contains("SECRET"));
	}

	@Test
	public void getHandledExceptionToLogWithoutCauseTest() {
		RuntimeException mainException = new RuntimeException("main message");

		CcpJsonRepresentation handledException = CcpRestApiExceptionHandlerSpring.getHandledExceptionToLog(mainException);

		List<String> causeChain = handledException.getAsStringList(CcpJsonRepresentation.CcpStackTraceFields.cause);
		assertTrue(causeChain.isEmpty());
	}
}
