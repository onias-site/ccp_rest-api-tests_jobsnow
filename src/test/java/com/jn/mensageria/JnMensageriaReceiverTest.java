package com.jn.mensageria;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnMensageriaReceiverTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnMensageriaReceiver.INSTANCE);
	}

	@Test
	public void getExecuteBulkOperationTest() {
		assertNotNull(JnMensageriaReceiver.INSTANCE.getExecuteBulkOperation());
	}

	@Test
	public void getFunctionToDeleteKeysInTheCacheTest() {
		assertNotNull(JnMensageriaReceiver.INSTANCE.getFunctionToDeleteKeysInTheCache());
	}

}
