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
import com.ccp.especifications.db.query.CcpQueryExecutorDecorator;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.http.CcpHttpResponseTransform;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatus;
import com.ccp.especifications.db.utils.entity.decorators.CountingEmailSender;
import com.ccp.especifications.db.utils.entity.decorators.CountingInstantMessenger;
import com.jn.entities.JnEntityAsyncTask;


public abstract class JnTestTemplate {
	enum JsonFieldNames implements CcpJsonFieldName{
		x, timestamp, response, request, headers, expectedStatus, actualStatus, method, url, message
	}
	protected final String ENDPOINT_URL = "http://localhost:8082/";

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
				// counts the asynchronous tasks still running (see waitForTheAsynchronousWrites)
				new CcpElasticSearchQueryExecutor(),
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

		long requestStart = System.currentTimeMillis();
		CcpHttpResponse response = http.ccpHttp.executeHttpRequest(path, method, headers, requestBody);
		this.waitForTheAsynchronousWrites(requestStart);

		CcpJsonRepresentation responseFromEndpoint = http.executeHttpRequest(name, method, headers, requestBody, transformer, response);

		int actualStatus = response.httpStatus;

		this.logRequestAndResponse(path, method, scenarioName, actualStatus, body, headers, responseFromEndpoint);

		String message = response.asSingleJson().getAsString(JsonFieldNames.message);
		scenarioName.verifyStatus(actualStatus, message);

		return responseFromEndpoint;
	}

	/**
	 * Waits until every asynchronous task the request started has finished: the APIs run with the asynchronous mensageria
	 * and answer before writing (the login answers the token before the session exists, the password registration before
	 * the password is saved), so the next step of a flow saw the old state (401 {@code invalidSession}, 202
	 * {@code missingSavePassword}). A task is saved in {@code jn_async_task} before the answer and gets {@code success} when
	 * it ends, and a task started by another one is saved before the first one ends; the index is refreshed before each
	 * count because a new task is not searchable before the refresh of Elasticsearch.
	 * @param requestStart when the request was sent
	 * @throws JnErrorAsynchronousWritesDidNotEnd when some task does not end in {@value #MAX_WAIT_FOR_ASYNC_WRITES} ms
	 */
	protected void waitForTheAsynchronousWrites(long requestStart) {
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		CcpEntityMetaData asyncTaskMetaData = JnEntityAsyncTask.ENTITY.getEntityMetaData();
		String asyncTaskIndex = asyncTaskMetaData.entityName;
		String[] asyncTaskIndexes = new String[] {asyncTaskIndex};
		String startedField = JnEntityAsyncTask.Fields.started.name();
		String successField = JnEntityAsyncTask.Fields.success.name();

		CcpQueryOptions unfinishedTasksStartedByTheRequest = CcpQueryOptions.INSTANCE
				.startQuery()
					.startBool()
						.startMust()
							.startRange().startFieldRange(startedField).greaterThanEquals(requestStart).endFieldRangeAndBackToRange().endRangeAndBackToMust()
						.endMustAndBackToBool()
						.startMustNot()
							.exists(successField)
						.endMustNotAndBackToBool()
					.endBoolAndBackToQuery()
				.endQueryAndBackToRequest();

		CcpQueryExecutorDecorator unfinishedTasks = unfinishedTasksStartedByTheRequest.selectFrom(asyncTaskIndex);
		String refreshPath = ElasticsearchPaths.refresh.getValue();
		long deadline = requestStart + MAX_WAIT_FOR_ASYNC_WRITES;

		while (System.currentTimeMillis() < deadline) {
			database.executeHttpRequest(refreshPath, refreshPath, CcpHttpMethods.POST, 200, CcpOtherConstants.EMPTY_JSON, asyncTaskIndexes, CcpHttpResponseType.singleRecord);
			long unfinishedCount = unfinishedTasks.total();
			boolean allTasksEnded = unfinishedCount == 0;
			if (allTasksEnded) {
				return;
			}
			this.pause();
		}
		JnErrorAsynchronousWritesDidNotEnd timeout = new JnErrorAsynchronousWritesDidNotEnd(requestStart);
		throw timeout;
	}

	/** Waits a little before counting the unfinished tasks again. */
	private void pause() {
		try {
			Thread.sleep(PAUSE_BETWEEN_COUNTS);
		} catch (InterruptedException e) {
			Thread currentThread = Thread.currentThread();
			currentThread.interrupt();
		}
	}

	/** How long, in ms, the asynchronous writes of a request may take. */
	private static final long MAX_WAIT_FOR_ASYNC_WRITES = 30_000;

	/** The pause, in ms, between two counts of the unfinished tasks. */
	private static final long PAUSE_BETWEEN_COUNTS = 100;

	/** The paths of Elasticsearch used here. */
	enum ElasticsearchPaths implements CcpJsonFieldName {
		/** Makes the recent writes of the index searchable. */
		refresh {
			public String getValue() {
				return "/_refresh";
			}
		}
	}

	/** Some asynchronous task of the request did not end in time. */
	@SuppressWarnings("serial")
	private static class JnErrorAsynchronousWritesDidNotEnd extends RuntimeException {
		/**
		 * Builds the error.
		 * @param requestStart when the request was sent
		 */
		private JnErrorAsynchronousWritesDidNotEnd(long requestStart) {
			super("Asynchronous tasks started since " + requestStart + " did not end in " + MAX_WAIT_FOR_ASYNC_WRITES + " ms");
		}
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
