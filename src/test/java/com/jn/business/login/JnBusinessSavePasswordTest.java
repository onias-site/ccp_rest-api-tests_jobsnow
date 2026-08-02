package com.jn.business.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessSavePasswordTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessSavePassword.INSTANCE);
	}

	@Test
	public void getJsonValidationClassTest() {
		assertNotNull(JnBusinessSavePassword.INSTANCE.getJsonValidationClass());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnBusinessSavePassword.INSTANCE.apply(null);
	}
}
