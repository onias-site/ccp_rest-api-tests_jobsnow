package com.jn.db.bulk;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnExecuteBulkOperationTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnExecuteBulkOperation.INSTANCE);
	}

	// ── executeBulk(Collection, Consumer) — null-parameter tests ─────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeBulkCollectionItemsNullTest() {
		JnExecuteBulkOperation.INSTANCE.executeBulk(null, JnDeleteKeysFromCache.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeBulkCollectionFunctionNullTest() {
		JnExecuteBulkOperation.INSTANCE.executeBulk(Collections.emptyList(), null);
	}

	// ── executeBulk(Json, Operation, Consumer, Entity...) — null-parameter ───

	@Test(expected = CcpNullParameterException.class)
	public void executeBulkJsonJsonNullTest() {
		JnExecuteBulkOperation.INSTANCE.executeBulk(null, CcpBulkEntityOperationType.create,
				JnDeleteKeysFromCache.INSTANCE, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeBulkJsonOperationNullTest() {
		JnExecuteBulkOperation.INSTANCE.executeBulk(CcpOtherConstants.EMPTY_JSON, null,
				JnDeleteKeysFromCache.INSTANCE, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeBulkJsonFunctionNullTest() {
		JnExecuteBulkOperation.INSTANCE.executeBulk(CcpOtherConstants.EMPTY_JSON,
				CcpBulkEntityOperationType.create, null, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeBulkJsonEntitiesNullTest() {
		JnExecuteBulkOperation.INSTANCE.executeBulk(CcpOtherConstants.EMPTY_JSON,
				CcpBulkEntityOperationType.create, JnDeleteKeysFromCache.INSTANCE,
				(com.ccp.especifications.db.utils.entity.CcpEntity[]) null);
	}
}
