package com.ccp.json.validations.global.engine;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpJsonValidationRulesEngineTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void getRulesExplanationTest() {
		assertNotNull(CcpJsonValidationRulesEngine.INSTANCE.getRulesExplanation(Object.class));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getRulesExplanationNullTest() {
		CcpJsonValidationRulesEngine.INSTANCE.getRulesExplanation(null);
	}
}
