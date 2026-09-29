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
	// Note: getMessageDigest() can never return null because it delegates to
	// MessageDigest.getInstance, which either returns a valid instance or throws an exception.
	// The instance (enum) getMessageDigest() uses a cache — it only returns null if
	// the cache/algorithm were tampered with via reflection, which would be artificial.
}
