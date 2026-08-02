package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessNotifyContactUsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessNotifyContactUs.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnBusinessNotifyContactUs.INSTANCE.apply(null);
	}
}
