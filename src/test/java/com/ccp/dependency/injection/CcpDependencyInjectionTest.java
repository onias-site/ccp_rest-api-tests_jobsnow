package com.ccp.dependency.injection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.especifications.json.CcpJsonHandler;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpDependencyInjectionTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void loadAndGetDependencyTest() {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
		assertTrue(CcpDependencyInjection.hasDependency(CcpJsonHandler.class));
		assertNotNull(CcpDependencyInjection.getDependency(CcpJsonHandler.class));
	}

	// Note: replaceDependenciesTemporally requires the CcpInstanceProvider interface
	// to be already registered as a dependency, which is not the case in isolated tests.
	// There is no trivial positive test without more elaborate infrastructure.

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void replaceDependenciesTemporallyJsonNullTest() {
		CcpDependencyInjection.replaceDependenciesTemporally(null, j -> j, new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void replaceDependenciesTemporallyBusinessNullTest() {
		CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, null, new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void replaceDependenciesTemporallyProvidersNullTest() {
		CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, j -> j, (CcpInstanceProvider<?>[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void loadAllDependenciesNullTest() {
		CcpDependencyInjection.loadAllDependencies((CcpInstanceProvider<?>[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void hasDependencyNullTest() {
		CcpDependencyInjection.hasDependency(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getDependencyNullTest() {
		CcpDependencyInjection.getDependency(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void removeDependecyNullTest() {
		CcpDependencyInjection.removeDependecy(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getInstanceNullTest() {
		CcpDependencyInjection.getInstance(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// Note: getDependency throws an exception when nothing is found (never returns null).
	// hasDependency returns a primitive boolean.
	// replaceDependenciesTemporally and getInstance depend on valid arguments.
}
