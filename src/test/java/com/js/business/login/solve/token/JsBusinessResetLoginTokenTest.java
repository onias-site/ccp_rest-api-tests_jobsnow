package com.js.business.login.solve.token;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JsBusinessResetLoginTokenTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JsBusinessResetLoginToken.INSTANCE);
	}

	@Test
	public void getJsonValidationClassTest() {
		assertNotNull(JsBusinessResetLoginToken.INSTANCE.getJsonValidationClass());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JsBusinessResetLoginToken.INSTANCE.execute(null);
	}
}
