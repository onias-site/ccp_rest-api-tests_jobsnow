package com.ccp.especifications.db.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

/**
 * Proves that a procedure that names no field to return is refused before any of its actions runs (finding 17): until
 * 2026-10-07 the check came after the actions, so their side effects (a write, a send, a delete) were already done when
 * the procedure failed.
 */
public class ProcedureWithoutFieldsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
	}

	@Test
	public void aProcedureWithoutFieldsIsRefusedBeforeItsActionsRun() {
		AtomicInteger sideEffects = new AtomicInteger();
		CcpBusiness actionWithSideEffect = new CcpBusiness() {
			public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
				sideEffects.incrementAndGet();
				return json;
			}
		};
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("anything"), "value");

		try {
			new CcpGetEntityId(request)
				.toBeginProcedureAnd()
					.executeAction(actionWithSideEffect)
					.andFinallyReturningTheseFields()
				.endThisProcedureRetrievingTheResultingData(new CcpFieldName("procedureWithoutFields"), CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, keys -> {});
			fail("a procedure without fields to return must be refused");
		} catch (CcpErrorFlowFieldsToReturnNotMentioned expected) {
			assertEquals("the action must not run", 0, sideEffects.get());
		}
	}
}
