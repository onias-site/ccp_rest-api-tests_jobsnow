package com.ccp.implementations.db.query.elasticsearch;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

public class CcpElasticSearchQueryExecutorTest {

	public CcpElasticSearchQueryExecutorTest() {
		CcpDependencyInjection.loadAllDependencies(
				new CcpElasticSearchQueryExecutor(),
				CcpLocalInstances.mensageriaSender,
				new CcpElasticSearchDbRequest(), 
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(), 
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(), 
				new CcpApacheMimeHttp() 
				);
	}

	private CcpQueryExecutor getExecutor() {
		return CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
	}

	private static CcpQueryOptions options() {
		return CcpQueryOptions.INSTANCE;
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpElasticSearchQueryExecutor());
	}

	@Test
	public void getInstanceTest() {
		CcpQueryExecutor instance = new CcpElasticSearchQueryExecutor().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getTermsStatisQueryNullTest() {
		getExecutor().getTermsStatis(null, new String[] { "r" }, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getTermsStatisResourcesNullTest() {
		getExecutor().getTermsStatis(options(), null, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getTermsStatisFieldNullTest() {
		getExecutor().getTermsStatis(options(), new String[] { "r" }, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteQueryNullTest() {
		getExecutor().delete(null, "r");
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteResourcesNullTest() {
		getExecutor().delete(options(), (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void updateQueryNullTest() {
		getExecutor().update(null, new String[] { "r" }, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void updateResourcesNullTest() {
		getExecutor().update(options(), null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void updateNewValuesNullTest() {
		getExecutor().update(options(), new String[] { "r" }, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultLongQueryNullTest() {
		getExecutor().consumeQueryResult(null, new String[] { "r" }, "1m", 10L, list -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultLongResourcesNullTest() {
		getExecutor().consumeQueryResult(options(), null, "1m", 10L, list -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultLongScrollTimeNullTest() {
		getExecutor().consumeQueryResult(options(), new String[] { "r" }, null, 10L, list -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultLongSizeNullTest() {
		getExecutor().consumeQueryResult(options(), new String[] { "r" }, "1m", (Long) null, list -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultLongConsumerNullTest() {
		getExecutor().consumeQueryResult(options(), new String[] { "r" }, "1m", 10L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void totalQueryNullTest() {
		getExecutor().total(null, new String[] { "r" });
	}

	@Test(expected = CcpNullParameterException.class)
	public void totalResourcesNullTest() {
		getExecutor().total(options(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsListQueryNullTest() {
		getExecutor().getResultAsList(null, new String[] { "r" });
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsListResourcesNullTest() {
		getExecutor().getResultAsList(options(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsMapQueryNullTest() {
		getExecutor().getResultAsMap(null, new String[] { "r" }, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsMapResourcesNullTest() {
		getExecutor().getResultAsMap(options(), null, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsMapFieldNullTest() {
		getExecutor().getResultAsMap(options(), new String[] { "r" }, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsPackageUrlNullTest() {
		getExecutor().getResultAsPackage(null, CcpHttpMethods.POST, 200, options(), new String[] { "r" });
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsPackageMethodNullTest() {
		getExecutor().getResultAsPackage("/_search", null, 200, options(), new String[] { "r" });
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsPackageQueryNullTest() {
		getExecutor().getResultAsPackage("/_search", CcpHttpMethods.POST, 200, null, new String[] { "r" });
	}

	@Test(expected = CcpNullParameterException.class)
	public void getResultAsPackageResourcesNullTest() {
		getExecutor().getResultAsPackage("/_search", CcpHttpMethods.POST, 200, options(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMapQueryNullTest() {
		getExecutor().getMap(null, new String[] { "r" }, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMapResourcesNullTest() {
		getExecutor().getMap(options(), null, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getMapFieldNullTest() {
		getExecutor().getMap(options(), new String[] { "r" }, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getAggregationsQueryNullTest() {
		getExecutor().getAggregations(null, "r");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getAggregationsResourcesNullTest() {
		getExecutor().getAggregations(options(), (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultIntegerQueryNullTest() {
		getExecutor().consumeQueryResult(null, new String[] { "r" }, "1m", 10, item -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultIntegerResourcesNullTest() {
		getExecutor().consumeQueryResult(options(), null, "1m", 10, item -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultIntegerScrollTimeNullTest() {
		getExecutor().consumeQueryResult(options(), new String[] { "r" }, null, 10, item -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultIntegerSizeNullTest() {
		getExecutor().consumeQueryResult(options(), new String[] { "r" }, "1m", (Integer) null, item -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void consumeQueryResultIntegerConsumerNullTest() {
		getExecutor().consumeQueryResult(options(), new String[] { "r" }, "1m", 10, null);
	}
}
