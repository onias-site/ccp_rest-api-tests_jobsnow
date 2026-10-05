package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;

/**
 * Telegram double that sends nothing, but keeps the text of each message. It is with it that the tests of the
 * send-to-user decorators prove that the message went out, or that it did not, in each operation. <p>The list is
 * static because the dependency injection instantiates the double by itself.
 */
public class CountingInstantMessenger implements CcpInstantMessenger {

	private static final List<String> sentMessages = Collections.synchronizedList(new ArrayList<>());

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		sentMessages.add(message);
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
		sentMessages.add(caption);
		return CcpOtherConstants.EMPTY_JSON;
	}

	/** How many messages already sent mention the given text. */
	public static long howManyMention(String text) {
		synchronized (sentMessages) {
			long howMany = sentMessages.stream().filter(x -> x != null && x.contains(text)).count();
			return howMany;
		}
	}

	public static int total() {
		int total = sentMessages.size();
		return total;
	}
}
