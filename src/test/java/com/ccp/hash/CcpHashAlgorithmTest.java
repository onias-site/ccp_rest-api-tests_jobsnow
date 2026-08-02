package com.ccp.hash;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;

public class CcpHashAlgorithmTest {

	@Test
	public void getMessageDigestNoArgsMd5Test() {
		assertNotNull(CcpHashAlgorithm.MD5.getMessageDigest());
	}

	@Test
	public void getMessageDigestNoArgsSha1Test() {
		assertNotNull(CcpHashAlgorithm.SHA1.getMessageDigest());
	}

	@Test
	public void getMessageDigestNoArgsSha256Test() {
		assertNotNull(CcpHashAlgorithm.SHA256.getMessageDigest());
	}

	@Test
	public void getMessageDigestNoArgsSha512Test() {
		assertNotNull(CcpHashAlgorithm.SHA512.getMessageDigest());
	}

	@Test
	public void getMessageDigestStaticTest() {
		assertNotNull(CcpHashAlgorithm.getMessageDigest("MD5"));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getMessageDigestStaticNullParamTest() {
		CcpHashAlgorithm.getMessageDigest((String) null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// Nota: getMessageDigest() nunca pode retornar null porque delega para
	// MessageDigest.getInstance que ou retorna instância válida ou lança exceção.
	// getMessageDigest() de instância (enum) usa cache — só retorna null se
	// o cache/algorithm fossem manipulados por reflexão, o que seria artificial.
}
