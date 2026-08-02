package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.messages.JnSendMessageToUser;

public class JnBusinessNotifySupportTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessNotifySupport.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyJsonNullTest() {
		JnBusinessNotifySupport.INSTANCE.apply(null, "topic", JnEntityJobsnowError.ENTITY, new JnSendMessageToUser());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyTopicNullTest() {
		JnBusinessNotifySupport.INSTANCE.apply(CcpOtherConstants.EMPTY_JSON, null, JnEntityJobsnowError.ENTITY, new JnSendMessageToUser());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyEntityNullTest() {
		JnBusinessNotifySupport.INSTANCE.apply(CcpOtherConstants.EMPTY_JSON, "topic", (CcpEntity) null, new JnSendMessageToUser());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applySenderNullTest() {
		JnBusinessNotifySupport.INSTANCE.apply(CcpOtherConstants.EMPTY_JSON, "topic", JnEntityJobsnowError.ENTITY, null);
	}
}
