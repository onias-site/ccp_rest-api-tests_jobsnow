package com.ccp.implementations.db.bulk.elasticsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre as classes internas do módulo
 * {@code ccp_db-bulk_elasticsearch}.
 */
public class ElasticSearchBulkAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				new CcpElasticSearchDbRequest(),
				new CcpElasticSerchDbBulk(),
				CcpLocalCacheInstances.mock);
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static CcpBulkItem ccpBulkItem() {
		return new CcpBulkItem(JSON, CcpBulkEntityOperationType.create, ENTITY, "id");
	}

	// ── BulkItem ──────────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemConstrutorNullTest() {
		new BulkItem(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkItemEqualsNullTest() {
		new BulkItem(ccpBulkItem()).equals(null);
	}

	// ── BulkOperation ─────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void bulkOperationGetContentNullTest() {
		BulkOperation.create.getContent(null);
	}

	// ── ElasticSearchBulkOperationResult ──────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void bulkOperationResultConstrutorItemNullTest() {
		new ElasticSearchBulkOperationResult(null, new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)));
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkOperationResultConstrutorResultNullTest() {
		new ElasticSearchBulkOperationResult(ccpBulkItem(), null);
	}

	// ── ElasticSerchDbBulkExecutor ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorConstrutorNullTest() {
		new ElasticSerchDbBulkExecutor((List<CcpBulkItem>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void bulkExecutorAddRecordNullTest() {
		new ElasticSerchDbBulkExecutor(new ArrayList<CcpBulkItem>()).addRecord((CcpBulkItem) null);
	}
}
