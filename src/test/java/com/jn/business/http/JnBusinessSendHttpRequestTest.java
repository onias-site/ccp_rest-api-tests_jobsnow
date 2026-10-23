package com.jn.business.http;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import org.junit.Test;

public class JnBusinessSendHttpRequestTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}



	@Test
	public void constructorTest() {
		assertNotNull(new JnBusinessSendHttpRequest(NoopExecutor.INSTANCE, JnMessageSenderExceptionHandler.THROWS));
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorProcessNullTest() {
		new JnBusinessSendHttpRequest(null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new JnBusinessSendHttpRequest(NoopExecutor.INSTANCE, JnMessageSenderExceptionHandler.THROWS).execute(null);
	}
}
