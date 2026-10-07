package com.ccp.implementations.email.sendgrid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Proves that the e-mail goes to SendGrid with a MIME type in {@code content.type} ({@code text/html},
 * {@code text/plain}): until 2026-10-06 it went with the enum constant name ({@code TEXT_HTML}), which SendGrid refuses.
 * SendGrid is replaced by a local server that answers 202 and keeps the body it got.
 */
public class SendGridContentTypeTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpApacheMimeHttp(), new CcpSendGridEmailSender());
	}

	private String send(CcpHttpContentType contentType) throws Exception {
		AtomicReference<String> received = new AtomicReference<>();
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/", exchange -> {
			received.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			exchange.sendResponseHeaders(202, -1);
			exchange.close();
		});
		server.start();
		try {
			String url = "http://localhost:" + server.getAddress().getPort() + "/v3/mail/send";
			CcpEmailSender sender = CcpDependencyInjection.getDependency(CcpEmailSender.class);
			sender.sendSimpleTextEmailMessage("token", url, "jnLoginToken", "noreply@ccpjobsnow.com", "subject", "<b>hi</b>", contentType, "someone@example.com");
			return received.get();
		} finally {
			server.stop(0);
		}
	}

	@Test
	public void anHtmlEmailGoesWithTheTextHtmlMimeType() throws Exception {
		String body = this.send(CcpHttpContentType.TEXT_HTML);

		assertTrue(body, body.contains("\"type\":\"text/html\""));
		assertFalse(body, body.contains("TEXT_HTML"));
	}

	@Test
	public void aPlainTextEmailGoesWithTheTextPlainMimeType() throws Exception {
		String body = this.send(CcpHttpContentType.TEXT_PLAIN);

		assertTrue(body, body.contains("\"type\":\"text/plain\""));
	}

	/** The template id is the key of the system's own template, already resolved into the message: it must not reach SendGrid. */
	@Test
	public void theInternalTemplateIdIsNotSentToSendGrid() throws Exception {
		String body = this.send(CcpHttpContentType.TEXT_HTML);

		assertFalse(body, body.contains("jnLoginToken"));
		assertFalse(body, body.contains("template_id"));
	}

	@Test
	public void eachContentTypeHasItsMimeType() {
		assertEquals("text/html", CcpHttpContentType.TEXT_HTML.mimeType);
		assertEquals("text/plain", CcpHttpContentType.TEXT_PLAIN.mimeType);
	}
}
