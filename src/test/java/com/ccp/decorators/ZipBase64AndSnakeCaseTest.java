package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Test;

/**
 * Proves the zip of a folder ({@link CcpFileDecorator#zip()}), the Base64 decoding with or without a {@code data:}
 * prefix and the conversion to snake case of {@link CcpTextDecorator}.
 */
public class ZipBase64AndSnakeCaseTest {

	/**
	 * Finding 54: the zip goes next to the folder, with the entries named relative to it. Until 2026-10-07 it went to the
	 * working directory of the process, with the entries named by the full path.
	 */
	@Test
	public void aFolderIsZippedNextToItWithItsFilesNamedRelativeToIt() throws Exception {
		String folderName = "zip_test_" + System.nanoTime();
		File folder = Files.createTempDirectory(folderName).toFile();
		Files.writeString(new File(folder, "a.txt").toPath(), "content of a");
		String folderPath = folder.getAbsolutePath().replace('\\', '/');

		new CcpFileDecorator(folderPath).zip();

		File zip = new File(folder.getParentFile(), folder.getName() + ".zip");
		try {
			assertTrue("the zip goes next to the folder: " + zip, zip.exists());
			List<String> entries = new ArrayList<>();
			try (ZipFile zipFile = new ZipFile(zip)) {
				for (ZipEntry entry : Collections.list(zipFile.entries())) {
					entries.add(entry.getName());
				}
			}
			assertEquals(Arrays.asList(folder.getName() + "/", folder.getName() + "/a.txt"), entries);
		} finally {
			zip.delete();
			new File(folder, "a.txt").delete();
			folder.delete();
		}
	}

	@Test
	public void base64IsDecodedWithOrWithoutADataPrefix() {
		assertEquals("hi", new String(new CcpTextDecorator("aGk=").getByteArrayFromBase64String()));
		assertEquals("hi", new String(new CcpTextDecorator("data:text/plain;base64,aGk=").getByteArrayFromBase64String()));
	}

	@Test
	public void camelCaseBecomesSnakeCase() {
		assertEquals("first_name_and_last", new CcpTextDecorator("firstNameAndLast").toSnakeCase().content);
		assertEquals("id_2", new CcpTextDecorator("Id_2").toSnakeCase().content);
		assertEquals("already_snake", new CcpTextDecorator("already_snake").toSnakeCase().content);
	}
}
