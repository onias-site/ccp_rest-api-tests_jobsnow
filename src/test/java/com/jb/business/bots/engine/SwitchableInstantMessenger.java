package com.jb.business.bots.engine;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/** Keeps the messages like {@link FakeInstantMessenger} and, when told to, fails like a bot that breaks in a step. */
class SwitchableInstantMessenger extends FakeInstantMessenger {

	/** Whether the next messages fail. */
	boolean failing = false;

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		if (this.failing) {
			throw new IllegalStateException("the bot failed in the step");
		}
		return super.sendTextMessage(botType, botToken, chatId, replyTo, message);
	}
}
