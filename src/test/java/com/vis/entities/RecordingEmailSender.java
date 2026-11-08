package com.vis.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.especifications.http.CcpHttpContentType;

/** E-mail sender of the tests that sends nothing and keeps the recipients and the subject of every e-mail. */
public class RecordingEmailSender implements CcpEmailProvider {
	/** The single instance: the class keeps no state. */
	public final static RecordingEmailSender INSTANCE = new RecordingEmailSender();

	private RecordingEmailSender() {}

	/** Every e-mail "sent", as "recipients | subject", in order. */
	public static final List<String> SENT = new ArrayList<>();

	/** The body of every e-mail "sent", as "recipients | message", in the order of {@link #SENT}. */
	public static final List<String> MESSAGES = new ArrayList<>();

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		SENT.add(Arrays.toString(recipients) + " | " + subject);
		MESSAGES.add(Arrays.toString(recipients) + " | " + message);
		return CcpOtherConstants.EMPTY_JSON;
	}

	public List<CcpJsonRepresentation> getSuppressedEmails(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList) {
		return new ArrayList<>();
	}

	public List<CcpJsonRepresentation> getSuppressedEmailsCreatedSince(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList, long startTimeInSeconds) {
		return new ArrayList<>();
	}
}
