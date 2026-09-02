package com.ccp.business;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class CcpBusinessTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}



	@Test
	public void canBeSavedAsAsyncTaskDefaultTest() {
		assertTrue(new NoopBusiness().canBeSavedAsAsyncTask());
	}

	@Test
	public void getJsonValidationClassDefaultTest() {
		assertNotNull(new NoopBusiness().getJsonValidationClass());
	}

	@Test
	public void nameDefaultTest() {
		assertNotNull(new NoopBusiness().name());
	}

	@Test
	public void executeSemValidacoesTest() {
		CcpJsonRepresentation r = new NoopBusiness().execute(CcpOtherConstants.EMPTY_JSON);
		assertNotNull(r);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void applyNullParamTest() {
		new NoopBusiness().execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeNullParamTest() {
		new NoopBusiness().execute(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────



	@org.junit.Test(expected = com.ccp.aop.CcpNullReturnException.class)
	public void applyRetornaNullDisparaAopTest() {
		new RetornaNullBusiness().execute(CcpOtherConstants.EMPTY_JSON);
	}
}
