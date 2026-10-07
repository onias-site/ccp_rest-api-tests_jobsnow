package com.ccp.implementations.instant.messenger.telegram;

import java.util.ArrayList;
import java.util.List;

import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;

/**
 * Telegram replaced in the tests: keeps the text parts of the last multipart request and answers as a successful send.
 */
class CapturingHttpRequester implements CcpHttpRequester {

	private static final String SENT_MESSAGE = "{\"ok\":true,\"result\":{\"message_id\":10}}";

	final List<CcpHttpBodyText> lastTexts = new ArrayList<>();

	public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
		CcpHttpResponse response = new CcpHttpResponse(SENT_MESSAGE, 200, "");
		return response;
	}

	public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
		this.lastTexts.clear();
		this.lastTexts.addAll(bodyTexts);
		CcpHttpResponse response = this.executeHttpRequest(url, method, headers, "");
		return response;
	}

	/** Returns the text part with the given name, or {@code null} when the request had none. */
	@CcpAllowNullReturn
	String getText(String name) {
		for (CcpHttpBodyText text : this.lastTexts) {
			boolean isTheText = text.name.equals(name);
			if (isTheText) {
				return text.text;
			}
		}
		return null;
	}
}
