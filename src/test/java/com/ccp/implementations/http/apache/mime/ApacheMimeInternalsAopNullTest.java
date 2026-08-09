package com.ccp.implementations.http.apache.mime;

import java.io.IOException;

import org.apache.http.protocol.BasicHttpContext;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre as classes internas do módulo
 * {@code ccp_http_apache-mime}.
 */
public class ApacheMimeInternalsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpApacheMimeHttp());
	}

	// ── CcpHttpRequestRetryHandler ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void retryRequestExceptionNullTest() {
		new CcpHttpRequestRetryHandler().retryRequest(null, 1, new BasicHttpContext());
	}

	@Test(expected = CcpNullParameterException.class)
	public void retryRequestContextNullTest() {
		new CcpHttpRequestRetryHandler().retryRequest(new IOException("falha"), 1, null);
	}

	// ── HttpMethod ────────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getMethodUrlNullTest() {
		HttpMethod.POST.getMethod(null, CcpOtherConstants.EMPTY_JSON, "{}");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMethodHeadersNullTest() {
		HttpMethod.POST.getMethod("http://localhost:9200", null, "{}");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMethodBodyNullTest() {
		HttpMethod.POST.getMethod("http://localhost:9200", CcpOtherConstants.EMPTY_JSON, null);
	}
}
