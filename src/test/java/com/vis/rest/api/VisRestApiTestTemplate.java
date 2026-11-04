package com.vis.rest.api;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import org.junit.BeforeClass;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.CcpLocalMensageriaRunningMarks;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.rest.api.commons.TestVariables;

/**
 * Base of the tests of the controllers of the vis API (port 8081, which must be up): sends the request through HTTP, as
 * the front end does, with or without the session of a random candidate, and waits for the writes that the API leaves
 * to its asynchronous mensageria before handing the answer to the test. Until 2026-11-03 no controller of the vis had a
 * test of its own: they were reached only through their services.
 */
public abstract class VisRestApiTestTemplate {

	enum Fields implements CcpJsonFieldName {
		User_Agent {
			public String getValue() {
				return "User-Agent";
			}
		},
		Content_Type {
			public String getValue() {
				return "Content-Type";
			}
		},
		application_json {
			public String getValue() {
				return "application/json";
			}
		},
		Cache_Control {
			public String getValue() {
				return "Cache-Control";
			}
		},
		GET, POST, DELETE, resume
	}

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	protected static final String SESSION_TOKEN = "12345678";

	protected static final String FAKE_SESSION_TOKEN = "ABCD1234";

	private static final String VIS_API_HOST = "localhost";

	private static final int VIS_API_PORT = 8081;

	private static final long MAX_WAIT_FOR_ASYNC_WRITES = 30_000;

	private static final long PAUSE_BETWEEN_COUNTS = 100;

	protected final TestVariables testVariables = new TestVariables();

	/** Stops the class with a clear message when the vis API is down, instead of a bare connection refused per test. */
	@BeforeClass
	public static void theVisApiMustBeUp() {
		try (Socket socket = new Socket()) {
			InetSocketAddress visApi = new InetSocketAddress(VIS_API_HOST, VIS_API_PORT);
			socket.connect(visApi, 2_000);
		} catch (IOException e) {
			throw new AssertionError("The vis API is down on port " + VIS_API_PORT + ": start VisRestApiSpringStarter before running the tests of its controllers", e);
		}
	}

	/** Opens the current session of the random candidate of the test, as a login does. */
	protected final void login() {
		JnEntityLoginEmail.ENTITY.save(this.testVariables.REQUEST_TO_LOGIN);
		this.testVariables.saveCurrentSession(SESSION_TOKEN);
	}

	/**
	 * Sends a request to the vis API and waits for the writes it left to the asynchronous mensageria.
	 * @param method GET, POST or DELETE
	 * @param path the path after the host, with no leading slash
	 * @param body the body (an empty JSON is sent as {@code {}})
	 * @param sessionToken the session token header (empty for none)
	 * @return the answer
	 */
	protected final HttpResponse<String> send(Fields method, String path, CcpJsonRepresentation body, String sessionToken) {
		URI uri = URI.create("http://" + VIS_API_HOST + ":" + VIS_API_PORT + "/" + path);
		// the session key holds the user agent, so the request repeats the one saved with the session
		String userAgent = this.testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.userAgent);
		String sessionTokenHeader = CcpJsonCommonsFields.sessionToken.getValue();
		String userAgentHeader = Fields.User_Agent.getValue();
		String contentTypeHeader = Fields.Content_Type.getValue();
		String contentType = Fields.application_json.getValue();
		String requestBody = body.asUgglyJson();
		BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody);
		String methodName = method.name();
		HttpRequest request = HttpRequest.newBuilder(uri)
				.header(sessionTokenHeader, sessionToken)
				.header(userAgentHeader, userAgent)
				.header(contentTypeHeader, contentType)
				.method(methodName, bodyPublisher)
				.build();
		HttpClient client = HttpClient.newHttpClient();
		BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
		long requestStart = System.currentTimeMillis();
		try {
			HttpResponse<String> response = client.send(request, bodyHandler);
			this.waitForTheAsynchronousWrites(requestStart);
			return response;
		} catch (IOException e) {
			throw new AssertionError("The request to " + uri + " has failed", e);
		} catch (InterruptedException e) {
			Thread currentThread = Thread.currentThread();
			currentThread.interrupt();
			throw new AssertionError("The request to " + uri + " was interrupted", e);
		}
	}

	protected final HttpResponse<String> sendWithTheSession(Fields method, String path, CcpJsonRepresentation body) {
		HttpResponse<String> response = this.send(method, path, body, SESSION_TOKEN);
		return response;
	}

	protected final HttpResponse<String> sendWithNoSession(Fields method, String path) {
		HttpResponse<String> response = this.send(method, path, CcpOtherConstants.EMPTY_JSON, "");
		return response;
	}

	/**
	 * The path of a route of the candidate of the test, under {@code resume/{email}}, where the session filter acts.
	 * @param suffix what follows the e-mail, starting with a slash (or empty)
	 * @return the path
	 */
	protected final String resumePath(String suffix) {
		String resume = Fields.resume.name();
		String path = resume + "/" + this.testVariables.VALID_EMAIL + suffix;
		return path;
	}

	protected final CcpJsonRepresentation bodyOf(HttpResponse<String> response) {
		String body = response.body();
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		return json;
	}

	protected final void assertStatus(int expectedStatus, HttpResponse<String> response) {
		String body = response.body();
		int actualStatus = response.statusCode();
		assertEquals(body, expectedStatus, actualStatus);
	}

	protected final String cacheControlOf(HttpResponse<String> response) {
		String cacheControlHeader = Fields.Cache_Control.getValue();
		String cacheControl = response.headers().firstValue(cacheControlHeader).orElse("");
		return cacheControl;
	}

	/**
	 * Waits until no task of the asynchronous mensageria published after the request is still running (the same wait
	 * of {@code JnTestTemplate}).
	 * @param requestStart when the request was sent
	 */
	private void waitForTheAsynchronousWrites(long requestStart) {
		long deadline = requestStart + MAX_WAIT_FOR_ASYNC_WRITES;

		while (System.currentTimeMillis() < deadline) {
			boolean someTaskIsRunning = CcpLocalMensageriaRunningMarks.isAnyRunningPublishedAfter(requestStart);
			if (false == someTaskIsRunning) {
				return;
			}
			this.pause();
		}
		throw new AssertionError("The asynchronous writes of the request sent at " + requestStart + " did not end in " + MAX_WAIT_FOR_ASYNC_WRITES + " ms");
	}

	private void pause() {
		try {
			Thread.sleep(PAUSE_BETWEEN_COUNTS);
		} catch (InterruptedException e) {
			Thread currentThread = Thread.currentThread();
			currentThread.interrupt();
		}
	}
}
