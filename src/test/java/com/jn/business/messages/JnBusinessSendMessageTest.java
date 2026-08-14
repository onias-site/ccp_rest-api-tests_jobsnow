package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

public class JnBusinessSendMessageTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static class TestSendMessage extends JnBusinessSendMessage {
		public TestSendMessage() {
			super(JnEntityJobsnowError.ENTITY);
		}
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
