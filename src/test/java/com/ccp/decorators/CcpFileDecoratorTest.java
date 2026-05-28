package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpFileDecoratorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final String BASE = System.getProperty("java.io.tmpdir") + "/ccp_file_test/";
	private static final String ARQUIVO = BASE + "teste.txt";

	@Before
	public void criarDiretorio() {
		new CcpStringDecorator(BASE).folder().createNewFileIfNotExists("teste.txt");
	}

	@After
	public void limpar() {
		new CcpStringDecorator(ARQUIVO).file().remove();
	}

	// ── write / getStringContent ──────────────────────────────────────────────

	@Test
	public void writeEGetStringContentTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("linha de conteudo");
		String conteudo = file.getStringContent();
		assertTrue(conteudo.contains("linha de conteudo"));
	}

	@Test
	public void writeSobreescreveConteudoAnteriorTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("primeiro");
		file.write("segundo");
		String conteudo = file.getStringContent();
		assertFalse(conteudo.contains("primeiro"));
		assertTrue(conteudo.contains("segundo"));
	}

	// ── append ────────────────────────────────────────────────────────────────

	@Test
	public void appendAdicionaLinhasTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("linha1");
		file.append("linha2");
		String conteudo = file.getStringContent();
		assertTrue(conteudo.contains("linha1"));
		assertTrue(conteudo.contains("linha2"));
	}

	// ── reset ─────────────────────────────────────────────────────────────────

	@Test
	public void resetApagaConteudoTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("conteudo que sera apagado");
		file.reset();
		String conteudo = file.getStringContent();
		assertTrue(conteudo.trim().isEmpty());
	}

	// ── getLines ─────────────────────────────────────────────────────────────

	@Test
	public void getLinesRetornaLinhasTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("a");
		file.append("b");
		file.append("c");
		List<String> linhas = file.getLines();
		assertTrue(linhas.size() >= 3);
	}

	// ── readLines ─────────────────────────────────────────────────────────────

	@Test
	public void readLinesIteraLinhasTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("primeira");
		file.append("segunda");
		int[] contador = {0};
		file.readLines((linha, numero) -> contador[0]++);
		assertTrue(contador[0] >= 2);
	}

	// ── exists / isFile ───────────────────────────────────────────────────────

	@Test
	public void existsArquivoCriadoTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("x");
		assertTrue(file.exists());
	}

	@Test
	public void existsArquivoNaoExistenteTest() {
		CcpFileDecorator file = new CcpStringDecorator(BASE + "inexistente_xyz.txt").file();
		assertFalse(file.exists());
	}

	@Test
	public void isFileRetornaTrueParaArquivoTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("x");
		assertTrue(file.isFile());
	}

	// ── getName / getPath ─────────────────────────────────────────────────────

	@Test
	public void getNameRetornaNomeDoArquivoTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertEquals("teste.txt", file.getName());
	}

	@Test
	public void getPathRetornaCaminhoAbsolutoTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertNotNull(file.getPath());
		assertTrue(file.getPath().contains("teste.txt"));
	}

	// ── remove ────────────────────────────────────────────────────────────────

	@Test
	public void removeApagaArquivoTest() {
		String path = BASE + "para_remover.txt";
		CcpFileDecorator file = new CcpStringDecorator(path).file();
		file.write("remover");
		assertTrue(file.exists());
		file.remove();
		assertFalse(file.exists());
	}

	// ── rename ────────────────────────────────────────────────────────────────

	@Test
	public void renameRenomearArquivoTest() {
		String original = BASE + "original.txt";
		String novo = BASE + "renomeado.txt";
		CcpFileDecorator file = new CcpStringDecorator(original).file();
		file.write("conteudo");
		CcpFileDecorator renomeado = file.rename(novo);
		assertTrue(renomeado.exists());
		renomeado.remove();
	}

	// ── asSingleJson ──────────────────────────────────────────────────────────

	@Test
	public void asSingleJsonTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("{'nome':'Onias','idade':39}");
		CcpJsonRepresentation json = file.asSingleJson();
		assertNotNull(json);
		assertFalse(json.isEmpty());
	}

	// ── asFolder ──────────────────────────────────────────────────────────────

	@Test
	public void asFolderRetornaFolderDecoratorTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).file().asFolder();
		assertNotNull(folder);
		assertTrue(folder.exists());
	}

	// ── getContent / toString ────────────────────────────────────────────────

	@Test
	public void getContentTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertEquals(ARQUIVO, file.getContent());
	}

	@Test
	public void toStringRetornaNomeTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertEquals("teste.txt", file.toString());
	}

	// ── zip ───────────────────────────────────────────────────────────────────

	@Test
	public void zipCriaArquivoZipTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("conteudo para zipar");
		file.zip();
		File zipFile = new File(file.getName() + ".zip");
		assertTrue(zipFile.exists());
		zipFile.delete();
	}

	// ── parent ────────────────────────────────────────────────────────────────

	@Test
	public void parentApontaParaDiretorioPaiTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertNotNull(file.parent);
		assertTrue(file.parent.content.contains("ccp_file_test"));
	}

	// ── isFile para diretório ─────────────────────────────────────────────────

	@Test
	public void isFileRetornaFalseParaDiretorioTest() {
		CcpFileDecorator file = new CcpStringDecorator(BASE).file();
		assertFalse(file.isFile());
	}

	// ── asJsonList ────────────────────────────────────────────────────────────

	@Test
	public void asJsonListTest() {
		String caminho = BASE + "lista.json";
		CcpFileDecorator file = new CcpStringDecorator(caminho).file();
		file.write("[{\"nome\":\"Onias\"},{\"nome\":\"Alice\"}]");
		List<CcpJsonRepresentation> lista = file.asJsonList();
		assertEquals(2, lista.size());
		file.remove();
	}
}
