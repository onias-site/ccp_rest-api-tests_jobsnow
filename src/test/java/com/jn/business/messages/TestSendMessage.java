package com.jn.business.messages;

import com.jn.entities.JnEntityJobsnowError;

class TestSendMessage extends JnBusinessSendMessage {
	public TestSendMessage() {
		super(JnEntityJobsnowError.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}
}
