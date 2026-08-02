package com.jn.utils;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnSystemPropertiesTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getSystemPropertyEnumNullTest() {
		JnSystemProperties.INSTANCE.getSystemProperty((CcpJsonFieldName) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getSystemPropertyStringNullTest() {
		JnSystemProperties.INSTANCE.getSystemProperty((String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getSystemInnerPropertyNullTest() {
		JnSystemProperties.INSTANCE.getSystemInnerProperty((CcpJsonFieldName[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getSystemInnerJsonNullTest() {
		JnSystemProperties.INSTANCE.getSystemInnerJson((String[]) null);
	}
}
