package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class JnBusinessSendMessageTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}



	@Test
	public void construtorTest() {
		assertNotNull(new TestSendMessage());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new TestSendMessage().execute(null);
	}
}
