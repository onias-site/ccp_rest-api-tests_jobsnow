package com.ccp.especifications.db.utils.entity.decorators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.especifications.http.CcpHttpContentType;

/**
 * E-mail provider double that sends nothing, but keeps the recipients of each sending. It proves that the
 * decorator that sends to the user did (or did not) send the e-mail.
 */
public class CountingEmailSender implements CcpEmailProvider {
	/** The single instance: the class keeps no state. */
	public final static CountingEmailSender INSTANCE = new CountingEmailSender();

	private CountingEmailSender() {}

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

	public List<CcpJsonRepresentation> getSuppressedEmails(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList) {
		return new ArrayList<>();
	}

	public List<CcpJsonRepresentation> getSuppressedEmailsCreatedSince(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList, long startTimeInSeconds) {
		return new ArrayList<>();
	}
}
