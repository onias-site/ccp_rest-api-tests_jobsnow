package com.ccp.especifications.db.utils.entity.decorators.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Before;
import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpExceptionFlow;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpAfterDeleteFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpAfterSaveFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpAfterSaveFromTwinEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpBeforeSaveFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.transfers.CcpAfterCopyDataFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.transfers.CcpBeforeCopyDataFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDecoratorTypes;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityJobsnowWarning;

/**
 * Proves the flow of the side effect decorators of the entities, each one built from its own annotation by
 * {@code CcpEntityDecoratorTypes}: every business of the annotation runs, in order (until 2026-10-06 only the first
 * matching item ran); a handler of {@code @CcpExceptionFlow} also catches the subclasses of its exception; a handled
 * exception in the {@code before} phase cancels the operation; every instance of a repeated transfer annotation runs;
 * and an annotation that refers to the other side of the entity, to another operation or to another target does not
 * run. The entity under the decorators is a facade that answers the writes without reaching the database.
 */
public class EntityFlowTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	/** The businesses that ran, in order. */
	static final List<String> RUNS = new CopyOnWriteArrayList<>();

	/** Records {@code first}. */
	public static class RecordFirst implements CcpBusiness {
		/** The single instance: the class keeps no state. */
		public final static RecordFirst INSTANCE = new RecordFirst();

		private RecordFirst() {}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			RUNS.add("first");
			return json;
		}
	}

	/** Records {@code second}. */
	public static class RecordSecond implements CcpBusiness {
		/** The single instance: the class keeps no state. */
		public final static RecordSecond INSTANCE = new RecordSecond();

		private RecordSecond() {}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			RUNS.add("second");
			return json;
		}
	}

	/** Records {@code twin}. */
	public static class RecordTwin implements CcpBusiness {
		/** The single instance: the class keeps no state. */
		public final static RecordTwin INSTANCE = new RecordTwin();

		private RecordTwin() {}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			RUNS.add("twin");
			return json;
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
		/** The single instance: the class keeps no state. */
		public final static ThrowsTheSubclass INSTANCE = new ThrowsTheSubclass();

		private ThrowsTheSubclass() {}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			throw new MoreSpecificError();
		}
	}

	/** The handler: records {@code handled}. */
	public static class RecordHandled implements CcpBusiness {
		/** The single instance: the class keeps no state. */
		public final static RecordHandled INSTANCE = new RecordHandled();

		private RecordHandled() {}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			RUNS.add("handled");
			return json;
		}
	}

	/**
	 * Two businesses after the save of the main entity, one after the save of the twin (which must not run here), a
	 * business after the delete that throws the subclass, and a before the save whose handled exception cancels it.
	 */
	@CcpAfterSaveFromMainEntity(execute = {RecordFirst.class, RecordSecond.class}, operationHandlers = {})
	@CcpAfterSaveFromTwinEntity(execute = {RecordTwin.class}, operationHandlers = {})
	@CcpAfterDeleteFromMainEntity(execute = {ThrowsTheSubclass.class}, operationHandlers = {@CcpExceptionFlow(whenThrowing = ConfiguredError.class, thenExecute = {RecordHandled.class})})
	@CcpBeforeSaveFromMainEntity(execute = {ThrowsTheSubclass.class}, operationHandlers = {@CcpExceptionFlow(whenThrowing = ConfiguredError.class, thenExecute = {RecordHandled.class})})
	public static class FlowOperations {
		/** The entity of the annotations ({@code mainEntity} reads it from this field). */
		public static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;
	}

	/**
	 * A copy whose {@code before} throws a handled exception, two {@code after} instances for the same target and one for
	 * another target (which must not run).
	 */
	@CcpBeforeCopyDataFromMainEntity(targetEntity = JnEntityJobsnowWarning.class, execute = {ThrowsTheSubclass.class}, transferHandlers = {@CcpExceptionFlow(whenThrowing = ConfiguredError.class, thenExecute = {RecordHandled.class})})
	@CcpAfterCopyDataFromMainEntity(targetEntity = JnEntityJobsnowWarning.class, execute = {RecordFirst.class}, transferHandlers = {})
	@CcpAfterCopyDataFromMainEntity(targetEntity = JnEntityJobsnowWarning.class, execute = {RecordSecond.class}, transferHandlers = {})
	@CcpAfterCopyDataFromMainEntity(targetEntity = JnEntityJobsnowError.class, execute = {RecordTwin.class}, transferHandlers = {})
	public static class TransferOperations {
		/** The source entity of the annotations ({@code mainEntity} reads it from this field). */
		public static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;
	}

	/** The main entity, answering every write with {@code true} without reaching the database. */
	private static CcpEntity facade() {
		return new CcpEntityDelegator(JnEntityJobsnowError.ENTITY) {
			public boolean save(CcpJsonRepresentation json) {
				return true;
			}

			public boolean delete(CcpJsonRepresentation json) {
				return true;
			}

			public boolean copyDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
				return true;
			}
		};
	}

	@Before
	public void clearRuns() {
		RUNS.clear();
	}

	@Test
	public void everyBusinessOfTheAnnotationRunsInOrder() {
		CcpEntity decorated = CcpEntityDecoratorTypes.AfterSaveFromMainEntity.getEntity(FlowOperations.class, facade());

		boolean inserted = decorated.save(CcpOtherConstants.EMPTY_JSON);

		assertTrue(inserted);
		assertEquals(List.of("first", "second"), RUNS);
	}

	@Test
	public void anAnnotationOfTheOtherSideOfTheEntityDoesNotRun() {
		CcpEntity decorated = CcpEntityDecoratorTypes.AfterSaveFromTwinEntity.getEntity(FlowOperations.class, facade());

		decorated.save(CcpOtherConstants.EMPTY_JSON);

		assertTrue("the side effect of the twin ran on the main entity: " + RUNS, RUNS.isEmpty());
	}

	@Test
	public void anAnnotationOfAnotherOperationDoesNotRun() {
		CcpEntity decorated = CcpEntityDecoratorTypes.AfterDeleteFromMainEntity.getEntity(FlowOperations.class, facade());

		decorated.save(CcpOtherConstants.EMPTY_JSON);

		assertTrue("the side effect of the delete ran on a save: " + RUNS, RUNS.isEmpty());
	}

	@Test
	public void aSubclassOfAConfiguredExceptionIsHandled() {
		CcpEntity decorated = CcpEntityDecoratorTypes.AfterDeleteFromMainEntity.getEntity(FlowOperations.class, facade());

		boolean deleted = decorated.delete(CcpOtherConstants.EMPTY_JSON);

		assertTrue("an exception handled after the delete does not undo it", deleted);
		assertEquals("the subclass escaped the handler", List.of("handled"), RUNS);
	}

	@Test
	public void aHandledExceptionInTheBeforeOfAWriteCancelsIt() {
		CcpEntity decorated = CcpEntityDecoratorTypes.BeforeSaveFromMainEntity.getEntity(FlowOperations.class, facade());

		boolean saved = decorated.save(CcpOtherConstants.EMPTY_JSON);

		assertFalse("the save went on after a handled exception", saved);
		assertEquals("the handler ran", List.of("handled"), RUNS);
	}

	/** Decided with the user on 2026-10-06: as in the write operations, a handled exception in the before cancels. */
	@Test
	public void aHandledExceptionInTheBeforeOfATransferCancelsIt() {
		CcpEntity decorated = CcpEntityDecoratorTypes.BeforeCopyDataFromMainEntity.getEntity(TransferOperations.class, facade());

		boolean copied = decorated.copyDataTo(CcpOtherConstants.EMPTY_JSON, JnEntityJobsnowWarning.ENTITY);

		assertFalse("the transfer went on after a handled exception", copied);
		assertEquals("the handler ran", List.of("handled"), RUNS);
	}

	@Test
	public void everyRepeatedTransferAnnotationOfTheTargetRuns() {
		CcpEntity decorated = CcpEntityDecoratorTypes.AfterCopyDataFromMainEntity.getEntity(TransferOperations.class, facade());

		boolean copied = decorated.copyDataTo(CcpOtherConstants.EMPTY_JSON, JnEntityJobsnowWarning.ENTITY);

		assertTrue(copied);
		assertEquals("the instance of another target must not run", List.of("first", "second"), RUNS);
	}

	@Test
	public void aTransferAnnotationDoesNotRunOnAnotherTransfer() {
		CcpEntity decorated = CcpEntityDecoratorTypes.AfterCopyDataFromMainEntity.getEntity(TransferOperations.class, facade());

		decorated.save(CcpOtherConstants.EMPTY_JSON);

		assertTrue(RUNS.isEmpty());
	}
}
