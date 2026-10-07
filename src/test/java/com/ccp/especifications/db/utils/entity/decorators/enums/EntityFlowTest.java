package com.ccp.especifications.db.utils.entity.decorators.enums;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOperation;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOperations;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpExceptionFlow;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Proves two fixes of 2026-10-06 in the flow of the entity decorators: every {@code @CcpEntityOperation} that matches the
 * operation, entity and phase runs (only the first one used to run, the others were ignored in silence), and a handler of
 * {@code @CcpExceptionFlow} also catches the subclasses of its exception (only the exact class used to be looked up).
 */
public class EntityFlowTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** Puts {@code first}. */
	public static class PutFirst implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json.put(new CcpFieldName("first"), true);
		}
	}

	/** Puts {@code second}. */
	public static class PutSecond implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json.put(new CcpFieldName("second"), true);
		}
	}

	/** The exception configured in the handler. */
	@SuppressWarnings("serial")
	public static class ConfiguredError extends RuntimeException {
	}

	/** A subclass of the configured exception, the one actually thrown. */
	@SuppressWarnings("serial")
	public static class MoreSpecificError extends ConfiguredError {
	}

	/** Throws the subclass. */
	public static class ThrowsTheSubclass implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			throw new MoreSpecificError();
		}
	}

	/** The handler: marks the JSON as handled. */
	public static class MarkHandled implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json.put(new CcpFieldName("handled"), true);
		}
	}

	/** Two items for the same operation, entity and phase, and one item whose business throws the subclass. */
	@CcpEntityOperations({
		@CcpEntityOperation(operationType = CcpEntityOperationType.afterSaveFromMainEntity, execute = {PutFirst.class}, operationHandlers = {}),
		@CcpEntityOperation(operationType = CcpEntityOperationType.afterSaveFromMainEntity, execute = {PutSecond.class}, operationHandlers = {}),
		@CcpEntityOperation(operationType = CcpEntityOperationType.afterDeleteFromMainEntity, execute = {ThrowsTheSubclass.class},
				operationHandlers = {@CcpExceptionFlow(whenThrowing = ConfiguredError.class, thenExecute = {MarkHandled.class})}),
	})
	public static class FlowOperations {
		/** The entity of the items ({@code mainEntity} reads it from this field). */
		public static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;
	}

	/** Counts the runs of the handler of the transfer. */
	static final java.util.concurrent.atomic.AtomicInteger TRANSFER_HANDLER_RUNS = new java.util.concurrent.atomic.AtomicInteger();

	/** The handler of the transfer: counts its runs. */
	public static class CountTransferHandler implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			TRANSFER_HANDLER_RUNS.incrementAndGet();
			return json;
		}
	}

	/**
	 * A copy whose {@code before} throws a handled exception, and two {@code after} items for the same transfer, target
	 * and phase.
	 */
	@com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDataTransfers({
		@com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDataTransfer(operationType = CcpEntityDataTransferType.beforeCopyDataFromMainEntity,
				targetEntity = com.jn.entities.JnEntityJobsnowWarning.class, execute = {ThrowsTheSubclass.class},
				transferHandlers = {@CcpExceptionFlow(whenThrowing = ConfiguredError.class, thenExecute = {CountTransferHandler.class})}),
		@com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDataTransfer(operationType = CcpEntityDataTransferType.afterCopyDataFromMainEntity,
				targetEntity = com.jn.entities.JnEntityJobsnowWarning.class, execute = {PutFirst.class}, transferHandlers = {}),
		@com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDataTransfer(operationType = CcpEntityDataTransferType.afterCopyDataFromMainEntity,
				targetEntity = com.jn.entities.JnEntityJobsnowWarning.class, execute = {PutSecond.class}, transferHandlers = {}),
	})
	public static class TransferOperations {
		/** The source entity of the items ({@code mainEntity} reads it from this field). */
		public static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;
	}

	/** Decided with the user on 2026-10-06: as in the write operations, a handled exception in the before cancels. */
	@Test
	public void aHandledExceptionInTheBeforeOfATransferCancelsIt() {
		int runsBefore = TRANSFER_HANDLER_RUNS.get();

		boolean transferred = CcpEntityDecoratorTransferType.copyDataTo.executeBefore(CcpOtherConstants.EMPTY_JSON,
				TransferOperations.class, TransferOperations.ENTITY, com.jn.entities.JnEntityJobsnowWarning.ENTITY);

		org.junit.Assert.assertFalse("the transfer went on after a handled exception", transferred);
		org.junit.Assert.assertEquals("the handler ran", runsBefore + 1, TRANSFER_HANDLER_RUNS.get());
	}

	@Test
	public void everyTransferItemOfTheSameTypeTargetAndPhaseRuns() {
		CcpJsonRepresentation result = CcpEntityDecoratorTransferType.copyDataTo.executeFlow(CcpOtherConstants.EMPTY_JSON,
				CcpEntityOperationPhase._after, TransferOperations.class, TransferOperations.ENTITY, com.jn.entities.JnEntityJobsnowWarning.ENTITY);

		assertTrue(result.toString(), result.getAsBoolean(new CcpFieldName("first")));
		assertTrue("the second transfer item used to be ignored: " + result, result.getAsBoolean(new CcpFieldName("second")));
	}

	@Test
	public void everyItemOfTheSameOperationEntityAndPhaseRuns() {
		CcpJsonRepresentation result = CcpEntityDecoratorOperationType.save.executeFlow(CcpOtherConstants.EMPTY_JSON,
				CcpEntityOperationPhase._after, FlowOperations.class, FlowOperations.ENTITY);

		assertTrue(result.toString(), result.getAsBoolean(new CcpFieldName("first")));
		assertTrue("the second item used to be ignored: " + result, result.getAsBoolean(new CcpFieldName("second")));
	}

	@Test
	public void aSubclassOfAConfiguredExceptionIsHandled() {
		CcpJsonRepresentation result = CcpEntityDecoratorOperationType.delete.executeFlow(CcpOtherConstants.EMPTY_JSON,
				CcpEntityOperationPhase._after, FlowOperations.class, FlowOperations.ENTITY);

		assertTrue("the subclass escaped the handler: " + result, result.getAsBoolean(new CcpFieldName("handled")));
	}
}
