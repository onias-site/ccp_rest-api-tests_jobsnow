package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the side effects and the charset of {@link CcpFileDecorator} and the removal of {@link CcpFolderDecorator}
 * (finding 12): until 2026-10-07 reading a path created its parent directories, appending did not create them, the text
 * was written in the platform charset and read as UTF-8, a missing file raised an error about its parent folder, and
 * removing a folder was not recursive and raised NullPointerException when the folder did not exist.
 */
public class FileSideEffectsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final String ROOT = new File(System.getProperty("java.io.tmpdir"), "ccp_file_side_effects_test").getAbsolutePath();

	/** Accented text written with escapes, so the source encoding does not matter. */
	private static final String ACCENTED = "ação café";

	/**
	 * Starts from a missing root, removed with java.nio instead of the code under test: a run against the old code (the
	 * proof of the finding) leaves folders behind, since its folder removal was not recursive.
	 */
	@Before
	public void startWithoutTheRoot() throws IOException {
		Path root = Paths.get(ROOT);
		if (Files.notExists(root)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(root)) {
			List<Path> deepestFirst = paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
			for (Path path : deepestFirst) {
				Files.delete(path);
			}
		}
	}

	@After
	public void removeTheRoot() {
		new CcpStringDecorator(ROOT).folder().remove();
	}

	private String path(String... names) {
		String path = ROOT + File.separator + String.join(File.separator, names);
		return path;
	}

	private CcpFileDecorator file(String... names) {
		CcpFileDecorator file = new CcpStringDecorator(this.path(names)).file();
		return file;
	}

	@Test
	public void askingAboutAPathDoesNotCreateItsFolders() {
		CcpFileDecorator file = this.file("a", "b", "x.txt");

		assertFalse(file.exists());
		file.getName();
		file.getPath();
		file.remove();

		assertFalse(new File(this.path("a")).exists());
	}

	@Test
	public void readingAMissingFileNamesTheFileAndCreatesNoFolder() {
		CcpFileDecorator file = this.file("missing", "x.txt");
		try {
			file.getStringContent();
			org.junit.Assert.fail("the file does not exist");
		} catch (CcpErrorFileIsMissing expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("x.txt"));
		}
		assertFalse(new File(this.path("missing")).exists());
	}

	@Test
	public void appendingCreatesTheMissingFolders() {
		CcpFileDecorator file = this.file("new", "deep", "x.txt");

		file.append("line");

		assertEquals("line\n", file.getStringContent());
	}

	@Test
	public void accentedTextComesBackAsItWasWritten() {
		CcpFileDecorator file = this.file("x.txt");

		file.write(ACCENTED);

		assertEquals(ACCENTED + "\n", file.getStringContent());
		assertEquals(Arrays.asList(ACCENTED), file.getLines());
		List<String> linesRead = new ArrayList<>();
		file.readLines((line, index) -> linesRead.add(line));
		assertEquals(Arrays.asList(ACCENTED), linesRead);
	}

	@Test
	public void removingAFolderRemovesEverythingInside() {
		this.file("tree", "sub", "deeper", "x.txt").write("x");
		this.file("tree", "y.txt").write("y");
		CcpFolderDecorator tree = new CcpStringDecorator(this.path("tree")).folder();

		tree.remove();

		assertFalse(new File(this.path("tree")).exists());
	}

	@Test
	public void removingAFolderThatDoesNotExistDoesNothing() {
		CcpFolderDecorator missing = new CcpStringDecorator(this.path("never", "created")).folder();

		missing.remove();

		assertFalse(new File(this.path("never")).exists());
	}
}
