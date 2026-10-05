package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpFileDecoratorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final String BASE = System.getProperty("java.io.tmpdir") + "/ccp_file_test/";
	private static final String ARQUIVO = BASE + "teste.txt";

	@Before
	public void createDirectory() {
		new CcpStringDecorator(BASE).folder().createNewFileIfNotExists("teste.txt");
	}

	@After
	public void clear() {
		new CcpStringDecorator(ARQUIVO).file().remove();
	}

	// ── write / getStringContent ──────────────────────────────────────────────

	@Test
	public void writeEGetStringContentTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("linha de conteudo");
		String fileContent = file.getStringContent();
		assertTrue(fileContent.contains("linha de conteudo"));
	}

	@Test
	public void writeOverwritesPreviousContentTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("primeiro");
		file.write("segundo");
		String fileContent = file.getStringContent();
		assertFalse(fileContent.contains("primeiro"));
		assertTrue(fileContent.contains("segundo"));
	}

	// ── append ────────────────────────────────────────────────────────────────

	@Test
	public void appendAddsLinesTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("linha1");
		file.append("linha2");
		String fileContent = file.getStringContent();
		assertTrue(fileContent.contains("linha1"));
		assertTrue(fileContent.contains("linha2"));
	}

	// ── reset ─────────────────────────────────────────────────────────────────

	@Test
	public void resetDeletesContentTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("conteudo que sera apagado");
		file.reset();
		String fileContent = file.getStringContent();
		assertTrue(fileContent.trim().isEmpty());
	}

	// ── getLines ─────────────────────────────────────────────────────────────

	@Test
	public void getLinesReturnsLinesTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("a");
		file.append("b");
		file.append("c");
		List<String> lines = file.getLines();
		assertTrue(lines.size() >= 3);
	}

	// ── readLines ─────────────────────────────────────────────────────────────

	@Test
	public void readLinesIteratesLinesTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("primeira");
		file.append("segunda");
		int[] counter = {0};
		file.readLines((line, number) -> counter[0]++);
		assertTrue(counter[0] >= 2);
	}

	// ── exists / isFile ───────────────────────────────────────────────────────

	@Test
	public void existsCreatedFileTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("x");
		assertTrue(file.exists());
	}

	@Test
	public void existsNonExistingFileTest() {
		CcpFileDecorator file = new CcpStringDecorator(BASE + "inexistente_xyz.txt").file();
		assertFalse(file.exists());
	}

	@Test
	public void isFileReturnsTrueForFileTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("x");
		assertTrue(file.isFile());
	}

	// ── getName / getPath ─────────────────────────────────────────────────────

	@Test
	public void getNameReturnsFileNameTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertEquals("teste.txt", file.getName());
	}

	@Test
	public void getPathReturnsAbsolutePathTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertNotNull(file.getPath());
		assertTrue(file.getPath().contains("teste.txt"));
	}

	// ── remove ────────────────────────────────────────────────────────────────

	@Test
	public void removeDeletesFileTest() {
		String path = BASE + "para_remover.txt";
		CcpFileDecorator file = new CcpStringDecorator(path).file();
		file.write("remover");
		assertTrue(file.exists());
		file.remove();
		assertFalse(file.exists());
	}

	// ── rename ────────────────────────────────────────────────────────────────

	@Test
	public void renameRenamesFileTest() {
		String original = BASE + "original.txt";
		String newOne = BASE + "renomeado.txt";
		CcpFileDecorator file = new CcpStringDecorator(original).file();
		file.write("conteudo");
		CcpFileDecorator renomeado = file.rename(newOne);
		assertTrue(renomeado.exists());
		renomeado.remove();
	}

	// ── asSingleJson ──────────────────────────────────────────────────────────

	@Test
	public void asSingleJsonTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("{'name':'Onias','age':39}");
		CcpJsonRepresentation json = file.asSingleJson();
		assertNotNull(json);
		assertFalse(json.isEmpty());
	}

	// ── asFolder ──────────────────────────────────────────────────────────────

	@Test
	public void asFolderReturnsFolderDecoratorTest() {
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
	public void toStringReturnsNameTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertEquals("teste.txt", file.toString());
	}

	// ── zip ───────────────────────────────────────────────────────────────────

	@Test
	public void zipCreatesZipFileTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		file.write("conteudo para zipar");
		file.zip();
		File zipFile = new File(file.getName() + ".zip");
		assertTrue(zipFile.exists());
		zipFile.delete();
	}

	// ── parent ────────────────────────────────────────────────────────────────

	@Test
	public void parentPointsToParentDirectoryTest() {
		CcpFileDecorator file = new CcpStringDecorator(ARQUIVO).file();
		assertNotNull(file.parent);
		assertTrue(file.parent.content.contains("ccp_file_test"));
	}

	// ── isFile for a directory ────────────────────────────────────────────────

	@Test
	public void isFileReturnsFalseForDirectoryTest() {
		CcpFileDecorator file = new CcpStringDecorator(BASE).file();
		assertFalse(file.isFile());
	}

	// ── asJsonList ────────────────────────────────────────────────────────────

	@Test
	public void asJsonListTest() {
		String filePath = BASE + "lista.json";
		CcpFileDecorator file = new CcpStringDecorator(filePath).file();
		file.write("[{\"nome\":\"Onias\"},{\"nome\":\"Alice\"}]");
		List<CcpJsonRepresentation> list = file.asJsonList();
		assertEquals(2, list.size());
		file.remove();
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────
	// Nota: construtor protected — fora do escopo.

	@Test(expected = CcpNullParameterException.class)
	public void writeNullParamTest() {
		new CcpStringDecorator(ARQUIVO).file().write(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void appendNullParamTest() {
		new CcpStringDecorator(ARQUIVO).file().append(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readLinesNullParamTest() {
		new CcpStringDecorator(ARQUIVO).file().readLines((FileLineReader) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void renameNullParamTest() {
		new CcpStringDecorator(ARQUIVO).file().rename(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpFileDecorator withNullContent() throws Exception {
		CcpFileDecorator d = new CcpStringDecorator(ARQUIVO).file();
		Field f = CcpFileDecorator.class.getDeclaredField("content");
		f.setAccessible(true);
		f.set(d, null);
		return d;
	}

	@Test(expected = CcpNullReturnException.class)
	public void getContentNullReturnTest() throws Exception {
		withNullContent().getContent();
	}
}
