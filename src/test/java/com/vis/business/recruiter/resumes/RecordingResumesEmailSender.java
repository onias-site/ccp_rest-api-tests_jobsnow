package com.vis.business.recruiter.resumes;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;

/** E-mail sender of the tests that sends nothing and keeps the last message of each recipient, with its subject. */
public class RecordingResumesEmailSender implements CcpEmailSender {

	/** The last "subject + message" of each recipient. */
	public static final Map<String, String> LAST_MESSAGE_BY_RECIPIENT = new LinkedHashMap<>();

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		Arrays.stream(recipients).forEach(recipient -> LAST_MESSAGE_BY_RECIPIENT.put(recipient, subject + "\n" + message));
		return CcpOtherConstants.EMPTY_JSON;
	}
}
