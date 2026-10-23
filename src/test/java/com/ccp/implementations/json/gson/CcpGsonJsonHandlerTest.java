package com.ccp.implementations.json.gson;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.json.CcpJsonHandler;

public class CcpGsonJsonHandlerTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static CcpJsonHandler getHandler() {
		return CcpDependencyInjection.getDependency(CcpJsonHandler.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void constructorProviderTest() {
		assertNotNull(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void getInstanceTest() {
		CcpJsonHandler instance = CcpGsonJsonHandler.INSTANCE.getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void toJsonNullTest() {
		getHandler().toJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void asPrettyJsonNullTest() {
		getHandler().asPrettyJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void fromJsonNullTest() {
		getHandler().fromJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isValidJsonNullTest() {
		getHandler().isValidJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isValidJsonListNullTest() {
		getHandler().isValidJsonList(null);
	}
}
