package com.ccp.especifications.db.utils.entity.decorators.enums;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpExceptionFlow;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityLoginTokenRequestResend;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os enums de decorators de entidade
 * ({@code CcpEntityDecoratorOperationType}, {@code CcpEntityDecoratorTransferType},
 * {@code CcpEntityExpurgableOptions}) e sobre os métodos default de {@code OperationWriter}.
 */
public class CcpEntityDecoratorEnumsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static final Class<?> OPERATIONS_CLASS = JnEntityLoginTokenRequestResend.class;

	// ── CcpEntityDecoratorOperationType ───────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteJsonNullTest() {
		CcpEntityDecoratorOperationType.save.execute(null, OPERATIONS_CLASS, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteClassNullTest() {
		CcpEntityDecoratorOperationType.save.execute(JSON, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteEntityNullTest() {
		CcpEntityDecoratorOperationType.save.execute(JSON, OPERATIONS_CLASS, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteEntitiesNullTest() {
		CcpEntityDecoratorOperationType.save.execute(JSON, OPERATIONS_CLASS, ENTITY, (CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteFlowJsonNullTest() {
		CcpEntityDecoratorOperationType.save.executeFlow(null, CcpEntityOperationStepType.antes, OPERATIONS_CLASS,
				ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteFlowWhenNullTest() {
		CcpEntityDecoratorOperationType.save.executeFlow(JSON, null, OPERATIONS_CLASS, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteFlowClassNullTest() {
		CcpEntityDecoratorOperationType.save.executeFlow(JSON, CcpEntityOperationStepType.antes, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteFlowEntityNullTest() {
		CcpEntityDecoratorOperationType.save.executeFlow(JSON, CcpEntityOperationStepType.antes, OPERATIONS_CLASS,
				null);
	}

	// ── CcpEntityDecoratorTransferType ────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteJsonNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.execute(null, OPERATIONS_CLASS, ENTITY, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteClassNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.execute(JSON, null, ENTITY, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteEntityNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.execute(JSON, OPERATIONS_CLASS, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteEntityToTransferNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.execute(JSON, OPERATIONS_CLASS, ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteFlowJsonNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeFlow(null, CcpEntityOperationStepType.antes, OPERATIONS_CLASS,
				ENTITY, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteFlowWhenNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeFlow(JSON, null, OPERATIONS_CLASS, ENTITY, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteFlowClassNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeFlow(JSON, CcpEntityOperationStepType.antes, null, ENTITY,
				ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteFlowEntityNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeFlow(JSON, CcpEntityOperationStepType.antes, OPERATIONS_CLASS,
				null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteFlowEntityToTransferNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeFlow(JSON, CcpEntityOperationStepType.antes, OPERATIONS_CLASS,
				ENTITY, null);
	}

	// ── OperationWriter (métodos default) ─────────────────────────────────────

	private static OperationWriter operationWriter() {
		return CcpEntityDecoratorOperationType.save;
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeBusinessJsonNullTest() {
		operationWriter().executeBusiness(null, json -> json, new HashMap<Class<?>, List<CcpBusiness>>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeBusinessBusinessNullTest() {
		operationWriter().executeBusiness(JSON, null, new HashMap<Class<?>, List<CcpBusiness>>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeBusinessHandlersNullTest() {
		operationWriter().executeBusiness(JSON, json -> json, (Map<Class<?>, List<CcpBusiness>>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getExceptionHandlersNullTest() {
		operationWriter().getExceptionHandlers((CcpExceptionFlow[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getBusinessNullTest() {
		operationWriter().getBusiness(null);
	}

	// ── CcpEntityExpurgableOptions ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getMillisecondsNullTest() {
		CcpEntityExpurgableOptions.daily.getMilliseconds(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMillisecondsSobrescritoNullTest() {
		CcpEntityExpurgableOptions.yearly.getMilliseconds(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getFormattedDateNullTest() {
		CcpEntityExpurgableOptions.daily.getFormattedDate(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getNextTimeStampNullTest() {
		CcpEntityExpurgableOptions.daily.getNextTimeStamp(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getNextDateNullTest() {
		CcpEntityExpurgableOptions.daily.getNextDate(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMillisecondsComFieldNullTest() {
		CcpEntityExpurgableOptions.daily.getMilliseconds(null, java.util.Calendar.DAY_OF_MONTH);
	}
}
