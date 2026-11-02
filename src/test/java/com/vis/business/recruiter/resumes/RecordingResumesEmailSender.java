package com.vis.business.recruiter.resumes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.especifications.http.CcpHttpContentType;

/** E-mail sender of the tests that sends nothing and keeps the last message of each recipient, with its subject. */
public class RecordingResumesEmailSender implements CcpEmailProvider {
	/** The single instance: the class keeps no state. */
	public final static RecordingResumesEmailSender INSTANCE = new RecordingResumesEmailSender();

	private RecordingResumesEmailSender() {}

	/** The last "subject + message" of each recipient. */
	public static final Map<String, String> LAST_MESSAGE_BY_RECIPIENT = new LinkedHashMap<>();

	/** The custom headers of the last message of each recipient. */
	public static final Map<String, CcpJsonRepresentation> LAST_HEADERS_BY_RECIPIENT = new LinkedHashMap<>();

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		Arrays.stream(recipients).forEach(recipient -> LAST_MESSAGE_BY_RECIPIENT.put(recipient, subject + "\n" + message));
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, CcpJsonRepresentation emailHeaders, String... recipients) {
		Arrays.stream(recipients).forEach(recipient -> LAST_HEADERS_BY_RECIPIENT.put(recipient, emailHeaders));
		return this.sendSimpleTextEmailMessage(providerToken, providerUrl, templateId, sender, subject, message, contentType, recipients);
	}

	public List<CcpJsonRepresentation> getSuppressedEmails(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList) {
		return new ArrayList<>();
	}

	public List<CcpJsonRepresentation> getSuppressedEmailsCreatedSince(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList, long startTimeInSeconds) {
		return new ArrayList<>();
	}
}
