package com.ccp.decorators;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ccp.hash.CcpHashAlgorithm;

/**
 * Locks the format of {@link CcpHashDecorator#asString(CcpHashAlgorithm)} (finding 14, kept by decision on 2026-10-07):
 * the signed hexadecimal of the digest, which may start with '-' and drops the leading zeros. It is the text of every
 * persisted id and hash, so a change here changes every stored key; this test fails before that happens unnoticed.
 */
public class HashFormatContractTest {

	private String sha1Of(String text) {
		String hash = new CcpStringDecorator(text).hash().asString(CcpHashAlgorithm.SHA1);
		return hash;
	}

	@Test
	public void aDigestWithTheHighBitSetComesOutWithAMinusSign() {
		assertEquals("-79081bc8055a58031ea2e22346151515c8899848", this.sha1Of("a"));
	}

	@Test
	public void aDigestWithALeadingZeroComesOutWithoutIt() {
		String hash = this.sha1Of("i");

		assertEquals("42dc4512fa3d391c5170cf3aa61e6a638f84342", hash);
		assertEquals(39, hash.length());
	}
}
