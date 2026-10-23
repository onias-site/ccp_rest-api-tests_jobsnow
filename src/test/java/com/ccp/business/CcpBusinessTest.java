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
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}



	@Test
	public void canBeSavedAsAsyncTaskDefaultTest() {
		assertTrue(NoopBusiness.INSTANCE.canBeSavedAsAsyncTask());
	}

	@Test
	public void getJsonValidationClassDefaultTest() {
		assertNotNull(NoopBusiness.INSTANCE.getJsonValidationClass());
	}

	@Test
	public void nameDefaultTest() {
		assertNotNull(NoopBusiness.INSTANCE.name());
	}

	@Test
	public void executeWithoutValidationsTest() {
		CcpJsonRepresentation result = NoopBusiness.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
		assertNotNull(result);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void applyNullParamTest() {
		NoopBusiness.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeNullParamTest() {
		NoopBusiness.INSTANCE.execute(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────



	@org.junit.Test(expected = com.ccp.aop.CcpNullReturnException.class)
	public void applyReturningNullTriggersAopTest() {
		ReturnsNullBusiness.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
	}
}
