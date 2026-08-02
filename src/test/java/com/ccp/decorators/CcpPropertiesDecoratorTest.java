package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.Test;

import com.ccp.aop.CcpNullReturnException;
import com.ccp.decorators.CcpInputStreamDecorator.CcpErrorInputStreamMissing;

public class CcpPropertiesDecoratorTest {

	// ── getContent ────────────────────────────────────────────────────────────

	@Test
	public void getContentRetornaInputStreamDecoratorTest() {
		CcpPropertiesDecorator props = new CcpStringDecorator("qualquer").propertiesFrom();
		CcpInputStreamDecorator content = props.getContent();
		assertNotNull(content);
		assertEquals("qualquer", content.getContent());
	}

	// ── classLoader ───────────────────────────────────────────────────────────

	@Test
	public void classLoaderCarregaRecursoDoClasspathTest() {
		CcpPropertiesDecorator props = new CcpStringDecorator("test-recurso.properties").propertiesFrom();
		CcpJsonRepresentation resultado = props.classLoader();
		assertNotNull(resultado);
		assertFalse(resultado.isEmpty());
	}

	// ── environmentVariablesOrClassLoaderOrFile ───────────────────────────────

	@Test
	public void environmentVariablesOrClassLoaderOrFileViaClassLoaderTest() {
		CcpPropertiesDecorator props = new CcpStringDecorator("test-recurso.properties").propertiesFrom();
		CcpJsonRepresentation resultado = props.environmentVariablesOrClassLoaderOrFile();
		assertNotNull(resultado);
		assertFalse(resultado.isEmpty());
	}

	// ── file ──────────────────────────────────────────────────────────────────

	@Test
	public void fileCarregaArquivoTest() {
		String caminho = System.getProperty("java.io.tmpdir") + File.separator + "test-props.json";
		new CcpStringDecorator(caminho).file().write("{\"chave\":\"valor\"}");
		CcpPropertiesDecorator props = new CcpStringDecorator(caminho).propertiesFrom();
		CcpJsonRepresentation resultado = props.file();
		assertNotNull(resultado);
		assertFalse(resultado.isEmpty());
		new CcpStringDecorator(caminho).file().remove();
	}

	// ── environmentVariables ──────────────────────────────────────────────────

	@Test(expected = CcpErrorInputStreamMissing.class)
	public void environmentVariableInexistenteLancaExcecaoTest() {
		new CcpStringDecorator("VARIAVEL_QUE_NAO_EXISTE_PROPS_TEST").propertiesFrom().environmentVariables();
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────
	// Nota: construtor protected e todos os métodos públicos são sem parâmetros.

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
