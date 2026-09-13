package com.jn.business.messages;

class TestSendMessage extends JnBusinessSendMessage {
	public TestSendMessage() {
		super(JnMessageSenderExceptionHandler.THROWS);
	}
	public JnMessageType[] getMessageTypes() {
		return new JnMessageType[] {JnMessageType.email};
	}

}
