package com.ccp.especifications.db.utils.entity.decorators.engine;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;

/**
 * Swallows the emails. Saving the login token triggers a message to the user, and what this test
 * measures is the state of both indexes after {@code deleteAnyWhere}, not the message content.
 */
class SilentEmailSender implements CcpEmailSender {

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		return CcpOtherConstants.EMPTY_JSON;
	}
}
