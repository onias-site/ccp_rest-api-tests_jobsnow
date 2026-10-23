package com.ccp.especifications.db.utils.entity.decorators.enums;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpExceptionFlow;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpAfterSaveFromMainEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Coverage of {@code CcpNullParameterAspect} over the entity decorator enums
 * ({@code CcpEntityDecoratorOperationType}, {@code CcpEntityDecoratorTransferType}, {@code CcpEntityOperationPhase},
 * {@code CcpEntitySideEffectBusinesses}, {@code CcpEntityAnnotationAttribute}, {@code CcpEntityExpurgableOptions}) and
 * over the default methods of {@code OperationWriter}.
 */
public class CcpEntityDecoratorEnumsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static final BiFunction<CcpJsonRepresentation, CcpEntityDecoratorOperationType, CcpJsonRepresentation> WRITE_SIDE_EFFECT = (json, operation) -> json;

	private static final BiFunction<CcpJsonRepresentation, CcpEntity, CcpJsonRepresentation> TRANSFER_SIDE_EFFECT = (json, target) -> json;

	/** Carries a side effect annotation, whose instance the side effect receives. */
	@CcpAfterSaveFromMainEntity(execute = {}, operationHandlers = {})
	private static class Operations {
	}

	// -- CcpEntityDecoratorOperationType -------------------------------------

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteBeforeJsonNullTest() {
		CcpEntityDecoratorOperationType.save.executeBefore(null, ENTITY, WRITE_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteBeforeEntityNullTest() {
		CcpEntityDecoratorOperationType.save.executeBefore(JSON, null, WRITE_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteBeforeSideEffectNullTest() {
		CcpEntityDecoratorOperationType.save.executeBefore(JSON, ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteAfterJsonNullTest() {
		CcpEntityDecoratorOperationType.save.executeAfter(null, ENTITY, WRITE_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteAfterEntityNullTest() {
		CcpEntityDecoratorOperationType.save.executeAfter(JSON, null, WRITE_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeExecuteAfterSideEffectNullTest() {
		CcpEntityDecoratorOperationType.save.executeAfter(JSON, ENTITY, null);
	}

	// -- CcpEntityDecoratorTransferType --------------------------------------

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteBeforeJsonNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeBefore(null, ENTITY, ENTITY, TRANSFER_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteBeforeEntityNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeBefore(JSON, null, ENTITY, TRANSFER_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteBeforeEntityToTransferNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeBefore(JSON, ENTITY, null, TRANSFER_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteBeforeSideEffectNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeBefore(JSON, ENTITY, ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteAfterJsonNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeAfter(null, ENTITY, ENTITY, TRANSFER_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteAfterEntityNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeAfter(JSON, null, ENTITY, TRANSFER_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteAfterEntityToTransferNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeAfter(JSON, ENTITY, null, TRANSFER_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTypeExecuteAfterSideEffectNullTest() {
		CcpEntityDecoratorTransferType.copyDataTo.executeAfter(JSON, ENTITY, ENTITY, null);
	}

	// -- CcpEntityOperationPhase ---------------------------------------------

	@Test(expected = CcpNullParameterException.class)
	public void phaseExecuteWriteOperationNullTest() {
		CcpEntityOperationPhase._before.executeWrite(null, JSON, ENTITY, WRITE_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void phaseExecuteWriteJsonNullTest() {
		CcpEntityOperationPhase._after.executeWrite(CcpEntityDecoratorOperationType.save, null, ENTITY, WRITE_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void phaseExecuteTransferTransferNullTest() {
		CcpEntityOperationPhase._before.executeTransfer(null, JSON, ENTITY, ENTITY, TRANSFER_SIDE_EFFECT);
	}

	@Test(expected = CcpNullParameterException.class)
	public void phaseExecuteTransferTargetNullTest() {
		CcpEntityOperationPhase._after.executeTransfer(CcpEntityDecoratorTransferType.copyDataTo, JSON, ENTITY, null, TRANSFER_SIDE_EFFECT);
	}

	// -- CcpEntitySideEffectBusinesses and CcpEntityAnnotationAttribute -------

	@Test(expected = CcpNullParameterException.class)
	public void sideEffectBusinessesJsonNullTest() {
		Annotation annotation = Operations.class.getAnnotation(CcpAfterSaveFromMainEntity.class);
		CcpEntitySideEffectBusinesses.operation.execute(null, annotation, CcpEntityOperationPhase._after);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sideEffectBusinessesAnnotationNullTest() {
		CcpEntitySideEffectBusinesses.operation.execute(JSON, null, CcpEntityOperationPhase._after);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sideEffectBusinessesPhaseNullTest() {
		Annotation annotation = Operations.class.getAnnotation(CcpAfterSaveFromMainEntity.class);
		CcpEntitySideEffectBusinesses.operation.execute(JSON, annotation, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void annotationAttributeReadFromNullTest() {
		CcpEntityAnnotationAttribute.value.readFrom(null);
	}

	// -- OperationWriter (default methods) -----------------------------------

	private static OperationWriter operationWriter() {
		return CcpEntitySideEffectBusinesses.operation;
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

	// -- CcpEntityExpurgableOptions ------------------------------------------

	@Test(expected = CcpNullParameterException.class)
	public void getMillisecondsNullTest() {
		CcpEntityExpurgableOptions.daily.getMilliseconds(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMillisecondsOverriddenNullTest() {
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
