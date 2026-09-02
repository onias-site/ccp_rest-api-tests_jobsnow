package com.ccp.flow;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;

public class CcpTreeFlowTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void beginThisStatementTest() {
		assertNotNull(CcpTreeFlow.beginThisStatement());
	}

	@Test
	public void fluenteEncadeamentoTest() {
		CcpBusiness noop = json -> json;
		CcpJsonRepresentation r = CcpTreeFlow.beginThisStatement()
				.tryToExecuteTheGivenFinalTargetProcess(noop)
				.usingTheGivenJson(CcpOtherConstants.EMPTY_JSON)
				.butIfThisExecutionReturns(CcpProcessStatusDefault.OK)
				.thenExecuteTheGivenProcesses(noop)
				.and()
				.endThisStatement();
		assertNotNull(r);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void tryToExecuteTheGivenFinalTargetProcessNullTest() {
		new CcpBeginThisStatement().tryToExecuteTheGivenFinalTargetProcess(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void usingTheGivenJsonNullTest() {
		new CcpTryToExecuteTheGivenFinalTargetProcess(json -> json).usingTheGivenJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void butIfThisExecutionReturnsNullTest() {
		new CcpUsingTheGivenJson(json -> json, CcpOtherConstants.EMPTY_JSON).butIfThisExecutionReturns(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void thenExecuteTheGivenProcessesNullTest() {
		new CcpIfThisExecutionReturns(json -> json, CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK, CcpOtherConstants.EMPTY_JSON)
				.thenExecuteTheGivenProcesses((CcpBusiness[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ifThisExecutionReturnsNullTest() {
		new CcpAndIfThisExecutionReturns(json -> json, CcpOtherConstants.EMPTY_JSON, CcpOtherConstants.EMPTY_JSON)
				.ifThisExecutionReturns(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void endThisStatementNullTest() {
		new CcpAndIfThisExecutionReturns(json -> json, CcpOtherConstants.EMPTY_JSON, CcpOtherConstants.EMPTY_JSON)
				.endThisStatement((CcpBusiness[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorTryToExecuteNullTest() {
		new CcpTryToExecuteTheGivenFinalTargetProcess(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorUsingTheGivenJsonProcessNullTest() {
		new CcpUsingTheGivenJson(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorUsingTheGivenJsonJsonNullTest() {
		new CcpUsingTheGivenJson(json -> json, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorIfThisExecutionReturnsProcessNullTest() {
		new CcpIfThisExecutionReturns(null, CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorIfThisExecutionReturnsJsonNullTest() {
		new CcpIfThisExecutionReturns(json -> json, null, CcpProcessStatusDefault.OK, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorIfThisExecutionReturnsStatusNullTest() {
		new CcpIfThisExecutionReturns(json -> json, CcpOtherConstants.EMPTY_JSON, null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorIfThisExecutionReturnsFlowNullTest() {
		new CcpIfThisExecutionReturns(json -> json, CcpOtherConstants.EMPTY_JSON, CcpProcessStatusDefault.OK, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorExecuteTheGivenProcessProcessNullTest() {
		new CcpExecuteTheGivenProcess(null, CcpOtherConstants.EMPTY_JSON, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorExecuteTheGivenProcessJsonNullTest() {
		new CcpExecuteTheGivenProcess(json -> json, null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorExecuteTheGivenProcessFlowNullTest() {
		new CcpExecuteTheGivenProcess(json -> json, CcpOtherConstants.EMPTY_JSON, null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// Todos os métodos retornam builders novos (`return new ...`) — nunca null naturalmente.
}
