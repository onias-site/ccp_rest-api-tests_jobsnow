package com.ccp.decorators;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.junit.Test;

import com.ccp.aop.CcpNullReturnException;

public class CcpInputStreamDecoratorTest {

	private static final String TMP = System.getProperty("java.io.tmpdir");

	// ── byteArray ─────────────────────────────────────────────────────────────

	@Test
	public void byteArrayAlwaysReturnsStreamTest() {
		InputStream is = new CcpStringDecorator("conteudo qualquer").inputStreamFrom().byteArray();
		assertNotNull(is);
	}

	@Test
	public void byteArrayReadableContentTest() throws Exception {
		String content = "jobsnow";
		InputStream is = new CcpStringDecorator(content).inputStreamFrom().byteArray();
		byte[] bytes = is.readAllBytes();
		assertTrue(new String(bytes).equals(content));
	}

	// ── file ─────────────────────────────────────────────────────────────────

	@Test(expected = CcpErrorInputStreamMissing.class)
	public void missingFileThrowsExceptionTest() {
		// absolute path with an existing parent but a missing file
		String path = TMP + File.separator + "ccp_nao_existe_xyz_" + System.nanoTime() + ".txt";
		new CcpStringDecorator(path).inputStreamFrom().file();
	}

	@Test
	public void existingFileReturnsStreamTest() throws Exception {
		String path = TMP + File.separator + "ccp_test_input.txt";
		new CcpStringDecorator(path).file().write("conteudo de teste");
		InputStream is = new CcpStringDecorator(path).inputStreamFrom().file();
		assertNotNull(is);
		is.close();
		new CcpStringDecorator(path).file().remove();
	}

	// ── classLoader ───────────────────────────────────────────────────────────

	@Test(expected = CcpErrorInputStreamMissing.class)
	public void classLoaderMissingResourceThrowsExceptionTest() {
		new CcpStringDecorator("nao-existe.properties").inputStreamFrom().classLoader();
	}

	// ── environmentVariables ─────────────────────────────────────────────────

	@Test(expected = CcpErrorInputStreamMissing.class)
	public void nonexistentEnvironmentVariableThrowsExceptionTest() {
		new CcpStringDecorator("VARIAVEL_QUE_NAO_EXISTE_CCP_TEST").inputStreamFrom().environmentVariables();
	}

	// ── fromEnvironmentVariablesOrClassLoaderOrFile ───────────────────────────

	@Test
	public void fallbackUsesFileWhenOthersFailTest() throws Exception {
		String path = TMP + File.separator + "ccp_test_fallback.txt";
		new CcpStringDecorator(path).file().write("fallback");
		InputStream is = new CcpStringDecorator(path).inputStreamFrom().fromEnvironmentVariablesOrClassLoaderOrFile();
		assertNotNull(is);
		is.close();
		new CcpStringDecorator(path).file().remove();
	}

	@Test(expected = RuntimeException.class)
	public void fallbackThrowsExceptionWhenEverythingFailsTest() {
		// absolute path with an existing parent but a missing file
		String path = TMP + File.separator + "ccp_nao_existe_fallback_" + System.nanoTime() + ".txt";
		new CcpStringDecorator(path).inputStreamFrom().fromEnvironmentVariablesOrClassLoaderOrFile();
	}

	// ── classLoader with a real resource ──────────────────────────────────────

	@Test
	public void classLoaderExistingResourceReturnsStreamTest() throws Exception {
		InputStream is = new CcpStringDecorator("test-recurso.properties").inputStreamFrom().classLoader();
		assertNotNull(is);
		assertTrue(is.readAllBytes().length > 0);
		is.close();
	}

	// ── toString / getContent ─────────────────────────────────────────────────

	@Test
	public void toStringTest() {
		String name = "meu-recurso";
		CcpInputStreamDecorator d = new CcpStringDecorator(name).inputStreamFrom();
		assertTrue(d.toString().equals(name));
	}

	@Test
	public void getContentTest() {
		String name = "outro-recurso";
		CcpInputStreamDecorator d = new CcpStringDecorator(name).inputStreamFrom();
		assertTrue(d.getContent().equals(name));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────
	// Note: protected constructor and every public method without parameters.

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpInputStreamDecorator withNullContent() throws Exception {
		CcpInputStreamDecorator d = new CcpStringDecorator("x").inputStreamFrom();
		Field f = CcpInputStreamDecorator.class.getDeclaredField("content");
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
