package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

import com.ccp.hash.CcpHashAlgorithm;

public class CcpHashDecoratorTest {

	@Test
	public void md5DeterministicoTest() {
		String entrada = "teste";
		CcpHashDecorator hash1 = new CcpStringDecorator(entrada).hash();
		CcpHashDecorator hash2 = new CcpStringDecorator(entrada).hash();
		assertEquals(hash1.asString(CcpHashAlgorithm.MD5), hash2.asString(CcpHashAlgorithm.MD5));
	}

	@Test
	public void sha256DeterministicoTest() {
		String entrada = "jobsnow";
		String resultado1 = new CcpStringDecorator(entrada).hash().asString(CcpHashAlgorithm.SHA256);
		String resultado2 = new CcpStringDecorator(entrada).hash().asString(CcpHashAlgorithm.SHA256);
		assertEquals(resultado1, resultado2);
	}

	@Test
	public void sha512DeterministicoTest() {
		String entrada = "senha123";
		String resultado = new CcpStringDecorator(entrada).hash().asString(CcpHashAlgorithm.SHA512);
		assertNotNull(resultado);
		assertFalse(resultado.isEmpty());
	}

	@Test
	public void sha1DeterministicoTest() {
		String entrada = "texto qualquer";
		String resultado = new CcpStringDecorator(entrada).hash().asString(CcpHashAlgorithm.SHA1);
		assertNotNull(resultado);
		assertFalse(resultado.isEmpty());
	}

	@Test
	public void entradaDiferenteGeraHashDiferenteTest() {
		String a = new CcpStringDecorator("abc").hash().asString(CcpHashAlgorithm.MD5);
		String b = new CcpStringDecorator("xyz").hash().asString(CcpHashAlgorithm.MD5);
		assertFalse(a.equals(b));
	}

	@Test
	public void algoritmosGeramsHashsDiferentesTest() {
		String entrada = "comparar";
		CcpHashDecorator hash = new CcpStringDecorator(entrada).hash();
		String md5    = hash.asString(CcpHashAlgorithm.MD5);
		String sha256 = hash.asString(CcpHashAlgorithm.SHA256);
		assertFalse(md5.equals(sha256));
	}

	@Test
	public void asBigIntegerNaoNuloTest() {
		BigInteger bi = new CcpStringDecorator("big").hash().asBigInteger(CcpHashAlgorithm.MD5);
		assertNotNull(bi);
	}

	@Test
	public void toStringRetornaConteudoTest() {
		String entrada = "hash-test";
		CcpHashDecorator hash = new CcpStringDecorator(entrada).hash();
		assertEquals(entrada, hash.toString());
	}

	@Test
	public void getContentRetornaConteudoTest() {
		String entrada = "content";
		CcpHashDecorator hash = new CcpStringDecorator(entrada).hash();
		assertEquals(entrada, hash.getContent());
	}

	@Test
	public void hashResultadoHexadecimalTest() {
		String resultado = new CcpStringDecorator("hex").hash().asString(CcpHashAlgorithm.MD5);
		assertTrue(resultado.matches("[0-9a-f-]+"));
	}
	
	
	@Test(expected = CcpErrorHashAlgorithmNotFound.class)
	public void throwsCcpErrorHashAlgorithmNotFoundTest() {
		CcpHashAlgorithm.getMessageDigest("algoritmoquenaoexiste");
	}
}
