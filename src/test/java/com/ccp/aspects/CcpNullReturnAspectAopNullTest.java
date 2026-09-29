package com.ccp.aspects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

/**
 * Verifies the contract of the two aspects themselves — that weaving is active in this test module
 * and that the opt-out annotations are honored.
 *
 * <p>
 * {@code CcpNullReturnAspect} cannot be exercised against production code (no production method
 * returns {@code null}: if one did, that would be precisely the defect the aspect reports). That is
 * why the null returns are produced here, by classes of this package — which is also under
 * {@code com.ccp..} and is therefore intercepted by the same pointcuts.
 * </p>
 */
public class CcpNullReturnAspectAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** Test targets intercepted by the aspects because they live in {@code com.ccp..}. */


	private static InterceptedTargets target() {
		return new InterceptedTargets();
	}

	// ── CcpNullReturnAspect ───────────────────────────────────────────────────

	@Test(expected = CcpNullReturnException.class)
	public void nullReturnThrowsExceptionTest() {
		target().returnsNull();
	}

	@Test(expected = CcpNullReturnException.class)
	public void nullCollectionReturnThrowsExceptionTest() {
		target().returnsNullList();
	}

	@Test
	public void annotatedNullReturnDoesNotThrowExceptionTest() {
		assertNull(target().returnsAllowedNull());
	}

	@Test
	public void voidMethodDoesNotThrowExceptionTest() {
		target().voidMethodWithImplicitReturn();
	}

	@Test
	public void nonNullReturnDoesNotThrowExceptionTest() {
		assertNotNull(target().returnsJson());
	}

	// ── CcpNullParameterAspect ────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void nullParameterThrowsExceptionTest() {
		target().receivesParameter(null);
	}

	@Test
	public void annotatedNullParameterDoesNotThrowExceptionTest() {
		assertEquals("null", target().receivesNullableParameter(null));
	}

	@Test
	public void filledParameterDoesNotThrowExceptionTest() {
		assertEquals("ok", target().receivesParameter("ok"));
	}

	// ── exception messages ────────────────────────────────────────────────────

	@Test
	public void nullReturnExceptionMessageTest() {
		try {
			target().returnsNull();
			org.junit.Assert.fail("should have thrown CcpNullReturnException");
		} catch (CcpNullReturnException e) {
			String message = e.getMessage();
			assertNotNull(message);
			org.junit.Assert.assertTrue(message.contains("returnsNull"));
		}
	}

	@Test
	public void nullParameterExceptionMessageTest() {
		try {
			target().receivesParameter(null);
			org.junit.Assert.fail("should have thrown CcpNullParameterException");
		} catch (CcpNullParameterException e) {
			String message = e.getMessage();
			assertNotNull(message);
			org.junit.Assert.assertTrue(message.contains("receivesParameter"));
		}
	}
}
