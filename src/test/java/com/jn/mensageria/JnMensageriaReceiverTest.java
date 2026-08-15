package com.jn.mensageria;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.business.messages.JnMessages.JnBusinessNotifyError;
import com.jn.entities.JnEntityAsyncTask;

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

	@Test(expected = CcpNullParameterException.class)
	public void executeProcessEntityNullTest() {
		JnMensageriaReceiver.INSTANCE.executeProcess((CcpEntity) null, "topic",
				CcpOtherConstants.EMPTY_JSON, JnBusinessNotifyError.instance);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeProcessProcessNameNullTest() {
		JnMensageriaReceiver.INSTANCE.executeProcess(JnEntityAsyncTask.ENTITY, null,
				CcpOtherConstants.EMPTY_JSON, JnBusinessNotifyError.instance);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeProcessJsonNullTest() {
		JnMensageriaReceiver.INSTANCE.executeProcess(JnEntityAsyncTask.ENTITY, "topic",
				null, JnBusinessNotifyError.instance);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeProcessNotifyErrorNullTest() {
		JnMensageriaReceiver.INSTANCE.executeProcess(JnEntityAsyncTask.ENTITY, "topic",
				CcpOtherConstants.EMPTY_JSON, null);
	}
}
