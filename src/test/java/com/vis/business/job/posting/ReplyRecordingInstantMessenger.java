package com.vis.business.job.posting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Keeps the messages that would go out to Telegram, with the message each one answers ({@code replyTo}), instead of
 * delivering them: the token of the user bot in the environment properties is real.
 */
class ReplyRecordingInstantMessenger implements CcpInstantMessenger {

	final List<CcpJsonRepresentation> sentMessages = Collections.synchronizedList(new ArrayList<>());

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		String botName = botType.name();
		CcpJsonRepresentation sent = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, botName)
				.put(JnJsonInstantMessengerFields.chatId, chatId)
				.put(CcpJsonCommonsFields.replyTo, replyTo)
				.put(JnJsonCommonsFields.message, message);
		this.sentMessages.add(sent);
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
		return this.sendTextMessage(botType, botToken, chatId, replyTo, caption);
	}

	/**
	 * The messages sent to a chat.
	 * @param chatId the chat
	 * @return the messages, in the order they were sent
	 */
	List<CcpJsonRepresentation> sentTo(Long chatId) {
		List<CcpJsonRepresentation> messages = new ArrayList<>();
		for (CcpJsonRepresentation sent : this.sentMessages) {
			Long recipient = sent.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			boolean toTheChat = recipient.equals(chatId);
			if (toTheChat) {
				messages.add(sent);
			}
		}
		return messages;
	}
}
