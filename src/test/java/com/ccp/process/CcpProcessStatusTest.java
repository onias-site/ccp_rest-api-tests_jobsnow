package com.ccp.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.flow.CcpErrorFlowDisturb;

public class CcpProcessStatusTest {

	// throwException builds a JSON: without the handler loaded here the class failed when run alone
	static {
		com.ccp.dependency.injection.CcpDependencyInjection.loadAllDependencies(new com.ccp.implementations.json.gson.CcpGsonJsonHandler());
	}

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

	/**
	 * Finding 7: a '%' in the message is kept as text. Until 2026-10-07 the message was concatenated into the format of
	 * String.format, so a percentage or an e-mail encoded as %40 raised UnknownFormatConversionException instead of the
	 * report of the mismatch.
	 */
	@Test
	public void aMessageWithPercentSignsIsReportedAsItIs() {
		String message = "100% of the fields, user%40jobsnow.com, rate of %s";
		try {
			CcpProcessStatusDefault.OK.verifyStatus(422, message);
			org.junit.Assert.fail("the status differs");
		} catch (CcpProcessStatus.UnexpectedProcessStatus expected) {
			String report = expected.getMessage();
			org.junit.Assert.assertTrue(report, report.endsWith("Message: " + message));
			org.junit.Assert.assertTrue(report, report.contains("'200'") && report.contains("'422'"));
		}
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
