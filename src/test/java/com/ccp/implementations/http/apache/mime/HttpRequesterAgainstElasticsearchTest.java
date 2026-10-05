package com.ccp.implementations.http.apache.mime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.ssl.SSLException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the Apache HTTP client of the framework against the local Elasticsearch: each verb reaches the server (the
 * body is sent only by POST, PUT and PATCH), the headers go along, a multipart request is built and sent, an
 * unreachable server becomes {@code CcpErrorApacheMimeHttp}, and the retry policy only repeats idempotent requests that
 * failed for a recoverable reason.
 */
public class HttpRequesterAgainstElasticsearchTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpApacheMimeHttp());
	}

	private static final String INDEX_URL = "http://localhost:9200/ccp_http_requester_test";

	private final CcpHttpRequester http = CcpDependencyInjection.getDependency(CcpHttpRequester.class);

	private final CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("Content-Type"), "application/json");

	@Test
	public void eachVerbReachesTheServer() {
		this.http.executeHttpRequest(INDEX_URL, CcpHttpMethods.DELETE, this.json, "");

		CcpHttpResponse created = this.http.executeHttpRequest(INDEX_URL, CcpHttpMethods.PUT, this.json, "{\"mappings\":{\"properties\":{\"name\":{\"type\":\"keyword\"}}}}");
		assertEquals(created.httpResponse, 200, created.httpStatus);

		CcpHttpResponse indexed = this.http.executeHttpRequest(INDEX_URL + "/_doc/1?refresh=true", CcpHttpMethods.POST, this.json, "{\"name\":\"a\"}");
		assertEquals(indexed.httpResponse, 201, indexed.httpStatus);

		CcpHttpResponse read = this.http.executeHttpRequest(INDEX_URL + "/_doc/1", CcpHttpMethods.GET, this.json, "ignored by GET");
		assertTrue(read.httpResponse, read.httpResponse.contains("\"name\":\"a\""));

		CcpHttpResponse patched = this.http.executeHttpRequest(INDEX_URL, CcpHttpMethods.PATCH, this.json, "{}");
		assertEquals("Elasticsearch has no PATCH", 405, patched.httpStatus);

		CcpHttpResponse deleted = this.http.executeHttpRequest(INDEX_URL, CcpHttpMethods.DELETE, this.json, "ignored by DELETE");
		assertEquals(deleted.httpResponse, 200, deleted.httpStatus);
		assertTrue(deleted.curl, deleted.curl.contains("DELETE"));
	}

	@Test
	public void aMultipartRequestIsBuiltAndSent() {
		CcpHttpBodyText text = new CcpHttpBodyText(CcpHttpContentType.TEXT_PLAIN, "chat_id", "1");
		Byte[] bytes = { 104, 105 };
		CcpHttpBodyBinary file = new CcpHttpBodyBinary(CcpHttpContentType.TEXT_HTML, "document", "hi.txt", bytes);

		CcpHttpResponse response = this.http.executeMultiPartHttpRequest("http://localhost:9200/_bulk", CcpHttpMethods.POST,
				CcpOtherConstants.EMPTY_JSON, Arrays.asList(text), Arrays.asList(file));

		assertTrue("Elasticsearch answered the multipart request: " + response.httpStatus, response.httpStatus >= 400);
	}

	@Test
	public void anUnreachableServerBecomesAnErrorOfTheClient() {
		try {
			this.http.executeHttpRequest("http://localhost:1/nothing", CcpHttpMethods.GET, this.json, "");
			fail("nobody listens on port 1");
		} catch (RuntimeException e) {
			assertTrue(e.getClass().getName(), e.getClass().getName().endsWith("CcpErrorApacheMimeHttp"));
		}
	}

	private boolean retries(IOException failure, int executionCount, Object request) {
		HttpClientContext context = HttpClientContext.create();
		context.setAttribute(HttpCoreContext.HTTP_REQUEST, request);
		boolean retry = new CcpHttpRequestRetryHandler().retryRequest(failure, executionCount, context);
		return retry;
	}

	@Test
	public void onlyRecoverableFailuresOfIdempotentRequestsAreRetried() {
		HttpGet get = new HttpGet("http://localhost");
		HttpPost post = new HttpPost("http://localhost");

		assertTrue(this.retries(new IOException("reset"), 1, get));
		assertFalse("a request with a body is not idempotent", this.retries(new IOException("reset"), 1, post));
		assertFalse("three tries at most", this.retries(new IOException("reset"), 3, get));
		assertFalse(this.retries(new InterruptedIOException("timeout"), 1, get));
		assertFalse(this.retries(new UnknownHostException("nowhere"), 1, get));
		assertFalse(this.retries(new ConnectTimeoutException("refused"), 1, get));
		assertFalse(this.retries(new SSLException("handshake"), 1, get));
	}

	@Test
	public void theMethodCarriesTheHeaders() {
		CcpJsonRepresentation headers = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("X-Test"), "yes");

		org.apache.http.client.methods.HttpRequestBase method = HttpMethod.PUT.getMethod("http://localhost", headers, "{}");

		assertEquals("yes", method.getFirstHeader("X-Test").getValue());
		assertEquals("PUT", method.getMethod());
	}

	@Test
	public void theVerbsWithoutABodyRefuseToBeBuiltForAMultipartRequest() {
		for (HttpMethod verb : Arrays.asList(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD)) {
			try {
				verb.getMethodWithoutBody("http://localhost");
				fail(verb + " has no body");
			} catch (UnsupportedOperationException expected) {
			}
		}
		assertEquals("HEAD", HttpMethod.HEAD.getMethodWithBody("http://localhost", "{}").getMethod());
		assertEquals("PATCH", HttpMethod.PATCH.getMethodWithoutBody("http://localhost").getMethod());
	}
}
