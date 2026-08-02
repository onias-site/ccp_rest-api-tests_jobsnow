package com.ccp.implementations.cache.gcp.memcache;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpGcpMemCacheTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpGcpMemCache());
	}

	private static CcpCache getCache() {
		return CcpDependencyInjection.getDependency(CcpCache.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpGcpMemCache());
	}

	@Test
	public void getInstanceTest() {
		CcpCache instance = new CcpGcpMemCache().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) — GcpMemCache via interface CcpCache ───────

	@Test(expected = CcpNullParameterException.class)
	public void getKeyNullTest() {
		getCache().get(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putKeyNullTest() {
		getCache().put(null, "value", 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putValueNullTest() {
		getCache().put("key", null, 10);
	}

	// delete(String) retorna V que pode ser null quando a chave não existe -
	// nesta suíte cobrimos apenas null-parameter.
	@Test(expected = CcpNullParameterException.class)
	public void deleteKeyNullTest() {
		getCache().delete(null);
	}
}
