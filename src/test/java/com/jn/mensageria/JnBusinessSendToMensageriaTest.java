package com.jn.mensageria;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessSendToMensageriaTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static class NoopSender implements JnBusinessSendToMensageria {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaNullTest() {
		new NoopSender().sendToMensageria(null);
	}
}
