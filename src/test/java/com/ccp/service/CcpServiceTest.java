package com.ccp.service;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpServiceTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static class StubService implements CcpService {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
		public Class<?> getJsonValidationClass() {
			return StubService.class;
		}
		public String name() {
			return "stub";
		}
	}

	@Test
	public void executeTest() {
		assertNotNull(new StubService().execute(new HashMap<>()));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeNullParamTest() {
		new StubService().execute((java.util.Map<String, Object>) null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// execute retorna apply.content — nunca null naturalmente.
}
