package com.vis.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.especifications.http.CcpHttpContentType;

/** E-mail provider of the tests that answers the lists the test gave it, keeps what was asked and fails on demand. */
public class FixedSuppressionsEmailProvider implements CcpEmailProvider {
	/** The single instance: the tests clear its state. */
	public final static FixedSuppressionsEmailProvider INSTANCE = new FixedSuppressionsEmailProvider();

	private FixedSuppressionsEmailProvider() {}

	/** The records of each list. */
	public final Map<CcpEmailSuppressionList, List<CcpJsonRepresentation>> lists = new HashMap<>();

	/** The moment of every reading, in order. */
	public final List<Long> startTimes = new ArrayList<>();

	/** Everyone who received an e-mail, in order. */
	public final List<String> recipients = new ArrayList<>();

	/** The failure of the readings, when there is one. */
	public RuntimeException failure;

	/** Forgets the lists, the readings, the recipients and the failure. */
	public void clear() {
		this.lists.clear();
		this.startTimes.clear();
		this.recipients.clear();
		this.failure = null;
	}

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		this.recipients.addAll(List.of(recipients));
		return CcpOtherConstants.EMPTY_JSON;
	}

	public List<CcpJsonRepresentation> getSuppressedEmails(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList) {
		return this.getSuppressedEmailsCreatedSince(providerToken, providerUrl, suppressionList, 0L);
	}

	public List<CcpJsonRepresentation> getSuppressedEmailsCreatedSince(String providerToken, String providerUrl, CcpEmailSuppressionList suppressionList, long startTimeInSeconds) {
		if(this.failure != null) {
			throw this.failure;
		}
		this.startTimes.add(startTimeInSeconds);
		List<CcpJsonRepresentation> records = this.lists.getOrDefault(suppressionList, new ArrayList<>());
		return records;
	}
}
