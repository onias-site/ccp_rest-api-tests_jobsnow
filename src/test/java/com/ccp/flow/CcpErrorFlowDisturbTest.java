package com.ccp.flow;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;

public class CcpErrorFlowDisturbTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void constructorStatusFieldsTest() {
		CcpErrorFlowDisturb error = new CcpErrorFlowDisturb(CcpProcessStatusDefault.OK);
		assertNotNull(error.status);
	}

	@Test
	public void constructorJsonStatusFieldsTest() {
		CcpErrorFlowDisturb error = new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK);
		assertNotNull(error.json);
	}

	@Test
	public void constructorJsonStatusMessageFieldsTest() {
		CcpErrorFlowDisturb error = new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK, "msg");
		assertNotNull(error.getMessage());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void ctorStatusNullTest() {
		new CcpErrorFlowDisturb((CcpProcessStatusDefault) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorStatusFieldsNullTest() {
		new CcpErrorFlowDisturb(CcpProcessStatusDefault.OK, (CcpJsonFieldName[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonStatusJsonNullTest() {
		new CcpErrorFlowDisturb((com.ccp.decorators.CcpJsonRepresentation) null, CcpProcessStatusDefault.OK);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonStatusStatusNullTest() {
		new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, (CcpProcessStatusDefault) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonStatusFieldsNullTest() {
		new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK, (CcpJsonFieldName[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonStatusMessageJsonNullTest() {
		new CcpErrorFlowDisturb(null, CcpProcessStatusDefault.OK, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonStatusMessageStatusNullTest() {
		new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, null, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonStatusMessageMessageNullTest() {
		new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK, (String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonStatusMessageFieldsNullTest() {
		new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK, "msg", (CcpJsonFieldName[]) null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// There are no public methods besides those inherited from RuntimeException; fields are public finals.
}
