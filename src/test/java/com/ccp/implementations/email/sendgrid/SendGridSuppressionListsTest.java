package com.ccp.implementations.email.sendgrid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Proves the reading of the SendGrid suppression lists against a local server that paginates as the documentation says:
 * the legacy code asked for no page and got only the first one.
 */
public class SendGridSuppressionListsTest {

	/** Never refuses a request. */
	private static final int NEVER = Integer.MAX_VALUE;

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpApacheMimeHttp.INSTANCE, CcpSendGridEmailProvider.INSTANCE);
	}

	private HttpServer server;

	private String start(SendGridSuppressionListHandler handler) throws IOException {
		this.server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		this.server.createContext("/", handler);
		this.server.start();
		int port = this.server.getAddress().getPort();
		return "http://localhost:" + port + "/v3/suppression";
	}

	@After
	public void stop() {
		this.server.stop(0);
	}

	private static CcpEmailProvider getReader() {
		return CcpDependencyInjection.getDependency(CcpEmailProvider.class);
	}

	@Test
	public void aListLargerThanOnePageIsReadToTheEnd() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(1203, NEVER);
		String url = this.start(handler);

		List<CcpJsonRepresentation> records = getReader().getSuppressedEmails("token", url, CcpEmailSuppressionList.bounces);

		assertEquals(1203, records.size());
		CcpFieldName email = new CcpFieldName("email");
		Set<String> emails = records.stream().map(x -> x.getAsString(email)).collect(Collectors.toSet());
		assertEquals(1203, emails.size());
		List<String> expected = Arrays.asList(
				"/v3/suppression/bounces?limit=500&offset=0",
				"/v3/suppression/bounces?limit=500&offset=500",
				"/v3/suppression/bounces?limit=500&offset=1000");
		assertEquals(expected, handler.requestedUris);
	}

	@Test
	public void aListThatFillsTheLastPageEndsWithAnEmptyPage() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(500, NEVER);
		String url = this.start(handler);

		List<CcpJsonRepresentation> records = getReader().getSuppressedEmails("token", url, CcpEmailSuppressionList.blocks);

		assertEquals(500, records.size());
		assertEquals(2, handler.requestedUris.size());
	}

	@Test
	public void anEmptyListTakesOneRequest() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(0, NEVER);
		String url = this.start(handler);

		List<CcpJsonRepresentation> records = getReader().getSuppressedEmails("token", url, CcpEmailSuppressionList.blocks);

		assertTrue(records.isEmpty());
		assertEquals(1, handler.requestedUris.size());
	}

	/** The paths of the v3 API: the v2 names ({@code spamreports}) do not exist there. */
	@Test
	public void eachListGoesToItsOwnPath() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(0, NEVER);
		String url = this.start(handler);

		for (CcpEmailSuppressionList suppressionList : CcpEmailSuppressionList.values()) {
			getReader().getSuppressedEmails("token", url, suppressionList);
		}

		List<String> expected = Arrays.asList(
				"/v3/suppression/bounces?limit=500&offset=0",
				"/v3/suppression/blocks?limit=500&offset=0",
				"/v3/suppression/invalid_emails?limit=500&offset=0",
				"/v3/suppression/spam_reports?limit=500&offset=0");
		assertEquals(expected, handler.requestedUris);
	}

	@Test
	public void theStartTimeGoesInEveryPage() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(700, NEVER);
		String url = this.start(handler);

		List<CcpJsonRepresentation> records = getReader().getSuppressedEmailsCreatedSince("token", url, CcpEmailSuppressionList.spam_reports, 1700000000L);

		assertEquals(700, records.size());
		List<String> expected = Arrays.asList(
				"/v3/suppression/spam_reports?limit=500&start_time=1700000000&offset=0",
				"/v3/suppression/spam_reports?limit=500&start_time=1700000000&offset=500");
		assertEquals(expected, handler.requestedUris);
	}

	@Test
	public void everyPageIsAuthenticatedWithTheBearerToken() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(600, NEVER);
		String url = this.start(handler);

		getReader().getSuppressedEmails("theToken", url, CcpEmailSuppressionList.invalid_emails);

		assertEquals(Arrays.asList("Bearer theToken", "Bearer theToken"), handler.authorizations);
	}

	/** A rate limit in the middle of the list must not look like its end. */
	@Test
	public void aRefusedPageRaisesAnErrorInsteadOfReturningAPartialList() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(1200, 2);
		String url = this.start(handler);

		try {
			getReader().getSuppressedEmails("token", url, CcpEmailSuppressionList.bounces);
		} catch (CcpErrorHttp expected) {
			assertEquals(2, handler.requestedUris.size());
			return;
		}
		assertFalse("A partial list was returned", true);
	}

	/** The error goes to logs and to the database: the API key must not go with it. */
	@Test
	public void theApiKeyIsNotInTheErrorOfARefusedPage() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(0, 1);
		String url = this.start(handler);

		try {
			getReader().getSuppressedEmails("theSecretApiKey", url, CcpEmailSuppressionList.bounces);
		} catch (CcpErrorHttp expected) {
			String message = expected.getMessage();
			assertFalse(message, message.contains("theSecretApiKey"));
			String details = expected.entity.asUgglyJson();
			assertFalse(details, details.contains("theSecretApiKey"));
			return;
		}
		assertFalse("The refused page raised no error", true);
	}

	@Test(expected = CcpNullParameterException.class)
	public void aNullTokenIsRefused() throws IOException {
		SendGridSuppressionListHandler handler = new SendGridSuppressionListHandler(0, NEVER);
		String url = this.start(handler);

		getReader().getSuppressedEmails(null, url, CcpEmailSuppressionList.bounces);
	}
}
