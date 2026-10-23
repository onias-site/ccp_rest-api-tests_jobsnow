package com.js.business.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JsBusinessExecuteLoginTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JsBusinessExecuteLogin.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JsBusinessExecuteLogin.INSTANCE.execute(null);
	}
}
 