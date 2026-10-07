package com.ccp.implementations.file.bucket.gcp;

import static org.junit.Assert.assertArrayEquals;

import java.util.Base64;

import org.junit.Test;

/**
 * Proves that the content read from the bucket is encoded in Base64 byte for byte (finding 30): until 2026-10-07 the
 * bytes went through a string in the platform charset first, so a binary file (PDF, image) came back corrupted.
 */
public class BinaryContentTest {

	@Test
	public void bytesThatAreNotTextComeBackAsTheyWere() {
		byte[] binary = { (byte) 0x25, (byte) 0x50, (byte) 0xC3, (byte) 0x28, (byte) 0xFF, (byte) 0xFE, 0, (byte) 0x80, (byte) 0xE2, (byte) 0x82 };

		String contentAsBase64 = GcpFileBucket.toBase64(binary);

		assertArrayEquals(binary, Base64.getDecoder().decode(contentAsBase64));
	}
}
