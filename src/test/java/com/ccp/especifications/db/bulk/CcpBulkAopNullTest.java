package com.ccp.especifications.db.bulk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerCreate;
import com.ccp.especifications.db.crud.CcpHandleWithSearchResultsInTheEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityJobsnowError;
import org.junit.Test;

/**
 * Cobertura dos aspectos {@code CcpNullParameterAspect} / {@code CcpNullReturnAspect} sobre o
 * pacote de operações bulk: {@code CcpBulkItem}, {@code CcpBulkExecutor},
 * {@code CcpExecuteBulkOperation}, {@code CcpBulkOperationResult} e
 * {@code CcpBulkEntityOperationType}.
 */
public class CcpBulkAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				new CcpElasticSearchDbRequest(),
				new CcpElasticSearchCrud(),
				new CcpElasticSerchDbBulk(),
				CcpLocalCacheInstances.mock);
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	static CcpBulkItem bulkItem() {
		return new CcpBulkItem(JSON, CcpBulkEntityOperationType.create, ENTITY, "id");
	}

	private static CcpBulkExecutor bulkExecutor() {
		return CcpDependencyInjection.getDependency(CcpBulkExecutor.class);
	}

	private static CcpExecuteBulkOperation executeBulkOperation() {
		return com.jn.db.bulk.JnExecuteBulkOperation.INSTANCE;
	}

	/** Implementação mínima para alcançar os métodos default de {@code CcpBulkOperationResult}. */


	// ── CcpBulkItem ───────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemConstrutorCopiaOtherNullTest() {
		new CcpBulkItem(null, CcpBulkEntityOperationType.update);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemConstrutorCopiaOperationNullTest() {
		new CcpBulkItem(bulkItem(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemConstrutorJsonNullTest() {
		new CcpBulkItem(null, CcpBulkEntityOperationType.create, ENTITY, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemConstrutorOperationNullTest() {
		new CcpBulkItem(JSON, null, ENTITY, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemConstrutorEntityNullTest() {
		new CcpBulkItem(JSON, CcpBulkEntityOperationType.create, null, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemConstrutorIdNullTest() {
		new CcpBulkItem(JSON, CcpBulkEntityOperationType.create, ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemEqualsNullTest() {
		bulkItem().equals(null);
	}

	// ── CcpBulkEntityOperationType ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeGetReprocessProducerNullTest() {
		CcpBulkEntityOperationType.create.getReprocess(null, new BulkOperationResultForTest(), ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeGetReprocessResultNullTest() {
		CcpBulkEntityOperationType.create.getReprocess(result -> JSON, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operationTypeGetReprocessEntityNullTest() {
		CcpBulkEntityOperationType.create.getReprocess(result -> JSON, new BulkOperationResultForTest(), null);
	}

	// ── CcpBulkOperationResult ────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void bulkOperationResultGetReprocessMapperNullTest() {
		new BulkOperationResultForTest().getReprocess(null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkOperationResultGetReprocessEntityNullTest() {
		new BulkOperationResultForTest().getReprocess(result -> JSON, null);
	}

	// ── CcpBulkExecutor ───────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorAddRecordJsonNullTest() {
		bulkExecutor().addRecord(null, CcpBulkEntityOperationType.create, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorAddRecordOperationNullTest() {
		bulkExecutor().addRecord(JSON, null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorAddRecordEntityNullTest() {
		bulkExecutor().addRecord(JSON, CcpBulkEntityOperationType.create, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorAddRecordsItemsNullTest() {
		bulkExecutor().addRecords((List<CcpBulkItem>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorAddRecordsRecordsNullTest() {
		bulkExecutor().addRecords((List<CcpJsonRepresentation>) null, CcpBulkEntityOperationType.create, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorAddRecordsOperationNullTest() {
		bulkExecutor().addRecords(new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)), null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorAddRecordsEntityNullTest() {
		bulkExecutor().addRecords(new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)),
				CcpBulkEntityOperationType.create, null);
	}

	// ── CcpExecuteBulkOperation ───────────────────────────────────────────────

	@SuppressWarnings("unchecked")
	@Test(expected = CcpNullParameterException.class)
	public void executeSelectUnionAllJsonNullTest() {
		executeBulkOperation().executeSelectUnionAllThenExecuteBulkOperation(null, keys -> {
		}, new CcpBulkHandlerCreate(ENTITY));
	}

	@SuppressWarnings("unchecked")
	@Test(expected = CcpNullParameterException.class)
	public void executeSelectUnionAllCacheFunctionNullTest() {
		executeBulkOperation().executeSelectUnionAllThenExecuteBulkOperation(JSON, null,
				new CcpBulkHandlerCreate(ENTITY));
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeSelectUnionAllHandlersNullTest() {
		executeBulkOperation().executeSelectUnionAllThenExecuteBulkOperation(JSON, keys -> {
		}, (CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>[]) null);
	}
}
