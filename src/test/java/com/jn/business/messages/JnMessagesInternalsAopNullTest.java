package com.jn.business.messages;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.business.messages.JnBusinessSendInstantMessage.JnInstantMessageType;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os membros de visibilidade restrita do pacote
 * de mensagens do jn: o construtor de {@code JnBusinessSendMessage} e
 * {@code JnInstantMessageType.getMessage}.
 */
public class JnMessagesInternalsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	@Test(expected = CcpNullParameterException.class)
	public void sendMessageConstrutorNullTest() {
		new JnBusinessSendMessage(null);
	}

	@Test
	public void sendMessageConstrutorTest() {
		org.junit.Assert.assertNotNull(new JnBusinessSendMessage(JnEntityJobsnowError.ENTITY));
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMessageJsonNullTest() {
		JnInstantMessageType.text.getMessage(null, JSON, JnInstantMessageType.text);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMessageOrElseThrowNullTest() {
		JnInstantMessageType.text.getMessage(JSON, null, JnInstantMessageType.text);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMessageFieldNullTest() {
		JnInstantMessageType.text.getMessage(JSON, JSON, null);
	}
}
