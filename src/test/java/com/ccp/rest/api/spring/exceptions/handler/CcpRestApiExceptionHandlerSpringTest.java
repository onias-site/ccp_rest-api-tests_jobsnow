package com.ccp.rest.api.spring.exceptions.handler;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

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
	public void construtorTest() {
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
		new CcpRestApiExceptionHandlerSpring().handle((Throwable) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getHandledExceptionToLogThrowableNullTest() {
		CcpRestApiExceptionHandlerSpring.getHandledExceptionToLog((Throwable) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getHandledExceptionToLogJsonNullTest() {
		CcpRestApiExceptionHandlerSpring.getHandledExceptionToLog((CcpJsonRepresentation) null);
	}
}
