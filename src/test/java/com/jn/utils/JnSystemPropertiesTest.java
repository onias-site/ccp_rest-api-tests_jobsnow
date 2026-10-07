package com.jn.utils;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonFieldName;
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

	// ── maxAttempts ───────────────────────────────────────────────────────────

	@Test
	public void maxAttemptsIsThreeWhenNotConfiguredTest() {
		org.junit.Assert.assertEquals(3, JnSystemProperties.maxAttempts(com.ccp.constants.CcpOtherConstants.EMPTY_JSON));
	}

	/** A property comes as text; until 2026-10-06 it was cast to Integer and a configured value broke the login. */
	@Test
	public void maxAttemptsConfiguredAsTextIsReadAsNumberTest() {
		com.ccp.decorators.CcpJsonRepresentation properties = com.ccp.constants.CcpOtherConstants.EMPTY_JSON.put(Fields.maxAttempts, "5");

		org.junit.Assert.assertEquals(5, JnSystemProperties.maxAttempts(properties));
	}
}
