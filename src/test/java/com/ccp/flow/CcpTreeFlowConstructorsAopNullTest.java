package com.ccp.flow;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.process.CcpProcessStatusDefault;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os construtores das etapas do fluent chain de
 * {@code CcpTreeFlow}, que só são alcançáveis de dentro do próprio pacote.
 */
public class CcpTreeFlowConstructorsAopNullTest {

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	@Test(expected = CcpNullParameterException.class)
	public void tryToExecuteConstrutorNullTest() {
		new CcpTryToExecuteTheGivenFinalTargetProcess(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void usingTheGivenJsonProcessNullTest() {
		new CcpUsingTheGivenJson(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void usingTheGivenJsonJsonNullTest() {
		new CcpUsingTheGivenJson(json -> json, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ifThisExecutionReturnsProcessNullTest() {
		new CcpIfThisExecutionReturns(null, JSON, CcpProcessStatusDefault.OK, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ifThisExecutionReturnsJsonNullTest() {
		new CcpIfThisExecutionReturns(json -> json, null, CcpProcessStatusDefault.OK, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ifThisExecutionReturnsStatusNullTest() {
		new CcpIfThisExecutionReturns(json -> json, JSON, null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ifThisExecutionReturnsFlowNullTest() {
		new CcpIfThisExecutionReturns(json -> json, JSON, CcpProcessStatusDefault.OK, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeTheGivenProcessProcessNullTest() {
		new CcpExecuteTheGivenProcess(null, JSON, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeTheGivenProcessJsonNullTest() {
		new CcpExecuteTheGivenProcess(json -> json, null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeTheGivenProcessFlowNullTest() {
		new CcpExecuteTheGivenProcess(json -> json, JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andIfThisExecutionReturnsProcessNullTest() {
		new CcpAndIfThisExecutionReturns(null, JSON, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andIfThisExecutionReturnsJsonNullTest() {
		new CcpAndIfThisExecutionReturns(json -> json, null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andIfThisExecutionReturnsFlowNullTest() {
		new CcpAndIfThisExecutionReturns(json -> json, JSON, null);
	}
}
