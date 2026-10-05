package com.ccp.dependency.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Ignore;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection.CcpErrorDependencyInjectionMissing;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.especifications.json.CcpJsonHandler;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

/**
 * Proves {@link CcpDependencyInjection#replaceDependenciesTemporally}: the documented contract is to run the business
 * with the given implementations and then restore the previous ones.
 */
public class TemporaryReplacementTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.map);
	}

	@Test
	public void theReplacementLooksForTheProviderInterfaceAndNeverRunsTheBusiness() {
		AtomicBoolean ran = new AtomicBoolean();
		CcpJsonHandler before = CcpDependencyInjection.getDependency(CcpJsonHandler.class);

		try {
			CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, json -> {
				ran.set(true);
				return json;
			}, new CcpGsonJsonHandler());
			fail("finding 57: the provider's own interface has no registered implementation");
		} catch (CcpErrorDependencyInjectionMissing e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith(CcpInstanceProvider.class.getName()));
		}

		assertEquals(false, ran.get());
		assertSame(before, CcpDependencyInjection.getDependency(CcpJsonHandler.class));
	}

	@Ignore("finding 57: replaceDependenciesTemporally reads the interface of the provider, not of the provided instance")
	@Test
	public void theBusinessSeesTheReplacementAndThePreviousImplementationComesBack() {
		CcpCache before = CcpDependencyInjection.getDependency(CcpCache.class);
		CcpCache replacement = CcpLocalCacheInstances.mock.getInstance();

		CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, json -> {
			assertEquals(replacement.getClass(), CcpDependencyInjection.getDependency(CcpCache.class).getClass());
			return json;
		}, CcpLocalCacheInstances.mock);

		assertSame(before, CcpDependencyInjection.getDependency(CcpCache.class));
	}
}
