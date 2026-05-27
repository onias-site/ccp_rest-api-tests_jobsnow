package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CcpUrlDecoratorTest {

	@Test
	public void encodeTest() {
		String original = "nome completo";
		CcpUrlDecorator url = new CcpStringDecorator(original).url();
		String encoded = url.asEnconded();
		assertEquals("nome+completo", encoded);
	}

	@Test
	public void decodeTest() {
		String encoded = "nome+completo";
		CcpUrlDecorator url = new CcpStringDecorator(encoded).url();
		String decoded = url.asDecoded();
		assertEquals("nome completo", decoded);
	}

	@Test
	public void roundtripEncodeDecodeTest() {
		String original = "e-mail: usuario@exemplo.com & cargo=dev";
		CcpUrlDecorator url = new CcpStringDecorator(original).url();
		String encoded = url.asEnconded();
		String decoded = new CcpStringDecorator(encoded).url().asDecoded();
		assertEquals(original, decoded);
	}

	@Test
	public void encodeCaracteresEspeciaisTest() {
		String original = "a=1&b=2";
		CcpUrlDecorator url = new CcpStringDecorator(original).url();
		String encoded = url.asEnconded();
		assertTrue(encoded.contains("%3D") || encoded.contains("="));
		assertTrue(encoded.contains("%26") || encoded.contains("&"));
	}

	@Test
	public void semCaracteresEspeciaisNaoMudaTest() {
		String original = "simplesTexto";
		CcpUrlDecorator url = new CcpStringDecorator(original).url();
		assertEquals(original, url.asEnconded());
		assertEquals(original, url.asDecoded());
	}

	@Test
	public void toStringTest() {
		String conteudo = "texto";
		CcpUrlDecorator url = new CcpStringDecorator(conteudo).url();
		assertEquals(conteudo, url.toString());
	}

	@Test
	public void getContentTest() {
		String conteudo = "valor";
		CcpUrlDecorator url = new CcpStringDecorator(conteudo).url();
		assertEquals(conteudo, url.getContent());
	}
}
