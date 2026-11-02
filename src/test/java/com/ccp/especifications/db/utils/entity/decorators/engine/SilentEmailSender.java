package com.ccp.especifications.db.utils.entity.decorators.engine;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.especifications.http.CcpHttpContentType;

/**
 * Swallows the emails. Saving the login token triggers a message to the user, and what this test
 * measures is the state of both indexes after {@code deleteAnyWhere}, not the message content.
 */
public class SilentEmailSender implements CcpEmailProvider {
	/** The single instance: the class keeps no state. */
	public final static SilentEmailSender INSTANCE = new SilentEmailSender();

	private SilentEmailSender() {}

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		return CcpOtherConstants.EMPTY_JSON;
	}

	public List<CcpJsonRepresentation> getSuppressedEmails(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList) {
		return new ArrayList<>();
	}

	public List<CcpJsonRepresentation> getSuppressedEmailsCreatedSince(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList, long startTimeInSeconds) {
		return new ArrayList<>();
	}
}
