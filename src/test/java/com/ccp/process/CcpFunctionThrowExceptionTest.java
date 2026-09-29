package com.ccp.process;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;

public class CcpFunctionThrowExceptionTest {

	@Test(expected = IllegalStateException.class)
	public void applyThrowsExceptionTest() {
		new CcpFunctionThrowException(new IllegalStateException("x")).execute(CcpOtherConstants.EMPTY_JSON);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void constructorNullParamTest() {
		new CcpFunctionThrowException(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullParamTest() {
		new CcpFunctionThrowException(new RuntimeException()).execute(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// apply always throws an exception; it never returns null.
}
