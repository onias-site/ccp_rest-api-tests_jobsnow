package com.ccp.especifications.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;

public class CcpHttpBodyTests {

	// ── CcpHttpBodyBinary ────────────────────────────────────────────────────

	@Test
	public void bodyBinaryCtorTest() {
		CcpHttpBodyBinary bodyBinary = new CcpHttpBodyBinary(CcpHttpContentType.TEXT_PLAIN, "n", "f.txt", new Byte[]{1, 2, 3});
		assertEquals(3, bodyBinary.getBytes().length);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bodyBinaryCtorContentTypeNullTest() {
		new CcpHttpBodyBinary(null, "n", "f.txt", new Byte[]{});
	}

	@Test(expected = CcpNullParameterException.class)
	public void bodyBinaryCtorNameNullTest() {
		new CcpHttpBodyBinary(CcpHttpContentType.TEXT_PLAIN, null, "f.txt", new Byte[]{});
	}

	@Test(expected = CcpNullParameterException.class)
	public void bodyBinaryCtorFileNameNullTest() {
		new CcpHttpBodyBinary(CcpHttpContentType.TEXT_PLAIN, "n", null, new Byte[]{});
	}

	@Test(expected = CcpNullParameterException.class)
	public void bodyBinaryCtorBytesNullTest() {
		new CcpHttpBodyBinary(CcpHttpContentType.TEXT_PLAIN, "n", "f.txt", null);
	}

	// ── CcpHttpBodyText ──────────────────────────────────────────────────────

	@Test
	public void bodyTextCtorTest() {
		CcpHttpBodyText bodyText = new CcpHttpBodyText(CcpHttpContentType.TEXT_PLAIN, "n", "t");
		assertNotNull(bodyText);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bodyTextCtorContentTypeNullTest() {
		new CcpHttpBodyText(null, "n", "t");
	}

	@Test(expected = CcpNullParameterException.class)
	public void bodyTextCtorNameNullTest() {
		new CcpHttpBodyText(CcpHttpContentType.TEXT_PLAIN, null, "t");
	}

	@Test(expected = CcpNullParameterException.class)
	public void bodyTextCtorTextNullTest() {
		new CcpHttpBodyText(CcpHttpContentType.TEXT_PLAIN, "n", null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// CcpHttpBodyBinary.getBytes() converts to a primitive byte[] — not subject to the aspect.
	// CcpHttpBodyText has no public method.
}
