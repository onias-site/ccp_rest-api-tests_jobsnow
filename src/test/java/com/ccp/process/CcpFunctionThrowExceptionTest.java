package com.ccp.process;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;

public class CcpFunctionThrowExceptionTest {

	@Test(expected = IllegalStateException.class)
	public void applyLancaExcecaoTest() {
		new CcpFunctionThrowException(new IllegalStateException("x")).apply(CcpOtherConstants.EMPTY_JSON);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorNullParamTest() {
		new CcpFunctionThrowException(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullParamTest() {
		new CcpFunctionThrowException(new RuntimeException()).apply(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// apply sempre lança exceção; nunca retorna null.
}
