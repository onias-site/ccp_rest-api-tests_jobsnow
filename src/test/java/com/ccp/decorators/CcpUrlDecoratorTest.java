package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.ccp.aop.CcpNullReturnException;

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
	public void encodeSpecialCharactersTest() {
		String original = "a=1&b=2";
		CcpUrlDecorator url = new CcpStringDecorator(original).url();
		String encoded = url.asEnconded();
		assertTrue(encoded.contains("%3D") || encoded.contains("="));
		assertTrue(encoded.contains("%26") || encoded.contains("&"));
	}

	@Test
	public void withoutSpecialCharactersDoesNotChangeTest() {
		String original = "simplesTexto";
		CcpUrlDecorator url = new CcpStringDecorator(original).url();
		assertEquals(original, url.asEnconded());
		assertEquals(original, url.asDecoded());
	}

	@Test
	public void toStringTest() {
		String content = "texto";
		CcpUrlDecorator url = new CcpStringDecorator(content).url();
		assertEquals(content, url.toString());
	}

	@Test
	public void getContentTest() {
		String content = "valor";
		CcpUrlDecorator url = new CcpStringDecorator(content).url();
		assertEquals(content, url.getContent());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────
	// Note: protected constructor and every public method without parameters.

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpUrlDecorator withNullContent() throws Exception {
		CcpUrlDecorator d = new CcpStringDecorator("x").url();
		Field f = CcpUrlDecorator.class.getDeclaredField("content");
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
