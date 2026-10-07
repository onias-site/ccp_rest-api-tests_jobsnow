package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;

public class CcpFolderDecoratorTest {

	private static final String BASE = System.getProperty("java.io.tmpdir") + File.separator + "ccp_folder_test";

	@Before
	public void createFolder() {
		new File(BASE).mkdirs();
	}

	@After
	public void clear() {
		clearDirectory(BASE);
	}

	private void clearDirectory(String path) {
		java.io.File dir = new java.io.File(path);
		if (!dir.exists()) return;
		java.io.File[] files = dir.listFiles();
		if (files != null) {
			for (java.io.File f : files) {
				if (f.isDirectory()) clearDirectory(f.getAbsolutePath());
				f.delete();
			}
		}
		dir.delete();
	}

	// ── exists ────────────────────────────────────────────────────────────────

	@Test
	public void existsExistingFolderTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertTrue(folder.exists());
	}

	@Test
	public void existsMissingFolderTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE + File.separator + "nao_existe_xyz").folder();
		assertFalse(folder.exists());
	}

	// ── getName ───────────────────────────────────────────────────────────────

	@Test
	public void getNameReturnsFolderNameTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertEquals("ccp_folder_test", folder.getName());
	}

	// ── createNewFolderIfNotExists ────────────────────────────────────────────

	@Test
	public void createNewFolderCreatesFolderTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		CcpFolderDecorator newOne = base.createNewFolderIfNotExists("sub_pasta");
		assertTrue(newOne.exists());
	}

	@Test
	public void createNewFolderDoesNotFailWhenItAlreadyExistsTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		base.createNewFolderIfNotExists("sub_pasta");
		CcpFolderDecorator novamente = base.createNewFolderIfNotExists("sub_pasta");
		assertTrue(novamente.exists());
	}

	// ── createNewFileIfNotExists ──────────────────────────────────────────────

	@Test
	public void createNewFileCreatesFileTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		CcpFileDecorator fileUnderTest = base.createNewFileIfNotExists("novo.txt");
		assertTrue(fileUnderTest.exists());
	}

	// ── writeInTheFile ────────────────────────────────────────────────────────

	@Test
	public void writeInTheFileWritesContentTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		CcpFileDecorator fileUnderTest = base.writeInTheFile("escrito.txt", "conteudo escrito");
		assertTrue(fileUnderTest.getStringContent().contains("conteudo escrito"));
	}

	@Test(expected = CcpErrorFileIsMissing.class)
	public void getStringContentFailTest() {
		String path = BASE + File.separator + "nada";
		CcpFileDecorator fileUnderTest = new CcpStringDecorator(path).file();
		fileUnderTest.getStringContent();
	}

	// ── readFiles ─────────────────────────────────────────────────────────────

	@Test
	public void readFilesIteratesFilesTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		base.createNewFileIfNotExists("a.txt");
		base.createNewFileIfNotExists("b.txt");
		List<String> nomes = new ArrayList<>();
		base.readFiles(f -> nomes.add(f.getName()));
		assertTrue(nomes.size() >= 2);
	}

	// ── readFolders ───────────────────────────────────────────────────────────

	@Test
	public void readFoldersIteratesSubfoldersTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		base.createNewFolderIfNotExists("sub1");
		base.createNewFolderIfNotExists("sub2");
		List<String> nomes = new ArrayList<>();
		base.readFolders(f -> nomes.add(f.getName()));
		assertTrue(nomes.size() >= 2);
	}

	@Test
	public void readFoldersEmptyFolderReturnsWithoutErrorTest() {
		String emptyFolder = BASE + File.separator + "pasta_vazia_test";
		new java.io.File(emptyFolder).mkdir();
		CcpFolderDecorator folder = new CcpStringDecorator(emptyFolder).folder();
		List<String> nomes = new ArrayList<>();
		folder.readFolders(f -> nomes.add(f.getName()));
		assertTrue(nomes.isEmpty());
	}

	// ── asFile ────────────────────────────────────────────────────────────────

	@Test
	public void asFileReturnsFileDecoratorTest() {
		CcpFileDecorator file = new CcpStringDecorator(BASE).folder().asFile();
		assertNotNull(file);
		assertEquals(BASE, file.getContent());
	}

	// ── toString / getContent ────────────────────────────────────────────────

	@Test
	public void toStringReturnsFolderNameTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertEquals("ccp_folder_test", folder.toString());
	}

	@Test
	public void getContentReturnsPathTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertEquals(BASE, folder.getContent());
	}

	// ── zip ───────────────────────────────────────────────────────────────────

	@Test
	public void zipCreatesZipFileTest() {
		CcpFolderDecorator base = new CcpStringDecorator(BASE).folder();
		base.createNewFileIfNotExists("a.txt");
		base.zip();
		File zipFile = new File(new File(BASE).getAbsoluteFile().getParentFile(), base.getName() + ".zip");
		assertTrue(zipFile.exists());
		zipFile.delete();
	}

	// ── parent ────────────────────────────────────────────────────────────────

	@Test
	public void parentPointsToParentDirectoryTest() {
		CcpFolderDecorator folder = new CcpStringDecorator(BASE).folder();
		assertNotNull(folder.parent);
	}

	// ── remove ────────────────────────────────────────────────────────────────

	@Test
	public void removeDeletesFolderTest() {
		String filePath = BASE + File.separator + "pasta_para_remover";
		CcpFolderDecorator folderUnderTest = new CcpStringDecorator(filePath).folder();
		folderUnderTest.createNewFileIfNotExists("dummy.txt"); 
		assertTrue(folderUnderTest.exists());
		folderUnderTest.remove();
		assertFalse(folderUnderTest.exists());
	}

	// ── readFiles em pasta vazia ──────────────────────────────────────────────

	@Test
	public void readFilesEmptyFolderTest() {
		String filePath = BASE + File.separator + "pasta_vazia_files";
		new java.io.File(filePath).mkdir();
		CcpFolderDecorator folder = new CcpStringDecorator(filePath).folder();
		List<String> nomes = new ArrayList<>();
		folder.readFiles(f -> nomes.add(f.getName()));
		assertTrue(nomes.isEmpty());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────
	// Nota: construtor protected — fora do escopo.

	@Test(expected = CcpNullParameterException.class)
	public void readFoldersNullParamTest() {
		new CcpStringDecorator(BASE).folder().readFolders((Consumer<CcpFolderDecorator>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readFilesNullParamTest() {
		new CcpStringDecorator(BASE).folder().readFiles((Consumer<CcpFileDecorator>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createNewFolderIfNotExistsNullParamTest() {
		new CcpStringDecorator(BASE).folder().createNewFolderIfNotExists((String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createNewFileIfNotExistsNullParamTest() {
		new CcpStringDecorator(BASE).folder().createNewFileIfNotExists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void writeInTheFileNullFileNameTest() {
		new CcpStringDecorator(BASE).folder().writeInTheFile(null, "x");
	}

	@Test(expected = CcpNullParameterException.class)
	public void writeInTheFileNullContentTest() {
		new CcpStringDecorator(BASE).folder().writeInTheFile("f.txt", null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpFolderDecorator withNullContent() throws Exception {
		CcpFolderDecorator d = new CcpStringDecorator(BASE).folder();
		Field f = CcpFolderDecorator.class.getDeclaredField("content");
		f.setAccessible(true);
		f.set(d, null);
		return d;
	}

	@Test(expected = CcpNullReturnException.class)
	public void getContentNullReturnTest() throws Exception {
		withNullContent().getContent();
	}
}
