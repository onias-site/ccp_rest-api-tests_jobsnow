package com.ccp.especifications.db.utils.entity.decorators.engine;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;

/**
 * Swallows the bot messages. Saving some twin entities notifies support, and the bot token in the
 * environment properties is real — without this test double the test would send a real message on
 * every run.
 */
class SilentInstantMessenger implements CcpInstantMessenger {

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
		return CcpOtherConstants.EMPTY_JSON;
	}
}
