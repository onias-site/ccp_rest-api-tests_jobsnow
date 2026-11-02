package com.ccp.implementations.email.sendgrid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.email.CcpEmailProvider;
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
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpApacheMimeHttp.INSTANCE, CcpSendGridEmailProvider.INSTANCE);
	}

	private String send(CcpHttpContentType contentType) throws Exception {
		AtomicReference<String> received = new AtomicReference<>();
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/", new SendGridBodyKeepingHandler(received));
		server.start();
		try {
			String url = "http://localhost:" + server.getAddress().getPort() + "/v3/mail/send";
			CcpEmailProvider sender = CcpDependencyInjection.getDependency(CcpEmailProvider.class);
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

	private String sendWithHeaders(CcpJsonRepresentation emailHeaders) throws Exception {
		AtomicReference<String> received = new AtomicReference<>();
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/", new SendGridBodyKeepingHandler(received));
		server.start();
		try {
			String url = "http://localhost:" + server.getAddress().getPort() + "/v3/mail/send";
			CcpEmailProvider sender = CcpDependencyInjection.getDependency(CcpEmailProvider.class);
			sender.sendSimpleTextEmailMessage("token", url, "jnLoginToken", "noreply@ccpjobsnow.com", "subject", "<b>hi</b>", CcpHttpContentType.TEXT_HTML, emailHeaders, "someone@example.com");
			return received.get();
		} finally {
			server.stop(0);
		}
	}

	/** Without these headers, in the {@code headers} object of the body, the mailbox shows no unsubscribe button. */
	@Test
	public void theCustomHeadersGoInTheBody() throws Exception {
		String listUnsubscribe = "<https://api.example.com/unsubscribe?email=a%40b.com&token=abc>";
		CcpJsonRepresentation emailHeaders = new CcpJsonRepresentation("{}")
				.put(new CcpFieldName("List-Unsubscribe"), listUnsubscribe)
				.put(new CcpFieldName("List-Unsubscribe-Post"), "List-Unsubscribe=One-Click");

		String body = this.sendWithHeaders(emailHeaders);

		CcpJsonRepresentation bodyJson = new CcpJsonRepresentation(body);
		CcpJsonRepresentation sentHeaders = bodyJson.getInnerJson(new CcpFieldName("headers"));
		assertEquals(listUnsubscribe, sentHeaders.getAsString(new CcpFieldName("List-Unsubscribe")));
		assertEquals("List-Unsubscribe=One-Click", sentHeaders.getAsString(new CcpFieldName("List-Unsubscribe-Post")));
	}

	/** SendGrid gets no empty object: the other e-mails go out as before. */
	@Test
	public void noHeadersLeaveTheObjectOut() throws Exception {
		String body = this.send(CcpHttpContentType.TEXT_HTML);

		assertFalse(body, body.contains("\"headers\""));
	}

	@Test
	public void eachContentTypeHasItsMimeType() {
		assertEquals("text/html", CcpHttpContentType.TEXT_HTML.mimeType);
		assertEquals("text/plain", CcpHttpContentType.TEXT_PLAIN.mimeType);
	}
}
