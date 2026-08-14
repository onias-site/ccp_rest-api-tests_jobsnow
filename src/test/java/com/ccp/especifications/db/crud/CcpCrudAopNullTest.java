package com.ccp.especifications.db.crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerCreate;
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpErrorCrudMultiGetSearchFailed;
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpErrorCrudMultiGetSearchUnfeasible;
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpErrorFlowFieldsToReturnNotMentioned;
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpSelectFinally;
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpSelectFoundInEntity;
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpSelectLoadDataFromEntity;
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpSelectNextStep;
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpSelectProcedure;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Cobertura dos aspectos {@code CcpNullParameterAspect} / {@code CcpNullReturnAspect} sobre o
 * pacote de CRUD: {@code CcpCrud}, {@code CcpSelectUnionAll}, o fluent chain de
 * {@code CcpGetEntityId} e as funções {@code FunctionPutEntity} / {@code FunctionPutStatus}.
 */
public class CcpCrudAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				new CcpElasticSearchDbRequest(),
				new CcpElasticSearchCrud(),
				CcpLocalCacheInstances.mock);
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static CcpCrud crud() {
		return CcpDependencyInjection.getDependency(CcpCrud.class);
	}

	private static CcpSelectUnionAll unionAll() {
		return new CcpSelectUnionAll(new CcpJsonRepresentation[] { JSON },
				new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)), ENTITY);
	}

	private static CcpSelectProcedure procedure() {
		return new CcpGetEntityId(JSON).toBeginProcedureAnd();
	}

	private static CcpSelectLoadDataFromEntity loadData() {
		return procedure().loadThisIdFromEntity(ENTITY);
	}

	private static CcpSelectFoundInEntity foundInEntity() {
		return procedure().ifThisIdIsPresentInEntity(ENTITY);
	}

	private static CcpSelectNextStep nextStep() {
		return foundInEntity().returnStatus(CcpProcessStatusDefault.OK);
	}

	private static CcpSelectFinally selectFinally() {
		return loadData().andFinally(new CcpJsonFieldName[0]);
	}

	// ── CcpCrud (métodos default) ─────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void unionAllArrayJsonsNullTest() {
		crud().unionAll((CcpJsonRepresentation[]) null, keys -> {
		}, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unionAllArrayCacheFunctionNullTest() {
		crud().unionAll(new CcpJsonRepresentation[] { JSON }, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unionAllArrayEntitiesNullTest() {
		crud().unionAll(new CcpJsonRepresentation[] { JSON }, keys -> {
		}, (CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unionAllJsonNullTest() {
		crud().unionAll((CcpJsonRepresentation) null, keys -> {
		}, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unionAllJsonCacheFunctionNullTest() {
		crud().unionAll(JSON, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unionAllJsonEntitiesNullTest() {
		crud().unionAll(JSON, keys -> {
		}, (CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteKeysInCacheJsonsNullTest() {
		crud().deleteKeysInCache((CcpJsonRepresentation[]) null, keys -> {
		}, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteKeysInCacheFunctionNullTest() {
		crud().deleteKeysInCache(new CcpJsonRepresentation[] { JSON }, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteKeysInCacheEntitiesNullTest() {
		crud().deleteKeysInCache(new CcpJsonRepresentation[] { JSON }, keys -> {
		}, (CcpEntity[]) null);
	}

	// ── CcpSelectUnionAll ─────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllConstrutorSearchParametersNullTest() {
		new CcpSelectUnionAll(null, new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)), ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllConstrutorResultsNullTest() {
		new CcpSelectUnionAll(new CcpJsonRepresentation[] { JSON }, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllConstrutorEntitiesNullTest() {
		new CcpSelectUnionAll(new CcpJsonRepresentation[] { JSON },
				new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)), (CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllIsPresentEntityNameNullTest() {
		unionAll().isPresent(null, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllIsPresentIdNullTest() {
		unionAll().isPresent("entity", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllHandleRecordSearchParameterNullTest() {
		unionAll().handleRecordInUnionAll(null, new CcpBulkHandlerCreate(ENTITY));
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllHandleRecordHandlerNullTest() {
		unionAll().handleRecordInUnionAll(JSON, (CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllGetEntityRowIndexNullTest() {
		unionAll().getEntityRow(null, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllGetEntityRowIdNullTest() {
		unionAll().getEntityRow("entity", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectUnionAllGetEntityRowsNullTest() {
		unionAll().getEntityRows(null);
	}

	// ── CcpGetEntityId ────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getEntityIdConstrutorNullTest() {
		new CcpGetEntityId((CcpJsonRepresentation[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void procedureLoadThisIdFromEntityNullTest() {
		procedure().loadThisIdFromEntity(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void procedureIfThisIdIsPresentInEntityNullTest() {
		procedure().ifThisIdIsPresentInEntity(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void procedureIfThisIdIsNotPresentInEntityNullTest() {
		procedure().ifThisIdIsNotPresentInEntity(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void procedureExecuteActionNullTest() {
		procedure().executeAction(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void loadDataAndFinallyNullTest() {
		loadData().andFinally((CcpJsonFieldName[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void foundInEntityExecuteActionNullTest() {
		foundInEntity().executeAction(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void foundInEntityReturnStatusNullTest() {
		foundInEntity().returnStatus(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void nextStepAndFinallyReturningTheseFieldsCollectionNullTest() {
		nextStep().andFinallyReturningTheseFields((Collection<CcpJsonFieldName>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void nextStepAndFinallyReturningTheseFieldsArrayNullTest() {
		nextStep().andFinallyReturningTheseFields((CcpJsonFieldName[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFinallyEndThisProcedureContextNullTest() {
		selectFinally().endThisProcedure(null, json -> json, json -> json, keys -> {
		});
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFinallyEndThisProcedureWhenErrorNullTest() {
		selectFinally().endThisProcedure(CcpSelectFoundInEntity.JsonFieldNames.statements, null, json -> json, keys -> {
		});
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFinallyEndThisProcedureWhenSuccessNullTest() {
		selectFinally().endThisProcedure(CcpSelectFoundInEntity.JsonFieldNames.statements, json -> json, null, keys -> {
		});
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFinallyEndThisProcedureCacheFunctionNullTest() {
		selectFinally().endThisProcedure(CcpSelectFoundInEntity.JsonFieldNames.statements, json -> json, json -> json,
				null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFinallyEndRetrievingDataContextNullTest() {
		selectFinally().endThisProcedureRetrievingTheResultingData(null, json -> json, json -> json, keys -> {
		});
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFinallyEndRetrievingDataWhenErrorNullTest() {
		selectFinally().endThisProcedureRetrievingTheResultingData(CcpSelectFoundInEntity.JsonFieldNames.statements,
				null, json -> json, keys -> {
				});
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFinallyEndRetrievingDataWhenSuccessNullTest() {
		selectFinally().endThisProcedureRetrievingTheResultingData(CcpSelectFoundInEntity.JsonFieldNames.statements,
				json -> json, null, keys -> {
				});
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFinallyEndRetrievingDataCacheFunctionNullTest() {
		selectFinally().endThisProcedureRetrievingTheResultingData(CcpSelectFoundInEntity.JsonFieldNames.statements,
				json -> json, json -> json, null);
	}

	// ── exceções de CcpGetEntityId ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void errorMultiGetSearchFailedNullTest() {
		new CcpErrorCrudMultiGetSearchFailed(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void errorMultiGetSearchUnfeasibleJsonsNullTest() {
		new CcpErrorCrudMultiGetSearchUnfeasible(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void errorMultiGetSearchUnfeasibleEntitiesNullTest() {
		new CcpErrorCrudMultiGetSearchUnfeasible(new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)),
				(CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void errorFlowFieldsToReturnNotMentionedNullTest() {
		new CcpErrorFlowFieldsToReturnNotMentioned(null);
	}

	// ── FunctionPutEntity / FunctionPutStatus ─────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void functionPutEntityApplyNullTest() {
		FunctionPutEntity.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void functionPutStatusApplyNullTest() {
		FunctionPutStatus.INSTANCE.execute(null);
	}
}
