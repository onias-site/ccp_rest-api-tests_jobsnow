package com.ccp.implementations.email.sendgrid;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * The fake SendGrid of {@link SendGridSuppressionListsTest}: serves a list with the given number of records,
 * honoring {@code limit} and {@code offset}, and keeps what it received. From the given request on, it answers 429.
 */
class SendGridSuppressionListHandler implements HttpHandler {

	/** How many records the list has. */
	private final int totalRecords;
	/** The first request (from 1) refused with 429. */
	private final int firstRefusedRequest;
	/** The URIs received, in order. */
	final List<String> requestedUris = new ArrayList<>();
	/** The authorization headers received, in order. */
	final List<String> authorizations = new ArrayList<>();

	SendGridSuppressionListHandler(int totalRecords, int firstRefusedRequest) {
		this.totalRecords = totalRecords;
		this.firstRefusedRequest = firstRefusedRequest;
	}

	public void handle(HttpExchange exchange) throws IOException {
		URI requestUri = exchange.getRequestURI();
		String requestedUri = requestUri.toString();
		this.requestedUris.add(requestedUri);
		String authorization = exchange.getRequestHeaders().getFirst("Authorization");
		this.authorizations.add(authorization);
		int requestNumber = this.requestedUris.size();
		boolean refused = requestNumber >= this.firstRefusedRequest;
		int status = refused ? 429 : 200;
		String body = refused ? "{\"errors\":[{\"message\":\"too many requests\"}]}" : this.getPage(requestedUri);
		byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, bodyBytes.length);
		exchange.getResponseBody().write(bodyBytes);
		exchange.close();
	}

	private String getPage(String requestedUri) {
		int limit = this.getParameter(requestedUri, "limit");
		int offset = this.getParameter(requestedUri, "offset");
		int end = Math.min(offset + limit, this.totalRecords);
		StringJoiner page = new StringJoiner(",", "[", "]");
		for (int recordNumber = offset; recordNumber < end; recordNumber++) {
			page.add("{\"created\":1700000000,\"email\":\"user" + recordNumber + "@example.com\",\"reason\":\"550\",\"status\":\"5.1.1\"}");
		}
		return page.toString();
	}

	private int getParameter(String requestedUri, String parameterName) {
		Pattern pattern = Pattern.compile("[?&]" + parameterName + "=(\\d+)");
		Matcher matcher = pattern.matcher(requestedUri);
		matcher.find();
		String value = matcher.group(1);
		int parameter = Integer.parseInt(value);
		return parameter;
	}
}
