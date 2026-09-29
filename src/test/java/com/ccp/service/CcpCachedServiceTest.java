package com.ccp.service;

import com.ccp.aop.CcpNullParameterException;
import org.junit.Test;

public class CcpCachedServiceTest {



	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void ctorFieldToCacheNullTest() {
		new CcpCachedService(null, new CcpCachedServiceTestStubService(), 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorServiceNullTest() {
		new CcpCachedService(() -> "x", null, 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeNullTest() {
		new CcpCachedService(() -> "x", new CcpCachedServiceTestStubService(), 10).execute(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// execute returns put.content — naturally never null because put always creates a new map.
}
