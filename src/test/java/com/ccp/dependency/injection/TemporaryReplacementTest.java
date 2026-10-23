package com.ccp.dependency.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

/**
 * Proves {@link CcpDependencyInjection#replaceDependenciesTemporally}: the business runs with the given implementations
 * and the registry comes back exactly as it was, also when the business throws. Until 2026-10-07 the method looked for
 * the previous implementation under the provider's own interface and never ran the business (finding 57), and the
 * restoration was skipped when the business threw (finding 2).
 */
public class TemporaryReplacementTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpLocalCacheInstances.map);
	}

	/** An interface that no other test registers. */
	public interface Greeter {
		String greet();
	}

	/** Implementation that declares the interface. */
	public static class PlainGreeter implements Greeter {
		public String greet() {
			return "hello";
		}
	}

	/** Implementation that declares no interface of its own, like an enum constant with a body. */
	public static class InheritedGreeter extends PlainGreeter {
		/** The single instance: the class keeps no state. */
		public final static InheritedGreeter INSTANCE = new InheritedGreeter();

		private InheritedGreeter() {}

		public String greet() {
			return "hi";
		}
	}

	@Test
	public void theBusinessSeesTheReplacementAndThePreviousImplementationComesBack() {
		CcpCache before = CcpDependencyInjection.getDependency(CcpCache.class);
		CcpCache replacement = CcpLocalCacheInstances.mock.getInstance();
		AtomicBoolean ran = new AtomicBoolean();

		CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, new TemporaryReplacementCacheCheck(replacement.getClass(), ran), CcpLocalCacheInstances.mock);

		assertTrue(ran.get());
		assertSame(before, CcpDependencyInjection.getDependency(CcpCache.class));
	}

	@Test
	public void thePreviousImplementationComesBackEvenWhenTheBusinessThrows() {
		CcpCache before = CcpDependencyInjection.getDependency(CcpCache.class);

		try {
			CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, TemporaryReplacementFailingBusiness.INSTANCE, CcpLocalCacheInstances.mock);
			fail("the business throws");
		} catch (TemporaryReplacementBusinessFailed expected) {
		}

		assertSame(before, CcpDependencyInjection.getDependency(CcpCache.class));
	}

	@Test
	public void anInterfaceWithoutImplementationBeforeIsLeftWithoutOne() {
		CcpDependencyInjection.removeDependecy(Greeter.class);

		String greeting = CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, TemporaryReplacementGreeting.INSTANCE, () -> new PlainGreeter()).getAsString(TemporaryReplacementGreeting.greeting);

		assertEquals("hello", greeting);
		assertFalse(CcpDependencyInjection.hasDependency(Greeter.class));
	}

	@Test
	public void anImplementationWithoutInterfaceOfItsOwnIsRegisteredUnderTheInheritedOne() {
		try {
			CcpDependencyInjection.loadAllDependencies(() -> InheritedGreeter.INSTANCE);

			assertEquals("hi", CcpDependencyInjection.getDependency(Greeter.class).greet());
		} finally {
			CcpDependencyInjection.removeDependecy(Greeter.class);
		}
	}
}
