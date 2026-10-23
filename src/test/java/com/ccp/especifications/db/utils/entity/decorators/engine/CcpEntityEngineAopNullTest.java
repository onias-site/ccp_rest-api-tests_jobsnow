package com.ccp.especifications.db.utils.entity.decorators.engine;

import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpBeforeSaveFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.transfers.CcpAfterCopyDataFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntitySideEffectBusinesses;
import java.util.function.Supplier;
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
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.js.entities.JsEntityLoginSessionValidation;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityJobsnowError;
import com.js.entities.JsEntityLoginTokenRequestResend;
import org.junit.Test;

/**
 * Coverage of the {@code CcpNullParameterAspect} / {@code CcpNullReturnAspect} aspects over the
 * entity engine: {@code CcpEntityDelegator}, {@code CcpDefaultEntityDelegator}, {@code CcpEntityFactory},
 * {@code CcpEntityMetaData}, {@code DefaultImplementationEntity} and every decorator of the package.
 */
public class CcpEntityEngineAopNullTest {

	/**
	 * {@code CcpDbRequester} is needed because of the {@code CcpSelectUnionAll} test double: its
	 * constructor asks for this dependency to find out the entity and id field names. Without it,
	 * building the scenario blows up before the method under test is called, and the aspect never
	 * gets to run. Nothing here touches the database — both methods used return a field name.
	 */
	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpElasticSearchDbRequest.INSTANCE);
	}

	static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	/** Minimal concrete subclass to reach the methods of {@code CcpDefaultEntityDelegator}. */


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
	public void delegatorConstructorNullTest() {
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
	public void delegatorGetJsonWithHandledPrimaryKeyNullTest() {
		delegator().getJsonWithHandledPrimaryKey(null);
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
	public void defaultDelegatorConstructorEntityNullTest() {
		new CcpDefaultEntityDelegator<Object>(null, bulkOperation(), keysToDelete()) {
		};
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorConstructorBulkNullTest() {
		new CcpDefaultEntityDelegator<Object>(ENTITY, null, keysToDelete()) {
		};
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorConstructorCacheFunctionNullTest() {
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
		defaultDelegator().transferDataTo(JSON, (CcpEntity) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorCopyDataToJsonNullTest() {
		defaultDelegator().copyDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void defaultDelegatorCopyDataToEntitiesNullTest() {
		defaultDelegator().copyDataTo(JSON, (CcpEntity) null);
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
	public void factoryConstructorNullTest() {
		new CcpEntityFactory(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetCustomEntityConfiguratorNullTest() {
		CcpEntityFactory.getCustomEntity((com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator) null,
				CcpEntityDecoratorTypes.Cacheable);
	}

	@Test(expected = CcpNullParameterException.class)
	public void factoryGetCustomEntityConfiguratorDecoratorsNullTest() {
		CcpEntityFactory.getCustomEntity(JnEntityJobsnowError.INSTANCE, (CcpEntityDecoratorTypes[]) null);
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
	public void defaultImplementationConstructorNullTest() {
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
	public void cacheDecoratorConstructorEntityNullTest() {
		new DecoratorCacheEntity(null, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorConstructorClassNullTest() {
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
		cacheDecorator().getRecordFromUnionAll(null, JSON.getJsonSupplier());
	}

	@Test(expected = CcpNullParameterException.class)
	public void cacheDecoratorGetRecordFromUnionAllJsonNullTest() {
		cacheDecorator().getRecordFromUnionAll(unionAll(), (Supplier<CcpJsonRepresentation>) null);
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
	public void transformerDecoratorConstructorEntityNullTest() {
		new DecoratorFieldsTransformerEntity(null, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transformerDecoratorConstructorClassNullTest() {
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
	public void transformerDecoratorGetJsonWithHandledPrimaryKeyNullTest() {
		transformerDecorator().getJsonWithHandledPrimaryKey(null);
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

	/** {@code AlreadyTransformedJson.redoJson} is only reachable through the return of {@code getHandledJson}. */
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
	public void validatorDecoratorConstructorEntityNullTest() {
		new DecoratorFieldsValidatorEntity(null, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorDecoratorConstructorClassNullTest() {
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

	// ── DecoratorWriteSideEffectEntity ──────────────────────────────────────

	private static DecoratorWriteSideEffectEntity writeSideEffectDecorator(CcpEntityOperationPhase phase) {
		return new DecoratorWriteSideEffectEntity(ENTITY, JsEntityLoginTokenRequestResend.class, CcpBeforeSaveFromMainEntity.class, phase,
				CcpEntityDecoratorOperationType.save, CcpEntityPhase.mainEntity, CcpEntitySideEffectBusinesses.operation);
	}

	@Test(expected = CcpNullParameterException.class)
	public void writeSideEffectDecoratorConstructorEntityNullTest() {
		new DecoratorWriteSideEffectEntity(null, JsEntityLoginTokenRequestResend.class, CcpBeforeSaveFromMainEntity.class, CcpEntityOperationPhase._before,
				CcpEntityDecoratorOperationType.save, CcpEntityPhase.mainEntity, CcpEntitySideEffectBusinesses.operation);
	}

	@Test(expected = CcpNullParameterException.class)
	public void writeSideEffectDecoratorConstructorClassNullTest() {
		new DecoratorWriteSideEffectEntity(ENTITY, null, CcpBeforeSaveFromMainEntity.class, CcpEntityOperationPhase._before,
				CcpEntityDecoratorOperationType.save, CcpEntityPhase.mainEntity, CcpEntitySideEffectBusinesses.operation);
	}

	@Test(expected = CcpNullParameterException.class)
	public void writeSideEffectDecoratorConstructorSideEffectNullTest() {
		new DecoratorWriteSideEffectEntity(ENTITY, JsEntityLoginTokenRequestResend.class, CcpBeforeSaveFromMainEntity.class, CcpEntityOperationPhase._before,
				CcpEntityDecoratorOperationType.save, CcpEntityPhase.mainEntity, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void beforeWriteSideEffectDecoratorSaveNullTest() {
		writeSideEffectDecorator(CcpEntityOperationPhase._before).save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void beforeWriteSideEffectDecoratorDeleteNullTest() {
		writeSideEffectDecorator(CcpEntityOperationPhase._before).delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void beforeWriteSideEffectDecoratorDeleteAnyWhereNullTest() {
		writeSideEffectDecorator(CcpEntityOperationPhase._before).deleteAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void afterWriteSideEffectDecoratorSaveNullTest() {
		writeSideEffectDecorator(CcpEntityOperationPhase._after).save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void afterWriteSideEffectDecoratorDeleteNullTest() {
		writeSideEffectDecorator(CcpEntityOperationPhase._after).delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void afterWriteSideEffectDecoratorDeleteAnyWhereNullTest() {
		writeSideEffectDecorator(CcpEntityOperationPhase._after).deleteAnyWhere(null);
	}

	// ── DecoratorReadOnlyEntity ───────────────────────────────────────────────

	private static DecoratorReadOnlyEntity readOnlyDecorator() {
		return new DecoratorReadOnlyEntity(ENTITY, JnEntityDisposableRecord.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorConstructorEntityNullTest() {
		new DecoratorReadOnlyEntity(null, JnEntityDisposableRecord.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readOnlyDecoratorConstructorClassNullTest() {
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
		readOnlyDecorator().transferDataTo(JSON, (CcpEntity) null);
	}

	// ── DecoratorTransferSideEffectEntity ───────────────────────────────────

	private static DecoratorTransferSideEffectEntity transferSideEffectDecorator(CcpEntityOperationPhase phase) {
		return new DecoratorTransferSideEffectEntity(ENTITY, JnEntityJobsnowError.class, CcpAfterCopyDataFromMainEntity.class, phase,
				CcpEntityDecoratorTransferType.copyDataTo, CcpEntityPhase.mainEntity, CcpEntitySideEffectBusinesses.transfer);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferSideEffectDecoratorConstructorEntityNullTest() {
		new DecoratorTransferSideEffectEntity(null, JnEntityJobsnowError.class, CcpAfterCopyDataFromMainEntity.class, CcpEntityOperationPhase._after,
				CcpEntityDecoratorTransferType.copyDataTo, CcpEntityPhase.mainEntity, CcpEntitySideEffectBusinesses.transfer);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferSideEffectDecoratorConstructorClassNullTest() {
		new DecoratorTransferSideEffectEntity(ENTITY, null, CcpAfterCopyDataFromMainEntity.class, CcpEntityOperationPhase._after,
				CcpEntityDecoratorTransferType.copyDataTo, CcpEntityPhase.mainEntity, CcpEntitySideEffectBusinesses.transfer);
	}

	@Test(expected = CcpNullParameterException.class)
	public void beforeTransferSideEffectDecoratorCopyDataToJsonNullTest() {
		transferSideEffectDecorator(CcpEntityOperationPhase._before).copyDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void beforeTransferSideEffectDecoratorCopyDataToEntityNullTest() {
		transferSideEffectDecorator(CcpEntityOperationPhase._before).copyDataTo(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void afterTransferSideEffectDecoratorTransferDataToJsonNullTest() {
		transferSideEffectDecorator(CcpEntityOperationPhase._after).transferDataTo(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void afterTransferSideEffectDecoratorTransferDataToEntityNullTest() {
		transferSideEffectDecorator(CcpEntityOperationPhase._after).transferDataTo(JSON, null);
	}

	// ── DecoratorTwinEntity ───────────────────────────────────────────────────

	private static DecoratorTwinEntity twinDecorator() {
		return new DecoratorTwinEntity(ENTITY, JsEntityLoginSessionValidation.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void twinDecoratorConstructorEntityNullTest() {
		new DecoratorTwinEntity(null, JsEntityLoginSessionValidation.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void twinDecoratorConstructorClassNullTest() {
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

	// ── CcpEntityOperationType (sibling package, exercised from here) ─────────

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeGetOperationCallbackNullTest() {
		CcpEntityOperationType.save.getOperationCallback(null);
	}

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
