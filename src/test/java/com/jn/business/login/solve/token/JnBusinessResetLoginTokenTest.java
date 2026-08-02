package com.jn.business.login.solve.token;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessResetLoginTokenTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessResetLoginToken.INSTANCE);
	}

	@Test
	public void getJsonValidationClassTest() {
		assertNotNull(JnBusinessResetLoginToken.INSTANCE.getJsonValidationClass());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnBusinessResetLoginToken.INSTANCE.apply(null);
	}
}
