package com.ccp.rest.api.spring.exceptions.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

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

	@Test
	public void getHandledExceptionToLogWithoutCauseTest() {
		RuntimeException mainException = new RuntimeException("main message");

		CcpJsonRepresentation handledException = CcpRestApiExceptionHandlerSpring.getHandledExceptionToLog(mainException);

		List<String> causeChain = handledException.getAsStringList(CcpJsonRepresentation.CcpStackTraceFields.cause);
		assertTrue(causeChain.isEmpty());
	}
}
