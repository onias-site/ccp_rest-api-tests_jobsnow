package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;

/**
 * E-mail provider double that sends nothing, but keeps the recipients of each sending. It proves that the
 * decorator that sends to the user did (or did not) send the e-mail.
 */
public class CountingEmailSender implements CcpEmailSender {

	private static final List<String> sentRecipients = Collections.synchronizedList(new ArrayList<>());

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		List<String> asList = Arrays.asList(recipients);
		sentRecipients.addAll(asList);
		return CcpOtherConstants.EMPTY_JSON;
	}

	/** How many e-mails have already been addressed to the given recipient. */
	public static long howManyFor(String recipient) {
		synchronized (sentRecipients) {
			long howMany = sentRecipients.stream().filter(x -> recipient.equalsIgnoreCase(x)).count();
			return howMany;
		}
	}
}
