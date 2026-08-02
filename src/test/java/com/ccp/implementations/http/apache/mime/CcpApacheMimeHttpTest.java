package com.ccp.implementations.http.apache.mime;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpApacheMimeHttpTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpApacheMimeHttp());
	}

	private static CcpHttpRequester getHttp() {
		return CcpDependencyInjection.getDependency(CcpHttpRequester.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpApacheMimeHttp());
	}

	@Test
	public void getInstanceTest() {
		CcpHttpRequester instance = new CcpApacheMimeHttp().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestUrlNullTest() {
		getHttp().executeHttpRequest(null, CcpHttpMethods.GET, CcpOtherConstants.EMPTY_JSON, "");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestMethodNullTest() {
		getHttp().executeHttpRequest("http://x", null, CcpOtherConstants.EMPTY_JSON, "");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestHeadersNullTest() {
		getHttp().executeHttpRequest("http://x", CcpHttpMethods.GET, null, "");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestBodyNullTest() {
		getHttp().executeHttpRequest("http://x", CcpHttpMethods.GET, CcpOtherConstants.EMPTY_JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartUrlNullTest() {
		getHttp().executeMultiPartHttpRequest(null, CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON,
				Collections.emptyList(), Collections.emptyList());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartMethodNullTest() {
		getHttp().executeMultiPartHttpRequest("http://x", null, CcpOtherConstants.EMPTY_JSON,
				Collections.emptyList(), Collections.emptyList());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartHeadersNullTest() {
		getHttp().executeMultiPartHttpRequest("http://x", CcpHttpMethods.POST, null,
				Collections.emptyList(), Collections.emptyList());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartBodyTextsNullTest() {
		getHttp().executeMultiPartHttpRequest("http://x", CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON,
				null, Collections.emptyList());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeMultiPartBodyBinariesNullTest() {
		getHttp().executeMultiPartHttpRequest("http://x", CcpHttpMethods.POST, CcpOtherConstants.EMPTY_JSON,
				Collections.emptyList(), null);
	}
}
