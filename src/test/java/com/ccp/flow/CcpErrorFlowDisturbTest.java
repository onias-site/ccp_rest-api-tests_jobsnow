package com.ccp.flow;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;

public class CcpErrorFlowDisturbTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void construtorStatusFieldsTest() {
		CcpErrorFlowDisturb e = new CcpErrorFlowDisturb(CcpProcessStatusDefault.OK);
		assertNotNull(e.status);
	}

	@Test
	public void construtorJsonStatusFieldsTest() {
		CcpErrorFlowDisturb e = new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK);
		assertNotNull(e.json);
	}

	@Test
	public void construtorJsonStatusMessageFieldsTest() {
		CcpErrorFlowDisturb e = new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK, "msg");
		assertNotNull(e.getMessage());
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
	// Não há métodos públicos além dos herdados de RuntimeException; campos são finals públicos.
}
