package com.js.business.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JsBusinessSavePasswordTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JsBusinessSavePassword.INSTANCE);
	}

	@Test
	public void getJsonValidationClassTest() {
		assertNotNull(JsBusinessSavePassword.INSTANCE.getJsonValidationClass());
	}
 
	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JsBusinessSavePassword.INSTANCE.execute(null);
	}
}
