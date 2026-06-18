package com.ccp.decorators;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;

import com.ccp.decorators.CcpInputStreamDecorator.CcpErrorInputStreamMissing;

public class CcpInputStreamDecoratorTest {

	private static final String TMP = System.getProperty("java.io.tmpdir");

	// ── byteArray ─────────────────────────────────────────────────────────────

	@Test
	public void byteArraySempreRetornaStreamTest() {
		InputStream is = new CcpStringDecorator("conteudo qualquer").inputStreamFrom().byteArray();
		assertNotNull(is);
	}

	@Test
	public void byteArrayConteudoLegivelTest() throws Exception {
		String conteudo = "jobsnow";
		InputStream is = new CcpStringDecorator(conteudo).inputStreamFrom().byteArray();
		byte[] bytes = is.readAllBytes();
		assertTrue(new String(bytes).equals(conteudo));
	}

	// ── file ─────────────────────────────────────────────────────────────────

	@Test(expected = CcpErrorInputStreamMissing.class)
	public void fileInexistenteLancaExcecaoTest() {
		// caminho absoluto com pai existente mas arquivo inexistente
		String caminho = TMP + File.separator + "ccp_nao_existe_xyz_" + System.nanoTime() + ".txt";
		new CcpStringDecorator(caminho).inputStreamFrom().file();
	}

	@Test
	public void fileExistenteRetornaStreamTest() throws Exception {
		String caminho = TMP + File.separator + "ccp_test_input.txt";
		new CcpStringDecorator(caminho).file().write("conteudo de teste");
		InputStream is = new CcpStringDecorator(caminho).inputStreamFrom().file();
		assertNotNull(is);
		is.close();
		new CcpStringDecorator(caminho).file().remove();
	}

	// ── classLoader ───────────────────────────────────────────────────────────

	@Test(expected = CcpErrorInputStreamMissing.class)
	public void classLoaderRecursoInexistenteLancaExcecaoTest() {
		new CcpStringDecorator("nao-existe.properties").inputStreamFrom().classLoader();
	}

	// ── environmentVariables ─────────────────────────────────────────────────

	@Test(expected = CcpErrorInputStreamMissing.class)
	public void environmentVariableInexistenteLancaExcecaoTest() {
		new CcpStringDecorator("VARIAVEL_QUE_NAO_EXISTE_CCP_TEST").inputStreamFrom().environmentVariables();
	}

	// ── fromEnvironmentVariablesOrClassLoaderOrFile ───────────────────────────

	@Test
	public void fallbackUsaArquivoQuandoOutrosFalhamTest() throws Exception {
		String caminho = TMP + File.separator + "ccp_test_fallback.txt";
		new CcpStringDecorator(caminho).file().write("fallback");
		InputStream is = new CcpStringDecorator(caminho).inputStreamFrom().fromEnvironmentVariablesOrClassLoaderOrFile();
		assertNotNull(is);
		is.close();
		new CcpStringDecorator(caminho).file().remove();
	}

	@Test(expected = RuntimeException.class)
	public void fallbackLancaExcecaoQuandoTudoFalhaTest() {
		// caminho absoluto com pai existente mas arquivo inexistente
		String caminho = TMP + File.separator + "ccp_nao_existe_fallback_" + System.nanoTime() + ".txt";
		new CcpStringDecorator(caminho).inputStreamFrom().fromEnvironmentVariablesOrClassLoaderOrFile();
	}

	// ── classLoader com recurso real ──────────────────────────────────────────

	@Test
	public void classLoaderRecursoExistenteRetornaStreamTest() throws Exception {
		InputStream is = new CcpStringDecorator("test-recurso.properties").inputStreamFrom().classLoader();
		assertNotNull(is);
		assertTrue(is.readAllBytes().length > 0);
		is.close();
	}

	// ── toString / getContent ─────────────────────────────────────────────────

	@Test
	public void toStringTest() {
		String nome = "meu-recurso";
		CcpInputStreamDecorator d = new CcpStringDecorator(nome).inputStreamFrom();
		assertTrue(d.toString().equals(nome));
	}

	@Test
	public void getContentTest() {
		String nome = "outro-recurso";
		CcpInputStreamDecorator d = new CcpStringDecorator(nome).inputStreamFrom();
		assertTrue(d.getContent().equals(nome));
	}
}
