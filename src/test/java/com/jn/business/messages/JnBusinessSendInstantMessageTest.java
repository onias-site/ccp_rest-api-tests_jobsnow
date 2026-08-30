package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessSendInstantMessageTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessSendInstantMessage.INSTANCE);
	}

	@Test
	public void getJsonValidationClassTest() {
		assertNotNull(JnBusinessSendInstantMessage.INSTANCE.getJsonValidationClass());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnBusinessSendInstantMessage.INSTANCE.execute(null);
	}

	// ── enum JnInstantMessageType ────────────────────────────────────────────

	@Test
	public void enumTextExistsTest() {
		assertNotNull(JnBusinessSendInstantMessage.JnInstantMessageType.text);
	}

	@Test
	public void enumFileExistsTest() {
		assertNotNull(JnBusinessSendInstantMessage.JnInstantMessageType.file);
	}

	@Test(expected = CcpNullParameterException.class)
	public void enumTextSendMessageJsonNullTest() {
		JnBusinessSendInstantMessage.JnInstantMessageType.text.sendMessage(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void enumTextSendMessageMessageNullTest() {
		JnBusinessSendInstantMessage.JnInstantMessageType.text.sendMessage(CcpOtherConstants.EMPTY_JSON,
				(com.ccp.decorators.CcpJsonRepresentation) null);
	}


	@Test(expected = CcpNullParameterException.class)
	public void enumTextApplyNullTest() {
		JnBusinessSendInstantMessage.JnInstantMessageType.text.execute(null);
	}

	@Test
	public void enumGetJsonValidationClassTest() {
		assertNotNull(JnBusinessSendInstantMessage.JnInstantMessageType.text.getJsonValidationClass());
		assertNotNull(JnBusinessSendInstantMessage.JnInstantMessageType.file.getJsonValidationClass());
	}
}
