package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessNotifyErrorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessNotifyError.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyJsonNullTest() {
		JnBusinessNotifyError.INSTANCE.execute((com.ccp.decorators.CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyThrowableNullTest() {
		JnBusinessNotifyError.INSTANCE.apply((Throwable) null);
	}
}
