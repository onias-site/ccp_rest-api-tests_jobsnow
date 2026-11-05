package com.ccp.implementations.db.utils.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbCallsOfTheThread;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntitySupportPendingCommand;

/**
 * Proves the measurement of the calls to Elasticsearch ({@link CcpDbCallsOfTheThread} in {@link ElasticSearchDbRequester}),
 * created on 2026-11-05 for the latency columns of the local panels of the requests and of the queues: inside a unit
 * each call carries the trace ({@code traceparent}) and its own id ({@code X-Opaque-Id}), which the slowlogs of
 * Elasticsearch keep, and is recorded with the latency seen by the client and the {@code took} of the response; outside a
 * unit nothing changes.
 */
public class DbCallsOfTheThreadTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final CcpFieldName TRACEPARENT = new CcpFieldName("traceparent");
	private static final CcpFieldName OPAQUE_ID = new CcpFieldName("X-Opaque-Id");

	/** Keeps the headers of each request and answers, in turn, the given responses. */
	static class ScriptedRequester implements CcpHttpRequester {
		final List<CcpJsonRepresentation> headers = new ArrayList<>();
		final List<CcpHttpResponse> responses;

		ScriptedRequester(CcpHttpResponse... responses) {
			this.responses = new ArrayList<>(List.of(responses));
		}

		public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
			this.headers.add(headers);
			return this.responses.remove(0);
		}

		public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
			return this.executeHttpRequest(url, method, headers, "");
		}
	}

	@After
	public void restoreTheRealHttpAndEndTheUnit() {
		CcpDbCallsOfTheThread.finish();
		CcpDependencyInjection.loadAllDependencies(CcpApacheMimeHttp.INSTANCE);
	}

	private static CcpDbRequester dbAnswering(ScriptedRequester requester) {
		CcpDependencyInjection.loadAllDependencies(() -> requester);
		CcpDbRequester db = CcpElasticSearchDbRequest.INSTANCE.getInstance();
		return db;
	}

	private static void search(CcpDbRequester db) {
		db.executeHttpRequest("search", "/any_entity/_search", CcpHttpMethods.POST, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
	}

	@Test
	public void outsideAUnitNoTraceIsSentAndNothingIsRecorded() {
		ScriptedRequester requester = new ScriptedRequester(new CcpHttpResponse("{\"took\":7}", 200, ""));
		CcpDbRequester db = dbAnswering(requester);

		search(db);

		CcpJsonRepresentation sentHeaders = requester.headers.get(0);
		assertFalse(sentHeaders.toString(), sentHeaders.containsField(TRACEPARENT));
		assertFalse(sentHeaders.toString(), sentHeaders.containsField(OPAQUE_ID));
		CcpJsonRepresentation summary = CcpDbCallsOfTheThread.finish();
		assertEquals(0, summary.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.dbCallsCount).intValue());
	}

	@Test
	public void insideAUnitEachCallCarriesTheTraceAndItsOwnId() {
		ScriptedRequester requester = new ScriptedRequester(new CcpHttpResponse("{\"took\":7,\"hits\":{}}", 200, ""),
				new CcpHttpResponse("{\"_index\":\"any_entity\",\"found\":false}", 200, ""));
		CcpDbRequester db = dbAnswering(requester);
		String traceId = CcpDbCallsOfTheThread.newTraceId();
		CcpDbCallsOfTheThread.start(traceId);

		search(db);
		db.executeHttpRequest("get", "/any_entity/_doc/1", CcpHttpMethods.GET, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);

		String firstTraceparent = requester.headers.get(0).getAsString(TRACEPARENT);
		assertTrue(firstTraceparent, firstTraceparent.matches("00-" + traceId + "-[0-9a-f]{16}-01"));
		assertEquals(traceId + "-1", requester.headers.get(0).getAsString(OPAQUE_ID));
		assertEquals(traceId + "-2", requester.headers.get(1).getAsString(OPAQUE_ID));

		CcpJsonRepresentation summary = CcpDbCallsOfTheThread.finish();
		assertEquals(traceId, summary.getAsString(CcpDbCallsOfTheThread.Fields.traceId));
		assertEquals(2, summary.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.dbCallsCount).intValue());
		assertEquals(7.0, summary.getAsDoubleNumber(CcpDbCallsOfTheThread.Fields.dbTookMs), 0.0);
		List<CcpJsonRepresentation> calls = summary.getAsJsonList(CcpDbCallsOfTheThread.Fields.dbCalls);
		CcpJsonRepresentation searchCall = calls.get(0);
		assertEquals("/any_entity/_search", searchCall.getAsString(new CcpFieldName("path")));
		assertEquals("POST", searchCall.getAsString(new CcpFieldName("method")));
		assertEquals(7L, searchCall.getAsLongNumber(CcpDbCallsOfTheThread.Fields.tookMs).longValue());
		CcpJsonRepresentation getCall = calls.get(1);
		assertFalse("a get reports no took", getCall.containsField(CcpDbCallsOfTheThread.Fields.tookMs));
		assertTrue(getCall.containsField(CcpDbCallsOfTheThread.Fields.clientMs));
		assertFalse("the unit ended", CcpDbCallsOfTheThread.isMeasuring());
	}

	@Test
	public void aRefusedStatusIsMeasuredToo() {
		ScriptedRequester requester = new ScriptedRequester(new CcpHttpResponse("{\"error\":{}}", 500, ""));
		CcpDbRequester db = dbAnswering(requester);
		CcpDbCallsOfTheThread.start(CcpDbCallsOfTheThread.newTraceId());
		try {
			search(db);
			fail("500 is not the expected status");
		} catch (CcpErrorHttp expected) {
		}
		CcpJsonRepresentation summary = CcpDbCallsOfTheThread.finish();
		List<CcpJsonRepresentation> calls = summary.getAsJsonList(CcpDbCallsOfTheThread.Fields.dbCalls);
		assertEquals(1, calls.size());
		assertEquals(500, calls.get(0).getAsIntegerNumber(new CcpFieldName("status")).intValue());
	}

	/**
	 * The indexes of each call, for the panel of the calls to Elasticsearch: the ones of the path and the ones named in the
	 * body of an {@code _mget}, without repetition and in alphabetical order; none when the path starts with an endpoint.
	 */
	@Test
	public void eachCallKnowsTheIndexesItTouches() {
		ScriptedRequester requester = new ScriptedRequester(new CcpHttpResponse("{\"took\":1}", 200, ""),
				new CcpHttpResponse("{\"docs\":[]}", 200, ""), new CcpHttpResponse("{}", 200, ""));
		CcpDbRequester db = dbAnswering(requester);
		CcpDbCallsOfTheThread.start(CcpDbCallsOfTheThread.newTraceId());

		db.executeHttpRequest("search", "/zeta,alpha/_search?size=0", CcpHttpMethods.POST, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
		CcpJsonRepresentation mgetBody = new CcpJsonRepresentation("{\"docs\":[{\"_index\":\"jn_b\",\"_id\":\"1\"},{\"_index\":\"jn_a\",\"_id\":\"2\"},{\"_index\":\"jn_b\",\"_id\":\"3\"}]}");
		db.executeHttpRequest("mget", "/_mget", CcpHttpMethods.POST, 200, mgetBody, CcpHttpResponseType.singleRecord);
		db.executeHttpRequest("health", "/_cluster/health", CcpHttpMethods.GET, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);

		List<CcpJsonRepresentation> calls = CcpDbCallsOfTheThread.finish().getAsJsonList(CcpDbCallsOfTheThread.Fields.dbCalls);
		CcpFieldName indices = new CcpFieldName("indices");
		assertEquals(List.of("alpha", "zeta"), calls.get(0).getAsStringList(indices));
		assertEquals(List.of("jn_a", "jn_b"), calls.get(1).getAsStringList(indices));
		assertEquals(List.of(), calls.get(2).getAsStringList(indices));
	}

	/** The took is read from the start of the real response of Elasticsearch, without parsing it. */
	@Test
	public void theRealDatabaseReportsTheTookOfASearch() {
		CcpDependencyInjection.loadAllDependencies(CcpApacheMimeHttp.INSTANCE);
		CcpDbRequester db = CcpElasticSearchDbRequest.INSTANCE.getInstance();
		String entityName = JnEntitySupportPendingCommand.ENTITY.getEntityMetaData().entityName;
		CcpDbCallsOfTheThread.start(CcpDbCallsOfTheThread.newTraceId());

		db.executeHttpRequest("search", "/" + entityName + "/_search", CcpHttpMethods.POST, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);

		CcpJsonRepresentation summary = CcpDbCallsOfTheThread.finish();
		CcpJsonRepresentation call = summary.getAsJsonList(CcpDbCallsOfTheThread.Fields.dbCalls).get(0);
		assertTrue(call.toString(), call.containsField(CcpDbCallsOfTheThread.Fields.tookMs));
		assertTrue(call.toString(), call.getAsDoubleNumber(CcpDbCallsOfTheThread.Fields.clientMs) > 0);
	}
}
