package com.ccp.implementations.db.utils.elasticsearch;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;

public class CcpElasticSearchDbRequestTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpElasticSearchDbRequest());
	}

	private static CcpDbRequester getDb() {
		return CcpDependencyInjection.getDependency(CcpDbRequester.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpElasticSearchDbRequest());
	}

	@Test
	public void getInstanceTest() {
		CcpDbRequester instance = new CcpElasticSearchDbRequest().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests — executeHttpRequest (variantes) ─────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestTraceNullTest() {
		getDb().executeHttpRequest(null, "/x", CcpHttpMethods.GET, 200, CcpOtherConstants.EMPTY_JSON,
				CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestUrlNullTest() {
		getDb().executeHttpRequest("t", null, CcpHttpMethods.GET, 200, CcpOtherConstants.EMPTY_JSON,
				CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestMethodNullTest() {
		getDb().executeHttpRequest("t", "/x", null, 200, CcpOtherConstants.EMPTY_JSON,
				CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestBodyNullTest() {
		getDb().executeHttpRequest("t", "/x", CcpHttpMethods.GET, 200, null,
				CcpHttpResponseType.singleRecord);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeHttpRequestTransformerNullTest() {
		getDb().executeHttpRequest("t", "/x", CcpHttpMethods.GET, 200, CcpOtherConstants.EMPTY_JSON, null);
	}

	// ── null-parameter tests — createTables ───────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void createTablesScriptNullTest() {
		getDb().createTables(null, "java", "err.json", "insert.json");
	}

	@Test(expected = CcpNullParameterException.class)
	public void createTablesJavaClassesNullTest() {
		getDb().createTables("script", null, "err.json", "insert.json");
	}

	@Test(expected = CcpNullParameterException.class)
	public void createTablesMappingErrorsNullTest() {
		getDb().createTables("script", "java", null, "insert.json");
	}

	@Test(expected = CcpNullParameterException.class)
	public void createTablesInsertErrorsNullTest() {
		getDb().createTables("script", "java", "err.json", null);
	}

	// ── null-parameter tests — executeDatabaseSetup ───────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeDatabaseSetupJavaClassesNullTest() {
		getDb().executeDatabaseSetup(null, "java", "script", e -> {}, e -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeDatabaseSetupHostFolderNullTest() {
		getDb().executeDatabaseSetup("java-path", null, "script", e -> {}, e -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeDatabaseSetupScriptNullTest() {
		getDb().executeDatabaseSetup("java-path", "java", null, e -> {}, e -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeDatabaseSetupWhenIncorrectMappingNullTest() {
		getDb().executeDatabaseSetup("java-path", "java", "script", null, e -> {});
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeDatabaseSetupWhenErrorNullTest() {
		getDb().executeDatabaseSetup("java-path", "java", "script", e -> {}, null);
	}
}
