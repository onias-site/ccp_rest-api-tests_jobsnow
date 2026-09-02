package com.jn.business.http;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import org.junit.Test;

public class JnBusinessSendHttpRequestTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}



	@Test
	public void construtorTest() {
		assertNotNull(new JnBusinessSendHttpRequest(new NoopExecutor(), JnMessageSenderExceptionHandler.THROWS));
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorProcessNullTest() {
		new JnBusinessSendHttpRequest(null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new JnBusinessSendHttpRequest(new NoopExecutor(), JnMessageSenderExceptionHandler.THROWS).execute(null);
	}
}
