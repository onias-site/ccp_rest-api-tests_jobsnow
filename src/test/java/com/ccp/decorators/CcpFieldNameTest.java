package com.ccp.decorators;

import java.lang.reflect.Field;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;

public class CcpFieldNameTest {

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void constructorObjectNullParamTest() {
		new CcpFieldName((Object) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorStringNullParamTest() {
		new CcpFieldName((String) null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpFieldName withNullName() throws Exception {
		CcpFieldName d = new CcpFieldName("x");
		Field f = CcpFieldName.class.getDeclaredField("name");
		f.setAccessible(true);
		f.set(d, null);
		return d;
	}

	// name() and toString() never return null because they use "" + this.name
	// (even when name is null, the concatenation produces "null").

	@Test
	public void nameDoesNotReturnNullEvenWithNullFieldTest() throws Exception {
		String r = withNullName().name();
		// must not throw CcpNullReturnException because "" + null == "null"
		org.junit.Assert.assertEquals("null", r);
	}

	@Test
	public void toStringDoesNotReturnNullEvenWithNullFieldTest() throws Exception {
		String r = withNullName().toString();
		org.junit.Assert.assertEquals("null", r);
	}

	// Placeholder to satisfy the pattern: no method of CcpFieldName can
	// naturally return null (both use "" + name).
	@SuppressWarnings("unused")
	private static void unusedImportGuard() {
		Class<?> c = CcpNullReturnException.class;
	}
}
