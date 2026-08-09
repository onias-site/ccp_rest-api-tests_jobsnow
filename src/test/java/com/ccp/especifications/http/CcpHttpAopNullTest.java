package com.ccp.especifications.http;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.http.CcpHttpRequester.CcpErrorHttp;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Cobertura dos aspectos {@code CcpNullParameterAspect} / {@code CcpNullReturnAspect} sobre
 * {@code CcpHttpHandler} e os métodos default de {@code CcpHttpRequester}.
 */
public class CcpHttpAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpApacheMimeHttp());
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static CcpHttpHandler handler() {
		return new CcpHttpHandler(200, "http://localhost:9200");
	}

	private static CcpHttpRequester requester() {
		return CcpDependencyInjection.getDependency(CcpHttpRequester.class);
	}

	// ── construtores de CcpHttpHandler ────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorFlowsNullTest() {
		new CcpHttpHandler((CcpJsonRepresentation) null, "http://localhost:9200");
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorFlowsUrlNullTest() {
		new CcpHttpHandler(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorAlternativeFlowStatusNullTest() {
		new CcpHttpHandler(null, json -> json, "http://localhost:9200");
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorAlternativeFlowBusinessNullTest() {
		new CcpHttpHandler(200, null, "http://localhost:9200");
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorAlternativeFlowUrlNullTest() {
		new CcpHttpHandler(200, json -> json, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorStatusNullTest() {
		new CcpHttpHandler((Integer) null, "http://localhost:9200");
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorUrlNullTest() {
		new CcpHttpHandler(200, (String) null);
	}

	// ── executeHttpSimplifiedGet ──────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpSimplifiedGetTraceNullTest() {
		handler().executeHttpSimplifiedGet(null, CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpSimplifiedGetTransformerNullTest() {
		handler().executeHttpSimplifiedGet("trace", null);
	}

	// ── executeHttpRequest (body como CcpJsonRepresentation) ──────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestJsonBodyTraceNullTest() {
		handler().executeHttpRequest(null, CcpHttpMethods.GET, JSON, JSON, CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestJsonBodyMethodNullTest() {
		handler().executeHttpRequest("trace", null, JSON, JSON, CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestJsonBodyHeadersNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, null, JSON, CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestJsonBodyBodyNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, JSON, (CcpJsonRepresentation) null,
				CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestJsonBodyTransformerNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, JSON, JSON, null);
	}

	// ── executeHttpRequest (body como String) ─────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestStringBodyTraceNullTest() {
		handler().executeHttpRequest(null, CcpHttpMethods.GET, JSON, "{}", CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestStringBodyMethodNullTest() {
		handler().executeHttpRequest("trace", null, JSON, "{}", CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestStringBodyHeadersNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, null, "{}", CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestStringBodyRequestNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, JSON, (String) null,
				CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestStringBodyTransformerNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, JSON, "{}", null);
	}

	// ── executeMultiPartHttpRequest ───────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartTraceNullTest() {
		handler().executeMultiPartHttpRequest(null, CcpHttpMethods.POST, JSON, new ArrayList<CcpHttpBodyText>(),
				new ArrayList<CcpHttpBodyBinary>(), CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartMethodNullTest() {
		handler().executeMultiPartHttpRequest("trace", null, JSON, new ArrayList<CcpHttpBodyText>(),
				new ArrayList<CcpHttpBodyBinary>(), CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartHeadersNullTest() {
		handler().executeMultiPartHttpRequest("trace", CcpHttpMethods.POST, null, new ArrayList<CcpHttpBodyText>(),
				new ArrayList<CcpHttpBodyBinary>(), CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartTextsNullTest() {
		handler().executeMultiPartHttpRequest("trace", CcpHttpMethods.POST, JSON, (List<CcpHttpBodyText>) null,
				new ArrayList<CcpHttpBodyBinary>(), CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartBinariesNullTest() {
		handler().executeMultiPartHttpRequest("trace", CcpHttpMethods.POST, JSON, new ArrayList<CcpHttpBodyText>(),
				(List<CcpHttpBodyBinary>) null, CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartTransformerNullTest() {
		handler().executeMultiPartHttpRequest("trace", CcpHttpMethods.POST, JSON, new ArrayList<CcpHttpBodyText>(),
				new ArrayList<CcpHttpBodyBinary>(), null);
	}

	// ── executeHttpRequest com CcpHttpResponse ────────────────────────────────

	private static CcpHttpResponse response() {
		return new CcpHttpResponse("{}", 200, "curl http://localhost:9200");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestComResponseTraceNullTest() {
		handler().executeHttpRequest(null, CcpHttpMethods.GET, JSON, "{}", CcpHttpResponseType.singleRecord,
				response());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestComResponseMethodNullTest() {
		handler().executeHttpRequest("trace", null, JSON, "{}", CcpHttpResponseType.singleRecord, response());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestComResponseHeadersNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, null, "{}", CcpHttpResponseType.singleRecord,
				response());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestComResponseRequestNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, JSON, null, CcpHttpResponseType.singleRecord,
				response());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestComResponseTransformerNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, JSON, "{}", null, response());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestComResponseResponseNullTest() {
		handler().executeHttpRequest("trace", CcpHttpMethods.GET, JSON, "{}", CcpHttpResponseType.singleRecord,
				(CcpHttpResponse) null);
	}

	// ── CcpErrorHttp ──────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void errorHttpConstrutorNullTest() {
		new CcpErrorHttp(null);
	}

	// ── CcpHttpRequester (métodos default) ────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void requesterExecuteHttpRequestUrlNullTest() {
		requester().executeHttpRequest(null, CcpHttpMethods.GET, JSON, "{}", 200);
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterExecuteHttpRequestMethodNullTest() {
		requester().executeHttpRequest("http://localhost:9200", null, JSON, "{}", 200);
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterExecuteHttpRequestHeadersNullTest() {
		requester().executeHttpRequest("http://localhost:9200", CcpHttpMethods.GET, null, "{}", 200);
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterExecuteHttpRequestRequestNullTest() {
		requester().executeHttpRequest("http://localhost:9200", CcpHttpMethods.GET, JSON, null, 200);
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterExecuteHttpRequestNumbersNullTest() {
		requester().executeHttpRequest("http://localhost:9200", CcpHttpMethods.GET, JSON, "{}", (Integer[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterGetHttpErrorTraceNullTest() {
		requester().getHttpError(null, "url", CcpHttpMethods.GET, JSON, "{}", 500, "resposta", new HashSet<String>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterGetHttpErrorUrlNullTest() {
		requester().getHttpError("trace", null, CcpHttpMethods.GET, JSON, "{}", 500, "resposta", new HashSet<String>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterGetHttpErrorMethodNullTest() {
		requester().getHttpError("trace", "url", null, JSON, "{}", 500, "resposta", new HashSet<String>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterGetHttpErrorHeadersNullTest() {
		requester().getHttpError("trace", "url", CcpHttpMethods.GET, null, "{}", 500, "resposta",
				new HashSet<String>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterGetHttpErrorRequestNullTest() {
		requester().getHttpError("trace", "url", CcpHttpMethods.GET, JSON, null, 500, "resposta",
				new HashSet<String>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterGetHttpErrorStatusNullTest() {
		requester().getHttpError("trace", "url", CcpHttpMethods.GET, JSON, "{}", null, "resposta",
				new HashSet<String>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterGetHttpErrorResponseNullTest() {
		requester().getHttpError("trace", "url", CcpHttpMethods.GET, JSON, "{}", 500, null, new HashSet<String>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void requesterGetHttpErrorExpectedStatusNullTest() {
		requester().getHttpError("trace", "url", CcpHttpMethods.GET, JSON, "{}", 500, "resposta", null);
	}
}
