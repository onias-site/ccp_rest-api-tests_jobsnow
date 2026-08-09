package com.ccp.implementations.db.crud.elasticsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre as classes internas do módulo
 * {@code ccp_db-crud_elasticsearch}.
 */
public class ElasticSearchCrudInternalsAopNullTest {

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

	private static ElasticSearchCrud crud() {
		CcpCrud dependency = CcpDependencyInjection.getDependency(CcpCrud.class);
		return (ElasticSearchCrud) dependency;
	}

	// ── ElasticSearchCrud ─────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getRequestBodyToMultipleGetIdsNullTest() {
		crud().getRequestBodyToMultipleGet((Set<String>) null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRequestBodyToMultipleGetEntitiesNullTest() {
		crud().getRequestBodyToMultipleGet(new HashSet<String>(Arrays.asList("id")), (CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unionAllValuesNullTest() {
		crud().unionAll((Collection<CcpJsonRepresentation>) null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unionAllEntitiesNullTest() {
		crud().unionAll(new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)), (CcpEntity[]) null);
	}

	// ── ElasticSearchHttpStatus ───────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void elasticSearchHttpStatusApplyNullTest() {
		ElasticSearchHttpStatus.OK.apply(null);
	}

	// ── FunctionResponseHandlerToMget ─────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void functionResponseHandlerToMgetApplyNullTest() {
		FunctionResponseHandlerToMget.INSTANCE.apply(null);
	}
}
