package com.jb.business.bots.engine;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Keeps the messages that would go out to Telegram instead of delivering them to the real bot. The token of the
 * support bot in the environment properties is real, so without this double the test would send a message to the
 * phone of whoever handles the support on every run.
 */
class FakeInstantMessenger implements CcpInstantMessenger {

	enum Fields implements CcpJsonFieldName {
		fileContent,
	}

	final List<CcpJsonRepresentation> sentMessages = new ArrayList<>();

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		this.store(botType, chatId, message);
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
		this.store(botType, chatId, caption);
		// the content of the file (the stack trace of an error) helps to diagnose a failing test
		byte[] bytes = new byte[fileContent.length];
		for (int index = 0; index < bytes.length; index++) {
			bytes[index] = fileContent[index];
		}
		String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
		int last = this.sentMessages.size() - 1;
		CcpJsonRepresentation stored = this.sentMessages.get(last);
		CcpJsonRepresentation storedWithTheFile = stored.put(Fields.fileContent, content);
		this.sentMessages.set(last, storedWithTheFile);
		return CcpOtherConstants.EMPTY_JSON;
	}

	private void store(CcpJsonFieldName botType, Long chatId, String message) {
		String botName = botType.name();
		CcpJsonRepresentation enviada = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, botName)
				.put(JnJsonInstantMessengerFields.chatId, chatId)
				.put(JnJsonCommonsFields.message, message);
		this.sentMessages.add(enviada);
	}

	/**
	 * Returns the last message sent to the given chat. The flow sends more than one message to the support (the one
	 * of the pending ticket, while setting up the scenario, and the one of the handled ticket), so the test asks for
	 * the last one and not for the only one.
	 */
	CcpJsonRepresentation lastMessageFor(Long chatId) {
		for (int indice = this.sentMessages.size() - 1; indice >= 0; indice--) {
			CcpJsonRepresentation enviada = this.sentMessages.get(indice);
			Long recipient = enviada.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			boolean isAnotherChat = false == recipient.equals(chatId);
			if (isAnotherChat) {
				continue;
			}
			return enviada;
		}
		return CcpOtherConstants.EMPTY_JSON;
	}
}
