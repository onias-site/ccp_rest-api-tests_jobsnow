package com.jb.business.bots.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Keeps the e-mails that would go out through the provider instead of delivering them. The local double that the
 * other tests use writes the body to a file, which would serve to check by hand but not for the test to read
 * back the token sent to the user.
 */
class FakeEmailSender implements CcpEmailSender {

	final List<CcpJsonRepresentation> sentEmails = new ArrayList<>();

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {

		List<String> sentRecipients = Arrays.asList(recipients);

		CcpJsonRepresentation enviado = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.templateId, templateId)
				.put(JnJsonCommonsFields.subject, subject)
				.put(JnJsonCommonsFields.message, message)
				.put(JnJsonCommonsFields.email, sentRecipients);

		this.sentEmails.add(enviado);

		return CcpOtherConstants.EMPTY_JSON;
	}
}
