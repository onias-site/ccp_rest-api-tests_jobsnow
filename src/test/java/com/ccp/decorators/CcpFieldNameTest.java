package com.ccp.decorators;

import java.lang.reflect.Field;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;

public class CcpFieldNameTest {

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorObjectNullParamTest() {
		new CcpFieldName((Object) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorStringNullParamTest() {
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

	// name() e toString() nunca retornam null porque usam "" + this.name
	// (mesmo quando name é null, a concatenação produz "null").

	@Test
	public void nameNaoRetornaNullMesmoComCampoNullTest() throws Exception {
		String r = withNullName().name();
		// não deve lançar CcpNullReturnException porque "" + null == "null"
		org.junit.Assert.assertEquals("null", r);
	}

	@Test
	public void toStringNaoRetornaNullMesmoComCampoNullTest() throws Exception {
		String r = withNullName().toString();
		org.junit.Assert.assertEquals("null", r);
	}

	// Placeholder para satisfazer o padrão: nenhum método de CcpFieldName pode
	// naturalmente retornar null (ambos usam "" + name).
	@SuppressWarnings("unused")
	private static void unusedImportGuard() {
		Class<?> c = CcpNullReturnException.class;
	}
}
