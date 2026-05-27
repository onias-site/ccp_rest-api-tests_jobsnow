package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CcpFolderDecoratorTest {

	private static final String BASE = System.getProperty("java.io.tmpdir") + File.separator + "ccp_folder_test";

	@Before
	public void criarPasta() {
		new File(BASE).mkdirs();
	}

	@After
	public void limpar() {
		limparDiretorio(BASE);
	}

	private void limparDiretorio(String path) {
		java.io.File dir = new java.io.File(path);
		if (!dir.exists()) return;
		java.io.File[] arquivos = dir.listFiles();
		if (arquivos != null) {
			for (java.io.File f : arquivos) {
				if (f.isDirectory()) limparDiretorio(f.getAbsolutePath());
				f.delete();
			}
		}
		dir.delete();
	}

	// ── exists ────────────────────────────────────────────────────────────────

	@Test
	public void existsPastaExistenteTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertTrue(folder.exists());
	}

	@Test
	public void existsPastaInexistenteTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE + File.separator + "nao_existe_xyz").folder();
		assertFalse(folder.exists());
	}

	// ── getName ───────────────────────────────────────────────────────────────

	@Test
	public void getNomeRetornaNomeDaPastaTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertEquals("ccp_folder_test", folder.getName());
	}

	// ── createNewFolderIfNotExists ────────────────────────────────────────────

	@Test
	public void createNewFolderCriaPastaTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		CcpFolderDecorator nova = base.createNewFolderIfNotExists("sub_pasta");
		assertTrue(nova.exists());
	}

	@Test
	public void createNewFolderNaoFalhaSeJaExistirTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		base.createNewFolderIfNotExists("sub_pasta");
		CcpFolderDecorator novamente = base.createNewFolderIfNotExists("sub_pasta");
		assertTrue(novamente.exists());
	}

	// ── createNewFileIfNotExists ──────────────────────────────────────────────

	@Test
	public void createNewFileCriaArquivoTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		CcpFileDecorator arquivo = base.createNewFileIfNotExists("novo.txt");
		assertTrue(arquivo.exists());
	}

	// ── writeInTheFile ────────────────────────────────────────────────────────

	@Test
	public void writeInTheFileEscreverConteudoTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		CcpFileDecorator arquivo = base.writeInTheFile("escrito.txt", "conteudo escrito");
		assertTrue(arquivo.getStringContent().contains("conteudo escrito"));
	}

	// ── readFiles ─────────────────────────────────────────────────────────────

	@Test
	public void readFilesIteraArquivosTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		base.createNewFileIfNotExists("a.txt");
		base.createNewFileIfNotExists("b.txt");
		List<String> nomes = new ArrayList<>();
		base.readFiles(f -> nomes.add(f.getName()));
		assertTrue(nomes.size() >= 2);
	}

	// ── readFolders ───────────────────────────────────────────────────────────

	@Test
	public void readFoldersIteraSubpastasTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		base.createNewFolderIfNotExists("sub1");
		base.createNewFolderIfNotExists("sub2");
		List<String> nomes = new ArrayList<>();
		base.readFolders(f -> nomes.add(f.getName()));
		assertTrue(nomes.size() >= 2);
	}

	@Test
	public void readFoldersPastaVaziaRetornaSemErroTest() {
		String pastaVazia = BASE + File.separator + "pasta_vazia_test";
		new java.io.File(pastaVazia).mkdir();
		CcpFolderDecorator folder = new CcpStringDecorator(pastaVazia).folder();
		List<String> nomes = new ArrayList<>();
		folder.readFolders(f -> nomes.add(f.getName()));
		assertTrue(nomes.isEmpty());
	}

	// ── asFile ────────────────────────────────────────────────────────────────

	@Test
	public void asFileRetornaFileDecoratorTest() {
		CcpFileDecorator file = new CcpStringDecorator(BASE).folder().asFile();
		assertNotNull(file);
		assertEquals(BASE, file.getContent());
	}

	// ── toString / getContent ────────────────────────────────────────────────

	@Test
	public void toStringRetornaNomeDaPastaTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertEquals("ccp_folder_test", folder.toString());
	}

	@Test
	public void getContentRetornaCaminhoTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertEquals(BASE, folder.getContent());
	}
}
