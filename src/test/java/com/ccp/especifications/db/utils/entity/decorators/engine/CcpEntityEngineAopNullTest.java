package com.ccp.especifications.db.utils.entity.decorators.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityContactUs;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import org.junit.Test;

/**
 * Cobertura dos aspectos {@code CcpNullParameterAspect} / {@code CcpNullReturnAspect} sobre o motor
 * de entidades: {@code CcpEntityDelegator}, {@code CcpDefaultEntityDelegator}, {@code CcpEntityFactory},
 * {@code CcpEntityMetaData}, {@code DefaultImplementationEntity} e todos os decorators do pacote.
 */
public class CcpEntityEngineAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	/** Subclasse concreta mínima para alcançar os métodos de {@code CcpDefaultEntityDelegator}. */


	static CcpExecuteBulkOperation bulkOperation() {
		return com.jn.db.bulk.JnExecuteBulkOperation.INSTANCE;
	}

	static Consumer<String[]> keysToDelete() {
		return com.jn.utils.JnDeleteKeysFromCache.INSTANCE;
	}

	private static CcpEntityDelegator delegator() {
		return new CcpEntityDelegator(ENTITY);
	}

	private static DefaultDelegatorForTest defaultDelegator() {
		return new DefaultDelegatorForTest();
	}

	private static CcpEntityMetaData metaData() {
		return ENTITY.getEntityMetaData();
	}

	// ── CcpEntityDelegator ────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void delegatorConstrutorNullTest() {
		new CcpEntityDelegator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorCalculateIdNullTest() {
		delegator().calculateId(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorDeleteNullTest() {
		delegator().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorDeleteAnyWhereNullTest() {
		delegator().deleteAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorGetOneByIdNullTest() {
		delegator().getOneById(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorGetOneByIdAnyWhereNullTest() {
		delegator().getOneByIdAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorGetParametersToSearchNullTest() {
		delegator().getParametersToSearch(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorIsPresentInThisUnionAllUnionNullTest() {
		delegator().isPresentInThisUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorIsPresentInThisUnionAllJsonNullTest() {
		delegator().isPresentInThisUnionAll(unionAll(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorSaveNullTest() {
		delegator().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorEqualsNullTest() {
		delegator().equals(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorExistsNullTest() {
		delegator().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorGetHandledJsonNullTest() {
		delegator().getHandledJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorToBulkItemsJsonNullTest() {
		delegator().toBulkItems(null, CcpBulkEntityOperationType.create);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorToBulkItemsOperationNullTest() {
		delegator().toBulkItems(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorCopyDataToJsonNullTest() {
		delegator().copyDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorCopyDataToEntityNullTest() {
		delegator().copyDataTo(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorTransferDataToJsonNullTest() {
		delegator().transferDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorTransferDataToEntityNullTest() {
		delegator().transferDataTo(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorValidateJsonNullTest() {
		delegator().validateJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorGetIdToSearchDisposableRecordNullTest() {
		delegator().getIdToSearchDisposableRecord(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorGetRecordFromUnionAllUnionNullTest() {
		delegator().getRecordFromUnionAll(null, () -> JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void delegatorGetRecordFromUnionAllSupplierNullTest() {
		delegator().getRecordFromUnionAll(unionAll(), null);
	}

	// ── CcpDefaultEntityDelegator ─────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorConstrutorEntityNullTest() {
		new CcpDefaultEntityDelegator<Object>(null, bulkOperation(), keysToDelete()) {
		};
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorConstrutorBulkNullTest() {
		new CcpDefaultEntityDelegator<Object>(ENTITY, null, keysToDelete()) {
		};
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorConstrutorCacheFunctionNullTest() {
		new CcpDefaultEntityDelegator<Object>(ENTITY, bulkOperation(), null) {
		};
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorDeleteNullTest() {
		defaultDelegator().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorDeleteAnyWhereNullTest() {
		defaultDelegator().deleteAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorSaveNullTest() {
		defaultDelegator().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorCalculateIdNullTest() {
		defaultDelegator().calculateId(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorGetOneByIdNullTest() {
		defaultDelegator().getOneById(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorGetOneByIdAnyWhereNullTest() {
		defaultDelegator().getOneByIdAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorGetParametersToSearchNullTest() {
		defaultDelegator().getParametersToSearch(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorIsPresentInThisUnionAllUnionNullTest() {
		defaultDelegator().isPresentInThisUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorIsPresentInThisUnionAllJsonNullTest() {
		defaultDelegator().isPresentInThisUnionAll(unionAll(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorExistsNullTest() {
		defaultDelegator().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorToBulkItemsJsonNullTest() {
		defaultDelegator().toBulkItems(null, CcpBulkEntityOperationType.create);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorToBulkItemsOperationNullTest() {
		defaultDelegator().toBulkItems(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorTransferDataToJsonNullTest() {
		defaultDelegator().transferDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorTransferDataToEntitiesNullTest() {
		defaultDelegator().transferDataTo(JSON, (CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorCopyDataToJsonNullTest() {
		defaultDelegator().copyDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorCopyDataToEntitiesNullTest() {
		defaultDelegator().copyDataTo(JSON, (CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorValidateJsonNullTest() {
		defaultDelegator().validateJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorGetIdToSearchDisposableRecordNullTest() {
		defaultDelegator().getIdToSearchDisposableRecord(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorGetRecordFromUnionAllUnionNullTest() {
		defaultDelegator().getRecordFromUnionAll(null, () -> JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorGetRecordFromUnionAllSupplierNullTest() {
		defaultDelegator().getRecordFromUnionAll(unionAll(), null);
	}

	// ── CcpEntityFactory ──────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void factoryConstrutorNullTest() {
		new CcpEntityFactory(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetCustomEntityConfiguratorNullTest() {
		CcpEntityFactory.getCustomEntity((com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator) null,
				CcpEntityDecoratorTypes.Cacheable);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetCustomEntityConfiguratorDecoratorsNullTest() {
		CcpEntityFactory.getCustomEntity(new JnEntityJobsnowError(), (CcpEntityDecoratorTypes[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetCustomEntityEntityNullTest() {
		CcpEntityFactory.getCustomEntity((CcpEntity) null, CcpEntityDecoratorTypes.Cacheable);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetCustomEntityEntityDecoratorsNullTest() {
		CcpEntityFactory.getCustomEntity(ENTITY, (CcpEntityDecoratorTypes[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetEntityClassNullTest() {
		CcpEntityFactory.getEntity(null, CcpEntityFactory.mainEntityNameProducer);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetEntityNameExtractorNullTest() {
		CcpEntityFactory.getEntity(JnEntityJobsnowError.class, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetEntityDecoratorsNullTest() {
		CcpEntityFactory.getEntity(JnEntityJobsnowError.class, CcpEntityFactory.mainEntityNameProducer,
				(CcpEntityDecoratorTypes[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetFieldsNullTest() {
		CcpEntityFactory.getFields(null);
	}

	// ── CcpEntityMetaData ─────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void metaDataGetOperationCallbackNullTest() {
		metaData().getOperationCallback(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataGetOnlyUpdatableFieldsNullTest() {
		metaData().getOnlyUpdatableFields(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataGetSortedPrimaryKeyValuesNullTest() {
		metaData().getSortedPrimaryKeyValues(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataGetOnlyExistingFieldsNullTest() {
		metaData().getOnlyExistingFields(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataGetOneByIdOrHandleItJsonNullTest() {
		metaData().getOneByIdOrHandleItIfThisIdWasNotFound(null, json -> json);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataGetOneByIdOrHandleItBusinessNullTest() {
		metaData().getOneByIdOrHandleItIfThisIdWasNotFound(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataGetMultipleByIdsNullTest() {
		metaData().getMultipleByIds(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataGetPrimaryKeyValuesNullTest() {
		metaData().getPrimaryKeyValues(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataToCreateBulkItemNullTest() {
		metaData().toCreateBulkItem(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataToUpdateBulkItemNullTest() {
		metaData().toUpdateBulkItem(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void metaDataToDeleteBulkItemNullTest() {
		metaData().toDeleteBulkItem(null);
	}

	// ── DefaultImplementationEntity ───────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void defaultImplementationConstrutorNullTest() {
		new DefaultImplementationEntity(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultImplementationEqualsNullTest() {
		new DefaultImplementationEntity(metaData()).equals(null);
	}

	// ── CcpEntityDecoratorTypes ───────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void decoratorTypesGetEntityClassNullTest() {
		CcpEntityDecoratorTypes.Cacheable.getEntity(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void decoratorTypesGetEntityDecoratedNullTest() {
		CcpEntityDecoratorTypes.Cacheable.getEntity(JnEntityJobsnowError.class, null);
	}

	// ── DecoratorCacheEntity ──────────────────────────────────────────────────

	private static DecoratorCacheEntity cacheDecorator() {
		return new DecoratorCacheEntity(ENTITY, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorConstrutorEntityNullTest() {
		new DecoratorCacheEntity(null, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorConstrutorClassNullTest() {
		new DecoratorCacheEntity(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorDeleteNullTest() {
		cacheDecorator().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorDeleteAnyWhereNullTest() {
		cacheDecorator().deleteAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorExistsNullTest() {
		cacheDecorator().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorGetOneByIdNullTest() {
		cacheDecorator().getOneById(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorGetRecordFromUnionAllUnionNullTest() {
		cacheDecorator().getRecordFromUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorGetRecordFromUnionAllJsonNullTest() {
		cacheDecorator().getRecordFromUnionAll(unionAll(), (CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorIsPresentInThisUnionAllUnionNullTest() {
		cacheDecorator().isPresentInThisUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorIsPresentInThisUnionAllJsonNullTest() {
		cacheDecorator().isPresentInThisUnionAll(unionAll(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorSaveNullTest() {
		cacheDecorator().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorTransferDataToJsonNullTest() {
		cacheDecorator().transferDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorTransferDataToEntityNullTest() {
		cacheDecorator().transferDataTo(JSON, null);
	}

	// ── DecoratorFieldsTransformerEntity ──────────────────────────────────────

	private static DecoratorFieldsTransformerEntity transformerDecorator() {
		return new DecoratorFieldsTransformerEntity(ENTITY, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorConstrutorEntityNullTest() {
		new DecoratorFieldsTransformerEntity(null, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorConstrutorClassNullTest() {
		new DecoratorFieldsTransformerEntity(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorDeleteNullTest() {
		transformerDecorator().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorExistsNullTest() {
		transformerDecorator().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorGetHandledJsonNullTest() {
		transformerDecorator().getHandledJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorGetOneByIdNullTest() {
		transformerDecorator().getOneById(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorGetParametersToSearchNullTest() {
		transformerDecorator().getParametersToSearch(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorGetRecordFromUnionAllUnionNullTest() {
		transformerDecorator().getRecordFromUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorGetRecordFromUnionAllJsonNullTest() {
		transformerDecorator().getRecordFromUnionAll(unionAll(), (CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorIsPresentInThisUnionAllUnionNullTest() {
		transformerDecorator().isPresentInThisUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorIsPresentInThisUnionAllJsonNullTest() {
		transformerDecorator().isPresentInThisUnionAll(unionAll(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorSaveNullTest() {
		transformerDecorator().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorTransferDataToJsonNullTest() {
		transformerDecorator().transferDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorTransferDataToEntityNullTest() {
		transformerDecorator().transferDataTo(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorCopyDataToJsonNullTest() {
		transformerDecorator().copyDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorCopyDataToEntityNullTest() {
		transformerDecorator().copyDataTo(JSON, null);
	}

	/** {@code AlreadyTransformedJson.redoJson} só é alcançável pelo retorno de {@code getHandledJson}. */
	@Test(expected = CcpNullParameterException.class)
	public void alreadyTransformedJsonRedoJsonNullTest() {
		CcpJsonRepresentation alreadyTransformed = transformerDecorator().getHandledJson(JSON);
		alreadyTransformed.redoJson(null);
	}

	// ── DecoratorFieldsValidatorEntity ────────────────────────────────────────

	private static DecoratorFieldsValidatorEntity validatorDecorator() {
		return new DecoratorFieldsValidatorEntity(ENTITY, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorConstrutorEntityNullTest() {
		new DecoratorFieldsValidatorEntity(null, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorConstrutorClassNullTest() {
		new DecoratorFieldsValidatorEntity(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorValidateJsonNullTest() {
		validatorDecorator().validateJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorSaveNullTest() {
		validatorDecorator().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorTransferDataToJsonNullTest() {
		validatorDecorator().transferDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorTransferDataToEntityNullTest() {
		validatorDecorator().transferDataTo(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorCopyDataToJsonNullTest() {
		validatorDecorator().copyDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorCopyDataToEntityNullTest() {
		validatorDecorator().copyDataTo(JSON, null);
	}

	// ── DecoratorOperationsWriterEntity ───────────────────────────────────────

	private static DecoratorOperationsWriterEntity operationsDecorator() {
		return new DecoratorOperationsWriterEntity(ENTITY, JnEntityLoginTokenRequestResend.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationsDecoratorConstrutorEntityNullTest() {
		new DecoratorOperationsWriterEntity(null, JnEntityLoginTokenRequestResend.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationsDecoratorConstrutorClassNullTest() {
		new DecoratorOperationsWriterEntity(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationsDecoratorSaveNullTest() {
		operationsDecorator().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationsDecoratorDeleteNullTest() {
		operationsDecorator().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationsDecoratorDeleteAnyWhereNullTest() {
		operationsDecorator().deleteAnyWhere(null);
	}

	// ── DecoratorReadOnlyEntity ───────────────────────────────────────────────

	private static DecoratorReadOnlyEntity readOnlyDecorator() {
		return new DecoratorReadOnlyEntity(ENTITY, JnEntityDisposableRecord.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorConstrutorEntityNullTest() {
		new DecoratorReadOnlyEntity(null, JnEntityDisposableRecord.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorConstrutorClassNullTest() {
		new DecoratorReadOnlyEntity(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorDeleteNullTest() {
		readOnlyDecorator().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorDeleteAnyWhereNullTest() {
		readOnlyDecorator().deleteAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorSaveNullTest() {
		readOnlyDecorator().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorTransferDataToJsonNullTest() {
		readOnlyDecorator().transferDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorTransferDataToEntitiesNullTest() {
		readOnlyDecorator().transferDataTo(JSON, (CcpEntity[]) null);
	}

	// ── DecoratorTransferDataEntity ───────────────────────────────────────────

	private static DecoratorTransferDataEntity transferDecorator() {
		return new DecoratorTransferDataEntity(ENTITY, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDecoratorConstrutorEntityNullTest() {
		new DecoratorTransferDataEntity(null, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDecoratorConstrutorClassNullTest() {
		new DecoratorTransferDataEntity(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDecoratorCopyDataToJsonNullTest() {
		transferDecorator().copyDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDecoratorCopyDataToEntityNullTest() {
		transferDecorator().copyDataTo(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDecoratorTransferDataToJsonNullTest() {
		transferDecorator().transferDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDecoratorTransferDataToEntityNullTest() {
		transferDecorator().transferDataTo(JSON, null);
	}

	// ── DecoratorTwinEntity ───────────────────────────────────────────────────

	private static DecoratorTwinEntity twinDecorator() {
		return new DecoratorTwinEntity(ENTITY, JnEntityContactUs.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void twinDecoratorConstrutorEntityNullTest() {
		new DecoratorTwinEntity(null, JnEntityContactUs.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void twinDecoratorConstrutorClassNullTest() {
		new DecoratorTwinEntity(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void twinDecoratorDeleteNullTest() {
		twinDecorator().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void twinDecoratorGetOneByIdNullTest() {
		twinDecorator().getOneById(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void twinDecoratorSaveNullTest() {
		twinDecorator().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void twinDecoratorGetParametersToSearchNullTest() {
		twinDecorator().getParametersToSearch(null);
	}

	// ── CcpEntityOperationType (pacote irmão, exercitado a partir daqui) ──────

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeGetTopicHandlerEntityNullTest() {
		CcpEntityOperationType.save.getTopicHandler(null, bulkOperation(), keysToDelete());
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeGetTopicHandlerBulkNullTest() {
		CcpEntityOperationType.save.getTopicHandler(ENTITY, null, keysToDelete());
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeGetTopicHandlerCacheFunctionNullTest() {
		CcpEntityOperationType.save.getTopicHandler(ENTITY, bulkOperation(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeInstanciateFunctionNullTest() {
		CcpEntityOperationType.instanciateFunction(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeGetJsonValidationClassNullTest() {
		CcpEntityOperationType.save.getJsonValidationClass(null);
	}

	// ── auxiliares ────────────────────────────────────────────────────────────

	private static com.ccp.especifications.db.crud.CcpSelectUnionAll unionAll() {
		return new com.ccp.especifications.db.crud.CcpSelectUnionAll(new CcpJsonRepresentation[] { JSON },
				new ArrayList<>(Arrays.asList(JSON)), ENTITY);
	}
}
