package com.ccp.especifications.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpHttpResponseTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void ctorStringTest() {
		CcpHttpResponse r = new CcpHttpResponse("{}", 200, "curl");
		assertTrue(r.isSuccess());
	}

	@Test
	public void ctorInputStreamTest() {
		InputStream is = new ByteArrayInputStream("{}".getBytes());
		CcpHttpResponse r = new CcpHttpResponse(is, 200, "curl");
		assertNotNull(r);
	}

	@Test
	public void isClientErrorTest() {
		assertTrue(new CcpHttpResponse("", 404, "").isClientError());
		assertFalse(new CcpHttpResponse("", 200, "").isClientError());
	}

	@Test
	public void isServerErrorTest() {
		assertTrue(new CcpHttpResponse("", 500, "").isServerError());
	}

	@Test
	public void asSingleJsonTest() {
		assertNotNull(new CcpHttpResponse("{\"a\":1}", 200, "").asSingleJson());
	}

	@Test
	public void asBase64Test() {
		assertNotNull(new CcpHttpResponse("body", 200, "").asBase64());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void ctorInputStreamNullTest() {
		new CcpHttpResponse((InputStream) null, 200, "");
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorInputStreamCurlNullTest() {
		new CcpHttpResponse(new ByteArrayInputStream("".getBytes()), 200, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorStringHttpResponseNullTest() {
		new CcpHttpResponse((String) null, 200, "");
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorStringCurlNullTest() {
		new CcpHttpResponse("", 200, null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// Todos os métodos públicos retornam String/JSON/List não-null naturalmente,
	// ou primitivos (boolean/int) fora do escopo do aspecto.
	// Manipular httpResponse via reflexão levaria a NPE nativa antes do aspecto.
}
