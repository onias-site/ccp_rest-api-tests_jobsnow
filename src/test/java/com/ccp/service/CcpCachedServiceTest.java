package com.ccp.service;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;

public class CcpCachedServiceTest {

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

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void ctorFieldToCacheNullTest() {
		new CcpCachedService(null, new StubService(), 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorServiceNullTest() {
		new CcpCachedService(() -> "x", null, 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeNullTest() {
		new CcpCachedService(() -> "x", new StubService(), 10).execute(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// execute retorna put.content — nunca null naturalmente porque put sempre gera novo mapa.
}
