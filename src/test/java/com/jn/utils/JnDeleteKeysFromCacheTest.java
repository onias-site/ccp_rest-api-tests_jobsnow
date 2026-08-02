package com.jn.utils;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

public class JnDeleteKeysFromCacheTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.mock);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnDeleteKeysFromCache.INSTANCE.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void acceptNullTest() {
		JnDeleteKeysFromCache.INSTANCE.accept(null);
	} 
}
