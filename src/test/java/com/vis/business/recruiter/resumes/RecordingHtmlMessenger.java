package com.vis.business.recruiter.resumes;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * An instant messenger that only records what would be sent: each html message as "chat | replyTo | text", answering an
 * increasing id of message, as Telegram does.
 */
public class RecordingHtmlMessenger implements CcpInstantMessenger {

	/** The single instance. */
	public static final RecordingHtmlMessenger INSTANCE = new RecordingHtmlMessenger();

	/** Every html message "sent", as "chat | replyTo | text", in order. */
	public static final List<String> SENT = new ArrayList<>();

	private long lastMessageId = 1000;

	private RecordingHtmlMessenger() {}

	public CcpJsonRepresentation sendHtmlMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		SENT.add(chatId + " | " + replyTo + " | " + message);
		this.lastMessageId++;
		return CcpOtherConstants.EMPTY_JSON.put(CcpJsonCommonsFields.replyTo, this.lastMessageId);
	}

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
		return CcpOtherConstants.EMPTY_JSON;
	}
}
