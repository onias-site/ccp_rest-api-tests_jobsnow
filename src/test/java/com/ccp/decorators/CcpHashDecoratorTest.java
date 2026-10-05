package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.math.BigInteger;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.hash.CcpErrorHashAlgorithmNotFound;

public class CcpHashDecoratorTest {

	@Test
	public void md5DeterministicTest() {
		String input = "teste";
		CcpHashDecorator hash1 = new CcpStringDecorator(input).hash();
		CcpHashDecorator hash2 = new CcpStringDecorator(input).hash();
		assertEquals(hash1.asString(CcpHashAlgorithm.MD5), hash2.asString(CcpHashAlgorithm.MD5));
	}

	@Test
	public void sha256DeterministicTest() {
		String input = "jobsnow";
		String resultado1 = new CcpStringDecorator(input).hash().asString(CcpHashAlgorithm.SHA256);
		String resultado2 = new CcpStringDecorator(input).hash().asString(CcpHashAlgorithm.SHA256);
		assertEquals(resultado1, resultado2);
	}

	@Test
	public void sha512DeterministicTest() {
		String input = "senha123";
		String result = new CcpStringDecorator(input).hash().asString(CcpHashAlgorithm.SHA512);
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	public void sha1DeterministicTest() {
		String input = "texto qualquer";
		String result = new CcpStringDecorator(input).hash().asString(CcpHashAlgorithm.SHA1);
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	public void differentInputGeneratesDifferentHashTest() {
		String a = new CcpStringDecorator("abc").hash().asString(CcpHashAlgorithm.MD5);
		String b = new CcpStringDecorator("xyz").hash().asString(CcpHashAlgorithm.MD5);
		assertFalse(a.equals(b));
	}

	@Test
	public void algorithmsGenerateDifferentHashesTest() {
		String input = "comparar";
		CcpHashDecorator hash = new CcpStringDecorator(input).hash();
		String md5    = hash.asString(CcpHashAlgorithm.MD5);
		String sha256 = hash.asString(CcpHashAlgorithm.SHA256);
		assertFalse(md5.equals(sha256));
	}

	@Test
	public void asBigIntegerNotNullTest() {
		BigInteger bi = new CcpStringDecorator("big").hash().asBigInteger(CcpHashAlgorithm.MD5);
		assertNotNull(bi);
	}

	@Test
	public void toStringReturnsContentTest() {
		String input = "hash-test";
		CcpHashDecorator hash = new CcpStringDecorator(input).hash();
		assertEquals(input, hash.toString());
	}

	@Test
	public void getContentReturnsContentTest() {
		String input = "content";
		CcpHashDecorator hash = new CcpStringDecorator(input).hash();
		assertEquals(input, hash.getContent());
	}

	@Test
	public void hashResultIsHexadecimalTest() {
		String result = new CcpStringDecorator("hex").hash().asString(CcpHashAlgorithm.MD5);
		assertTrue(result.matches("[0-9a-f-]+"));
	}
	
	
	@Test(expected = CcpErrorHashAlgorithmNotFound.class)
	public void throwsCcpErrorHashAlgorithmNotFoundTest() {
		CcpHashAlgorithm.getMessageDigest("algoritmoquenaoexiste");
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void asStringNullParamTest() {
		new CcpStringDecorator("x").hash().asString(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void asBigIntegerNullParamTest() {
		new CcpStringDecorator("x").hash().asBigInteger(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpHashDecorator withNullContent() throws Exception {
		CcpHashDecorator d = new CcpStringDecorator("x").hash();
		Field f = CcpHashDecorator.class.getDeclaredField("content");
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
