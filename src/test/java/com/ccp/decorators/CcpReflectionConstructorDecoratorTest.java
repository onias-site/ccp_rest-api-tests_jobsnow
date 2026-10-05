package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;
import com.ccp.constants.CcpOtherConstants;

public class CcpReflectionConstructorDecoratorTest {

	// ── thisClassExists ────────────────────────────────────────────────────────

	@Test
	public void thisClassExistsTrueTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		assertTrue(refl.thisClassExists());
	}

	@Test
	public void thisClassExistsFalseTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("com.nao.existe.Classe");
		assertFalse(refl.thisClassExists());
	}

	// ── forName ───────────────────────────────────────────────────────────────

	@Test
	public void forNameReturnsClassTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		Class<?> clazz = refl.forName();
		assertEquals(ArrayList.class, clazz);
	}

	@Test(expected = RuntimeException.class)
	public void forNameMissingClassThrowsExceptionTest() {
		new CcpReflectionConstructorDecorator("com.nao.existe.Classe").forName();
	}

	@Test(expected = RuntimeException.class)
	public void test() {
		CcpJsonFieldName field = new CcpFieldName("nomes7");
		new CcpReflectionConstructorDecorator(CcpOtherConstants.EMPTY_JSON.put(field, "com.nao.existe.Classe"), field.getValue()).forName();
		
	}
	// ── newInstance ───────────────────────────────────────────────────────────

	@Test
	public void newInstanceCreatesInstanceTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		Object instance = refl.newInstance();
		assertNotNull(instance);
		assertTrue(instance instanceof ArrayList);
	}

	@Test(expected = RuntimeException.class)
	public void newInstanceMissingClassThrowsExceptionTest() {
		new CcpReflectionConstructorDecorator("com.nao.existe.Classe").newInstance();
	}

	// ── getContent / toString ──────────────────────────────────────────────────

	@Test
	public void getContentReturnsClassNameTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		assertEquals("java.util.ArrayList", refl.getContent());
	}

	@Test
	public void toStringReturnsClassNameTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		assertEquals("java.util.ArrayList", refl.toString());
	}

	// ── constructor with Class<?> ──────────────────────────────────────────────

	@Test
	public void constructorWithClassTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator(ArrayList.class);
		assertEquals("java.util.ArrayList", refl.getContent());
		assertTrue(refl.thisClassExists());
	}

	// ── fromNewInstance / fromStaticContext / fromInstance ────────────────────

	@Test
	public void fromNewInstanceReturnsDecoratorTest() {
		CcpReflectionOptionsDecorator opt = new CcpReflectionConstructorDecorator("java.util.ArrayList").fromNewInstance();
		assertNotNull(opt);
		assertEquals(ArrayList.class, opt.getContent());
	}

	@Test
	public void fromStaticContextReturnsDecoratorTest() {
		CcpReflectionOptionsDecorator opt = new CcpReflectionConstructorDecorator("java.util.ArrayList").fromStaticContext();
		assertNotNull(opt);
		assertEquals(ArrayList.class, opt.getContent());
	}

	@Test
	public void fromInstanceReturnsDecoratorTest() {
		ArrayList<Object> list = new ArrayList<>();
		CcpReflectionOptionsDecorator opt = new CcpReflectionConstructorDecorator("java.util.ArrayList").fromInstance(list);
		assertNotNull(opt);
		assertEquals(ArrayList.class, opt.getContent());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void constructorJsonNullParamJsonTest() {
		new CcpReflectionConstructorDecorator((CcpJsonRepresentation) null, "campo");
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorJsonNullParamFieldTest() {
		new CcpReflectionConstructorDecorator(CcpOtherConstants.EMPTY_JSON, (String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorClassNullParamTest() {
		new CcpReflectionConstructorDecorator((Class<?>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void fromInstanceNullParamTest() {
		new CcpReflectionConstructorDecorator("java.util.ArrayList").fromInstance(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpReflectionConstructorDecorator withNullContent() throws Exception {
		CcpReflectionConstructorDecorator d = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		Field f = CcpReflectionConstructorDecorator.class.getDeclaredField("content");
		f.setAccessible(true);
		f.set(d, null);
		return d;
	}

	@Test(expected = CcpNullReturnException.class)
	public void getContentNullReturnTest() throws Exception {
		withNullContent().getContent();
	}

	@Test(expected = CcpNullReturnException.class)
	public void toStringNullReturnTest() throws Exception {
		withNullContent().toString();
	}
}
