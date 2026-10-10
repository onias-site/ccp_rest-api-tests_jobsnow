package com.ccp.implementations.http.apache.mime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponse;
import com.sun.net.httpserver.HttpServer;

/**
 * Proves the safety fixes of the Apache HTTP requester (2026-10-06): the curl command that goes into errors, logs and the
 * {@code jn_http_api_*} records masks the credentials, and every request uses one shared client whose connections go
 * back to the pool (before, each request built a new client and closed nothing).
 */
public class HttpClientSafetyTest {

	private final ApacheMimeHttpRequester requester = new ApacheMimeHttpRequester();

	@Test
	public void theCurlMasksTheCredentialHeaders() throws Exception {
		HttpPost request = new HttpPost("http://localhost/send");
		request.addHeader("Authorization", "Bearer SG.secret-api-key");
		request.addHeader("sessionToken", "ABCD1234");
		request.addHeader("Accept", "application/json");
		request.setEntity(new StringEntity("{}"));

		String curl = this.requester.toCurl(request);

		assertFalse(curl, curl.contains("SG.secret-api-key"));
		assertFalse(curl, curl.contains("ABCD1234"));
		assertTrue(curl, curl.contains("Authorization: " + ApacheMimeHttpRequester.MASK));
		assertTrue(curl, curl.contains("Accept: application/json"));
	}

	@Test
	public void theCurlMasksTheTelegramBotTokenOfTheUrl() {
		HttpGet request = new HttpGet("https://api.telegram.org/bot123456:AAH-secret_token/getUpdates?offset=1");

		String curl = this.requester.toCurl(request);

		assertFalse(curl, curl.contains("AAH-secret_token"));
		assertTrue(curl, curl.contains("/bot" + ApacheMimeHttpRequester.MASK + "/getUpdates?offset=1"));
	}

	/**
	 * A multipart body over 25 KB can not be read back as text ({@code ContentTooLongException}). Until 2026-10-10 the
	 * curl tried to and threw, so the error file sent to the support (46 KB) was reported as a failed sending.
	 */
	@Test
	public void theCurlOfABigMultipartBodyDoesNotFail() {
		HttpPost request = new HttpPost("http://localhost/sendDocument");
		HttpEntity bigFile = MultipartEntityBuilder.create().addBinaryBody("document", new byte[46_237], ContentType.TEXT_PLAIN, "error.txt").build();
		request.setEntity(bigFile);

		String curl = this.requester.toCurl(request);

		assertTrue(curl, curl.startsWith("curl -X POST \"http://localhost/sendDocument\""));
		assertTrue(curl, curl.contains("multipart/form-data"));
		assertFalse(curl, curl.contains("--data"));
	}

	/** The request that reached the server and was accepted is reported as accepted, whatever the size of its file. */
	@Test(timeout = 60_000)
	public void aBigMultipartRequestAcceptedByTheServerIsNotReportedAsAFailure() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/", exchange -> {
			exchange.getRequestBody().readAllBytes();
			byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		server.start();
		try {
			String url = "http://localhost:" + server.getAddress().getPort() + "/sendDocument";
			Byte[] content = new Byte[46_237];
			Arrays.fill(content, (byte) 'x');
			CcpHttpBodyBinary file = new CcpHttpBodyBinary(CcpHttpContentType.TEXT_PLAIN, "document", "error.txt", content);
			CcpHttpResponse response = this.requester.executeMultiPartHttpRequest(url, CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON, List.of(), List.of(file));
			assertEquals(200, response.httpStatus);
		} finally {
			server.stop(0);
		}
	}

	@Test
	public void everyRequestUsesTheSameClient() {
		assertSame(CcpHttpRequestRetryHandler.getClient(), CcpHttpRequestRetryHandler.getClient());
	}

	/**
	 * Many more requests than the pool has connections for one host: they all complete only if each response gives its
	 * connection back.
	 */
	@Test(timeout = 60_000)
	public void theConnectionsGoBackToThePool() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/", exchange -> {
			byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		server.start();
		try {
			String url = "http://localhost:" + server.getAddress().getPort() + "/ping";
			for (int k = 0; k < 100; k++) {
				CcpHttpResponse response = this.requester.executeHttpRequest(url, CcpHttpMethods.GET, CcpOtherConstants.EMPTY_JSON, "");
				assertEquals(200, response.httpStatus);
			}
		} finally {
			server.stop(0);
		}
	}
}
