package com.ccp.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.flow.CcpErrorFlowDisturb;

public class CcpProcessStatusTest {

	@Test
	public void asNumberTest() {
		assertEquals(200, CcpProcessStatusDefault.OK.asNumber());
	}

	@Test
	public void asJsonFieldNameTest() {
		assertNotNull(CcpProcessStatusDefault.OK.asJsonFieldName());
	}

	@Test
	public void verifyStatusCorrectTest() {
		String verifiedStatusName = CcpProcessStatusDefault.OK.verifyStatus(200, "ok");
		assertEquals("OK", verifiedStatusName);
	}

	@Test(expected = RuntimeException.class)
	public void verifyStatusMismatchThrowsExceptionTest() {
		CcpProcessStatusDefault.OK.verifyStatus(500, "diff");
	}

	@Test
	public void verifyStatusNamesCorrectTest() {
		CcpProcessStatus processStatus = CcpProcessStatusDefault.OK.verifyStatusNames(200, "OK");
		assertNotNull(processStatus);
	}

	@Test(expected = CcpErrorFlowDisturb.class)
	public void throwExceptionTest() {
		CcpProcessStatusDefault.OK.throwException(CcpOtherConstants.EMPTY_JSON);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void verifyStatusMessageNullTest() {
		CcpProcessStatusDefault.OK.verifyStatus(200, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void verifyStatusNamesActualStatusNameNullTest() {
		CcpProcessStatusDefault.OK.verifyStatusNames(200, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void throwExceptionNullParamTest() {
		CcpProcessStatusDefault.OK.throwException(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// asNumber returns a primitive int. verifyStatus returns a String — naturally never null.
	// verifyStatusNames returns this — never null. asJsonFieldName returns new CcpFieldName — never null.
}
