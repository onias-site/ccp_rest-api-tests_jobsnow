package com.ccp.service;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class CcpServiceTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}



	@Test
	public void executeTest() {
		assertNotNull(CcpServiceTestStubService.INSTANCE.execute(new HashMap<>()));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeNullParamTest() {
		CcpServiceTestStubService.INSTANCE.execute((java.util.Map<String, Object>) null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// execute returns apply.content — naturally never null.
}
