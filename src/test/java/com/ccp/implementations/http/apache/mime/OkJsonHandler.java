package com.ccp.implementations.http.apache.mime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** The fake server of {@link HttpClientSafetyTest}: reads the whole request body and answers 200 with {@code {"ok":true}}. */
class OkJsonHandler implements HttpHandler {

	/** The single instance. */
	public static final OkJsonHandler INSTANCE = new OkJsonHandler();

	private OkJsonHandler() {}

	public void handle(HttpExchange exchange) throws IOException {
		exchange.getRequestBody().readAllBytes();
		byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}
}
