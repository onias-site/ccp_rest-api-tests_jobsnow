package com.ccp.especifications.http;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * An unexpected HTTP status must throw {@link CcpErrorHttp}. From 2026-07-31 (commit cd97ba0) to
 * 2026-09-27 this did not happen: the absence of an alternative flow came to be represented by
 * {@code DO_NOTHING} instead of {@code null}, and every 400/404/500 was treated as success — that is
 * what hid, for two months, that the versionable history purge never deleted anything.
 */
public class CcpHttpHandlerStatusTest {

	private static int responseStatus;

	@Before
	public void loadDependencies() {
		CcpInstanceProvider<CcpHttpRequester> fakeRequester = () -> new RespondingRequester();
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), fakeRequester);
	}

	@Test
	public void expectedStatusReturnsResponse() {
		responseStatus = 200;
		CcpJsonRepresentation response = new CcpHttpHandler(200, "http://test").executeHttpSimplifiedGet("test", CcpHttpResponseType.singleRecord);
		assertEquals("ok", response.getAsString(TestFields.resultado));
	}

	@Test(expected = CcpErrorHttp.class)
	public void unexpectedStatusThrowsError() {
		responseStatus = 400;
		new CcpHttpHandler(200, "http://test").executeHttpSimplifiedGet("test", CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpErrorHttp.class)
	public void statusOutsideFlowMapThrowsError() {
		responseStatus = 500;
		new CcpHttpHandler(this.flowsFor200And404(), "http://test").executeHttpSimplifiedGet("test", CcpHttpResponseType.singleRecord);
	}

	@Test
	public void statusInsideFlowMapProceeds() {
		responseStatus = 404;
		CcpJsonRepresentation response = new CcpHttpHandler(this.flowsFor200And404(), "http://test").executeHttpSimplifiedGet("test", CcpHttpResponseType.singleRecord);
		assertEquals("ok", response.getAsString(TestFields.resultado));
	}

	@Test
	public void declaredAlternativeFlowReceivesUnexpectedStatus() {
		responseStatus = 400;
		CcpJsonRepresentation response = new CcpHttpHandler(200, json -> json.put(TestFields.resultado, "alternative"), "http://test").executeHttpSimplifiedGet("test", CcpHttpResponseType.singleRecord);
		assertEquals("alternative", response.getAsString(TestFields.resultado));
	}

	private CcpJsonRepresentation flowsFor200And404() {
		CcpJsonRepresentation flows = CcpOtherConstants.EMPTY_JSON
				.addJsonTransformer(200, CcpOtherConstants.DO_NOTHING)
				.addJsonTransformer(404, CcpOtherConstants.DO_NOTHING);
		return flows;
	}

	enum TestFields implements com.ccp.decorators.CcpJsonFieldName {
		resultado
	}

	static class RespondingRequester implements CcpHttpRequester {

		public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
			CcpHttpResponse response = new CcpHttpResponse("{\"resultado\":\"ok\"}", responseStatus, "curl " + url);
			return response;
		}

		public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
			return this.executeHttpRequest(url, method, headers, "");
		}
	}
}
