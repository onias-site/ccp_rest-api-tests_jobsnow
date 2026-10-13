package com.vis.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;

/** E-mail sender of the tests that sends nothing and keeps the recipients and the subject of every e-mail. */
public class RecordingEmailSender implements CcpEmailSender {

	/** Every e-mail "sent", as "recipients | subject", in order. */
	public static final List<String> SENT = new ArrayList<>();

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		SENT.add(Arrays.toString(recipients) + " | " + subject);
		return CcpOtherConstants.EMPTY_JSON;
	}
}
