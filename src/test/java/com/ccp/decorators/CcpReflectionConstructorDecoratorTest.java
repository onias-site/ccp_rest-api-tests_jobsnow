package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

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
	public void forNameRetornaClassTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		Class<?> clazz = refl.forName();
		assertEquals(ArrayList.class, clazz);
	}

	@Test(expected = RuntimeException.class)
	public void forNameClasseInexistenteLancaExcecaoTest() {
		new CcpReflectionConstructorDecorator("com.nao.existe.Classe").forName();
	}

	// ── newInstance ───────────────────────────────────────────────────────────

	@Test
	public void newInstanceCriaInstanciaTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		Object instance = refl.newInstance();
		assertNotNull(instance);
		assertTrue(instance instanceof ArrayList);
	}

	@Test(expected = RuntimeException.class)
	public void newInstanceClasseInexistenteLancaExcecaoTest() {
		new CcpReflectionConstructorDecorator("com.nao.existe.Classe").newInstance();
	}

	// ── getContent / toString ──────────────────────────────────────────────────

	@Test
	public void getContentRetornaNomeClasseTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		assertEquals("java.util.ArrayList", refl.getContent());
	}

	@Test
	public void toStringRetornaNomeClasseTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator("java.util.ArrayList");
		assertEquals("java.util.ArrayList", refl.toString());
	}

	// ── construtor com Class<?> ────────────────────────────────────────────────

	@Test
	public void construtorComClassTest() {
		CcpReflectionConstructorDecorator refl = new CcpReflectionConstructorDecorator(ArrayList.class);
		assertEquals("java.util.ArrayList", refl.getContent());
		assertTrue(refl.thisClassExists());
	}

	// ── fromNewInstance / fromStaticContext / fromInstance ────────────────────

	@Test
	public void fromNewInstanceRetornaDecoratorTest() {
		CcpReflectionOptionsDecorator opt = new CcpReflectionConstructorDecorator("java.util.ArrayList").fromNewInstance();
		assertNotNull(opt);
		assertEquals(ArrayList.class, opt.getContent());
	}

	@Test
	public void fromStaticContextRetornaDecoratorTest() {
		CcpReflectionOptionsDecorator opt = new CcpReflectionConstructorDecorator("java.util.ArrayList").fromStaticContext();
		assertNotNull(opt);
		assertEquals(ArrayList.class, opt.getContent());
	}

	@Test
	public void fromInstanceRetornaDecoratorTest() {
		ArrayList<Object> lista = new ArrayList<>();
		CcpReflectionOptionsDecorator opt = new CcpReflectionConstructorDecorator("java.util.ArrayList").fromInstance(lista);
		assertNotNull(opt);
		assertEquals(ArrayList.class, opt.getContent());
	}
}
