package com.jn.business.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.business.messages.JnBusinessSendUserToken;

public class JnBusinessSendUserTokenTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessSendUserToken.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnBusinessSendUserToken.INSTANCE.execute(null);
	}
}
