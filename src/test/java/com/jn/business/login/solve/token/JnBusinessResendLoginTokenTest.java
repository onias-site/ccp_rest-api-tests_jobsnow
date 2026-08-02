package com.jn.business.login.solve.token;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessResendLoginTokenTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessResendLoginToken.INSTANCE);
	}

	@Test
	public void getJsonValidationClassTest() {
		assertNotNull(JnBusinessResendLoginToken.INSTANCE.getJsonValidationClass());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnBusinessResendLoginToken.INSTANCE.apply(null);
	}
}
