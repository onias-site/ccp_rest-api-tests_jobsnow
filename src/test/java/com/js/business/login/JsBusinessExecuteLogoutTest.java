package com.js.business.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JsBusinessExecuteLogoutTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JsBusinessExecuteLogout.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() { 
		JsBusinessExecuteLogout.INSTANCE.execute(null);
	}
}
