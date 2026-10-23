package com.ccp.implementations.email.sendgrid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** The fake SendGrid of {@link SendGridContentTypeTest}: keeps the body it got and answers 202. */
class SendGridBodyKeepingHandler implements HttpHandler {

	/** Receives the body. */
	private final AtomicReference<String> received;

	SendGridBodyKeepingHandler(AtomicReference<String> received) {
		this.received = received;
	}

	public void handle(HttpExchange exchange) throws IOException {
		this.received.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
		exchange.sendResponseHeaders(202, -1);
		exchange.close();
	}
}
