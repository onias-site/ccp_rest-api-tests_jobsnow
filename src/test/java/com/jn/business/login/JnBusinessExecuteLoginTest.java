package com.jn.business.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessExecuteLoginTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessExecuteLogin.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnBusinessExecuteLogin.INSTANCE.execute(null);
	}
}
 