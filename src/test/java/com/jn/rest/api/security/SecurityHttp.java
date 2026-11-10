package com.jn.rest.api.security;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.ccp.decorators.CcpJsonFieldName;
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
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.rest.api.commons.TestVariables;

/**
 * The HTTP of the security suite: requests to the local APIs (vis on 8081, js on 8082, both must be up) as the front end
 * sends them, with the session token header and the user agent saved with the session. Never points to production.
 */
final class SecurityHttp {

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
		localhost
	}

	static final int VIS = 8081;

	static final int JS = 8082;

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

	private SecurityHttp() {}

	/** Loads the dependencies (the static block) before a test class touches an entity. */
	static void load() {}

	static void theApiMustBeUp(int port) {
		String host = Fields.localhost.name();
		try (Socket socket = new Socket()) {
			InetSocketAddress api = new InetSocketAddress(host, port);
			socket.connect(api, 2_000);
		} catch (IOException e) {
			throw new AssertionError("The local API on port " + port + " is down: the security suite runs only against the local APIs", e);
		}
	}

	/**
	 * Sends a request.
	 * @param port 8081 (vis) or 8082 (js)
	 * @param method GET, POST, DELETE...
	 * @param path the path after the host, with no leading slash
	 * @param body the JSON body ({@code {}} when there is none)
	 * @param user whose user agent goes in the header (the session key holds it)
	 * @param sessionToken the session token header (empty for none)
	 * @return the answer
	 */
	static HttpResponse<String> send(int port, String method, String path, String body, TestVariables user, String sessionToken) {
		String host = Fields.localhost.name();
		URI uri = URI.create("http://" + host + ":" + port + "/" + path);
		String userAgent = user.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.userAgent);
		String sessionTokenHeader = CcpJsonCommonsFields.sessionToken.getValue();
		String userAgentHeader = Fields.User_Agent.getValue();
		String contentTypeHeader = Fields.Content_Type.getValue();
		String contentType = Fields.application_json.getValue();
		HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(body);
		HttpRequest request = HttpRequest.newBuilder(uri)
				.header(sessionTokenHeader, sessionToken)
				.header(userAgentHeader, userAgent)
				.header(contentTypeHeader, contentType)
				.method(method, publisher)
				.build();
		HttpClient client = HttpClient.newHttpClient();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return response;
		} catch (IOException e) {
			throw new AssertionError("The request to " + uri + " has failed", e);
		} catch (InterruptedException e) {
			Thread currentThread = Thread.currentThread();
			currentThread.interrupt();
			throw new AssertionError("The request to " + uri + " was interrupted", e);
		}
	}

	private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

	/** A random session token of 8 characters, the only length the session validation accepts. */
	static String token() {
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder token = new StringBuilder();
		for (int position = 0; position < 8; position++) {
			int index = RANDOM.nextInt(alphabet.length());
			token.append(alphabet.charAt(index));
		}
		return token.toString();
	}

	/** Whether the status refuses the request for lack of a valid session: 401, or 422 when the header is missing. */
	static boolean refusedForTheSession(int status) {
		return status == 401 || status == 422;
	}
}
