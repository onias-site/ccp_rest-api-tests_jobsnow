package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.business.messages.JnMessages.JnBusinessNotifyError;

public class JnBusinessNotifyErrorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessNotifyError.instance);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyJsonNullTest() {
		JnBusinessNotifyError.instance.execute((com.ccp.decorators.CcpJsonRepresentation) null);
	}
}
