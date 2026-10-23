package com.jn.mensageria;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class JnBusinessSendToMensageriaTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}



	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaNullTest() {
		NoopSender.INSTANCE.sendToMensageria(null);
	}
}
