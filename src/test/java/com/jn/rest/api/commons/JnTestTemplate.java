package com.jn.rest.api.commons;

import java.util.function.Function;

import org.junit.BeforeClass;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.http.CcpHttpResponseTransform;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.decorators.CountingEmailSender;
import com.jn.entities.decorators.CountingInstantMessenger;


public abstract class JnTestTemplate {
	enum JsonFieldNames implements CcpJsonFieldName{
		x, timestamp, response, request, headers, expectedStatus, actualStatus, method, url, message
	}
	protected final String ENDPOINT_URL = "http://localhost:8080/";

	/**
	 * Runs before each test class, and not only once in a {@code static} block: other classes of the
	 * suite swap the dependency injection, and with {@code static} the login classes running after them
	 * would inherit someone else's injection — a result dependent on execution order.
	 *
	 * <p>Telegram and e-mail are test doubles that send nothing: the login flows notify support and send
	 * the token by e-mail, and with no registered implementation they broke when looking up the dependency.
	 */
	@BeforeClass
	public static void loadDependencies() {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> new CountingInstantMessenger();
		CcpInstanceProvider<CcpEmailSender> email = () -> new CountingEmailSender();

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
				);
	}


	public static void main(String[] args) {
		
	}
	
	protected abstract CcpHttpMethods getMethod();

	protected CcpJsonRepresentation getHeaders() {
		return CcpOtherConstants.EMPTY_JSON;
	}

	protected CcpJsonRepresentation testEndpoint(String uri, CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation responseJson = this.testEndpoint(expectedStatus, CcpOtherConstants.EMPTY_JSON, uri,
				CcpHttpResponseType.singleRecord);
		return responseJson;
	}

	protected CcpJsonRepresentation testEndpoint(CcpProcessStatus scenarioName, CcpJsonRepresentation body, String uri,
			CcpHttpResponseTransform<CcpJsonRepresentation> transformer) {

		CcpHttpMethods method = this.getMethod();
		CcpJsonRepresentation headers = this.getHeaders();

		int expectedStatus = scenarioName.asNumber();
		String path = this.ENDPOINT_URL + uri;
		CcpHttpHandler http = new CcpHttpHandler(expectedStatus, CcpOtherConstants.DO_NOTHING, path);
		String name = this.getClass().getName();
		String requestBody = body.asUgglyJson();

		CcpHttpResponse response = http.ccpHttp.executeHttpRequest(path, method, headers, requestBody);

		CcpJsonRepresentation responseFromEndpoint = http.executeHttpRequest(name, method, headers, requestBody, transformer, response);

		int actualStatus = response.httpStatus;

		this.logRequestAndResponse(path, method, scenarioName, actualStatus, body, headers, responseFromEndpoint);

		String message = response.asSingleJson().getAsString(JsonFieldNames.message);
		scenarioName.verifyStatus(actualStatus, message);

		return responseFromEndpoint;
	}

	private <V> void logRequestAndResponse(String url, CcpHttpMethods method, CcpProcessStatus status, int actualStatus,
			CcpJsonRepresentation body, CcpJsonRepresentation headers, V responseBody) {

		CcpJsonRepresentation loggedResponse = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.x, responseBody);

		if (responseBody instanceof CcpJsonRepresentation json) {
			loggedResponse = json;
		}

		String date = new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss");

		int expectedStatus = status.asNumber();
		CcpJsonRepresentation logEntry = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.url, url)
				.put(JsonFieldNames.method, method)
				.put(JsonFieldNames.actualStatus, actualStatus)
				.put(JsonFieldNames.expectedStatus, expectedStatus)
				.put(JsonFieldNames.headers, headers)
				.put(JsonFieldNames.request, body)
				.put(JsonFieldNames.response, loggedResponse)
				.put(JsonFieldNames.timestamp, date);
		String logContent = logEntry.asPrettyJson();

		String testName = this.getClass().getSimpleName();
		new CcpStringDecorator("c:\\logs\\jn\\").folder().createNewFolderIfNotExists(testName)
				.writeInTheFile(status + ".json", logContent);
	}
	
	public final String execute(TestVariables testVariables, CcpProcessStatus expectedStatus) {
		String result = this.execute(testVariables, expectedStatus, x -> "");
		return result;
	}
	
	public abstract String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer);

}
