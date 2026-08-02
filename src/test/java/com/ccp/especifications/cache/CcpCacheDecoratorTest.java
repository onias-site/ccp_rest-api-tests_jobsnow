package com.ccp.especifications.cache;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

public class CcpCacheDecoratorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.mock);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	// Nota: ctor(CcpBulkItem) e ctor(CcpEntity, String) delegam via this(...) — a
	// avaliação de argumentos ocorre antes do aspecto interceptar (item.entity, item.id),
	// então NullPointerException nativa vence CcpNullParameterException.

	@Test(expected = CcpNullParameterException.class)
	public void ctorStringNullTest() {
		new CcpCacheDecorator((String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getFunctionNullTest() {
		new CcpCacheDecorator("k").get((java.util.function.Function<com.ccp.decorators.CcpJsonRepresentation, Object>) null, 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getBusinessNullBusinessTest() {
		new CcpCacheDecorator("k").get((com.ccp.business.CcpBusiness) null, CcpOtherConstants.EMPTY_JSON, 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getBusinessNullJsonTest() {
		new CcpCacheDecorator("k").get(j -> j, null, 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOrDefaultNullTest() {
		new CcpCacheDecorator("k").getOrDefault((Object) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOrThrowExceptionNullTest() {
		new CcpCacheDecorator("k").getOrThrowException(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putValueNullTest() {
		new CcpCacheDecorator("k").put((Object) null, 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementKeyKeyNullTest() {
		new CcpCacheDecorator("k").incrementKey(null, "v");
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementKeyValueNullTest() {
		new CcpCacheDecorator("k").incrementKey("x", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementKeysJsonKeysJsonNullTest() {
		new CcpCacheDecorator("k").incrementKeys(null, "a");
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementKeysJsonKeysKeysNullTest() {
		new CcpCacheDecorator("k").incrementKeys(CcpOtherConstants.EMPTY_JSON, (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementKeysJsonNullTest() {
		new CcpCacheDecorator("k").incrementKeys(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// delete() é anotado com @CcpAllowNullReturn — pode retornar null legalmente.
}
