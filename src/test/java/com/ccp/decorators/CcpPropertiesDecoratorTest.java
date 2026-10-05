package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.Test;

import com.ccp.aop.CcpNullReturnException;

public class CcpPropertiesDecoratorTest {

	// ── getContent ────────────────────────────────────────────────────────────

	@Test
	public void getContentReturnsInputStreamDecoratorTest() {
		CcpPropertiesDecorator props = new CcpStringDecorator("qualquer").propertiesFrom();
		CcpInputStreamDecorator content = props.getContent();
		assertNotNull(content);
		assertEquals("qualquer", content.getContent());
	}

	// ── classLoader ───────────────────────────────────────────────────────────

	@Test
	public void classLoaderLoadsResourceFromClasspathTest() {
		CcpPropertiesDecorator props = new CcpStringDecorator("test-recurso.properties").propertiesFrom();
		CcpJsonRepresentation result = props.classLoader();
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	// ── environmentVariablesOrClassLoaderOrFile ───────────────────────────────

	@Test
	public void environmentVariablesOrClassLoaderOrFileViaClassLoaderTest() {
		CcpPropertiesDecorator props = new CcpStringDecorator("test-recurso.properties").propertiesFrom();
		CcpJsonRepresentation result = props.environmentVariablesOrClassLoaderOrFile();
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	// ── file ──────────────────────────────────────────────────────────────────

	@Test
	public void fileLoadsFileTest() {
		String path = System.getProperty("java.io.tmpdir") + File.separator + "test-props.json";
		new CcpStringDecorator(path).file().write("{\"chave\":\"valor\"}");
		CcpPropertiesDecorator props = new CcpStringDecorator(path).propertiesFrom();
		CcpJsonRepresentation result = props.file();
		assertNotNull(result);
		assertFalse(result.isEmpty());
		new CcpStringDecorator(path).file().remove();
	}

	// ── environmentVariables ──────────────────────────────────────────────────

	@Test(expected = CcpErrorInputStreamMissing.class)
	public void nonexistentEnvironmentVariableThrowsExceptionTest() {
		new CcpStringDecorator("VARIAVEL_QUE_NAO_EXISTE_PROPS_TEST").propertiesFrom().environmentVariables();
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────
	// Note: protected constructor and every public method without parameters.

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	@Test(expected = CcpNullReturnException.class)
	public void getContentNullReturnTest() throws Exception {
		CcpPropertiesDecorator d = new CcpStringDecorator("x").propertiesFrom();
		Field f = CcpPropertiesDecorator.class.getDeclaredField("content");
		f.setAccessible(true);
		f.set(d, null);
		d.getContent();
	}
}
