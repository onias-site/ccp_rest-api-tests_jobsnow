package com.jn.mensageria;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class JnBusinessSendToMensageriaTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}



	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaNullTest() {
		new NoopSender().sendToMensageria(null);
	}
}
