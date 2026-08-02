package com.ccp.implementations.password.mindrot;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.password.CcpPasswordHandler;

public class CcpMindrotPasswordHandlerTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpMindrotPasswordHandler());
	}

	private static CcpPasswordHandler getHandler() {
		return CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpMindrotPasswordHandler());
	}

	@Test
	public void getInstanceTest() {
		CcpPasswordHandler instance = new CcpMindrotPasswordHandler().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void matchesPasswordNullTest() {
		getHandler().matches(null, "hash");
	}

	@Test(expected = CcpNullParameterException.class)
	public void matchesHashNullTest() {
		getHandler().matches("password", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getHashPasswordNullTest() {
		getHandler().getHash(null);
	}
}
