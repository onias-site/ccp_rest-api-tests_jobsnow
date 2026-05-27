package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpStringDecoratorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── construtores ──────────────────────────────────────────────────────────

	@Test
	public void construtorStringTest() {
		CcpStringDecorator d = new CcpStringDecorator("hello");
		assertEquals("hello", d.content);
	}

	@Test
	public void construtorByteArrayTest() {
		byte[] bytes = "mundo".getBytes();
		CcpStringDecorator d = new CcpStringDecorator(bytes);
		assertEquals("mundo", d.content);
	}

	@Test
	public void construtorByteArrayWrapperTest() {
		byte[] primitivos = "java".getBytes();
		Byte[] wrappers = new Byte[primitivos.length];
		for (int i = 0; i < primitivos.length; i++) wrappers[i] = primitivos[i];
		CcpStringDecorator d = new CcpStringDecorator(wrappers);
		assertEquals("java", d.content);
	}

	@Test
	public void construtorInputStreamTest() {
		InputStream is = new ByteArrayInputStream("stream".getBytes());
		CcpStringDecorator d = new CcpStringDecorator(is);
		assertEquals("stream", d.content);
	}

	// ── factories ─────────────────────────────────────────────────────────────

	@Test
	public void emailFactoryTest() {
		CcpEmailDecorator email = new CcpStringDecorator("teste@teste.com").email();
		assertNotNull(email);
		assertTrue(email instanceof CcpEmailDecorator);
	}

	@Test
	public void hashFactoryTest() {
		CcpHashDecorator hash = new CcpStringDecorator("texto").hash();
		assertNotNull(hash);
		assertEquals("texto", hash.content);
	}

	@Test
	public void numberFactoryTest() {
		CcpNumberDecorator n = new CcpStringDecorator("3.14").number();
		assertNotNull(n);
		assertTrue(n.equalsTo(3.14d));
	}

	@Test
	public void textFactoryTest() {
		CcpTextDecorator t = new CcpStringDecorator("texto").text();
		assertNotNull(t);
		assertEquals("texto", t.content);
	}

	@Test
	public void urlFactoryTest() {
		CcpUrlDecorator url = new CcpStringDecorator("a b").url();
		assertNotNull(url);
	}

	@Test
	public void passwordFactoryTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("Abc@1234").password();
		assertNotNull(p);
		assertTrue(p.isStrong());
	}

	@Test
	public void jsonFactoryTest() {
		CcpJsonRepresentation json = new CcpStringDecorator("{'nome':'Alice'}").json();
		assertNotNull(json);
		assertFalse(json.isEmpty());
	}

	// ── verificações de tipo ──────────────────────────────────────────────────

	@Test
	public void isInnerJsonTrueTest() {
		assertTrue(new CcpStringDecorator("{'a':1}").isInnerJson());
	}

	@Test
	public void isInnerJsonFalseTest() {
		assertFalse(new CcpStringDecorator("nao sou json").isInnerJson());
	}

	@Test
	public void isListTrueTest() {
		assertTrue(new CcpStringDecorator("[1,2,3]").isList());
	}

	@Test
	public void isListFalseTest() {
		assertFalse(new CcpStringDecorator("texto simples").isList());
	}

	@Test
	public void isLongNumberTrueTest() {
		assertTrue(new CcpStringDecorator("42").isLongNumber());
	}

	@Test
	public void isLongNumberFalseTest() {
		assertFalse(new CcpStringDecorator("nao-e-numero").isLongNumber());
	}

	@Test
	public void isDoubleNumberTrueTest() {
		assertTrue(new CcpStringDecorator("3.14").isDoubleNumber());
	}

	@Test
	public void isDoubleNumberFalseTest() {
		assertFalse(new CcpStringDecorator("abc").isDoubleNumber());
	}

	@Test
	public void isBooleanTrueTest() {
		assertTrue(new CcpStringDecorator("true").isBoolean());
		assertTrue(new CcpStringDecorator("false").isBoolean());
		assertTrue(new CcpStringDecorator("TRUE").isBoolean());
	}

	@Test
	public void isBooleanFalseTest() {
		assertFalse(new CcpStringDecorator("sim").isBoolean());
		assertFalse(new CcpStringDecorator("1").isBoolean());
	}

	// ── getContent / toString / getBytes ──────────────────────────────────────

	@Test
	public void getContentTest() {
		assertEquals("valor", new CcpStringDecorator("valor").getContent());
	}

	@Test
	public void toStringTest() {
		assertEquals("texto", new CcpStringDecorator("texto").toString());
	}

	@Test
	public void getBytesTest() {
		Byte[] bytes = new CcpStringDecorator("abc").getBytes();
		assertEquals(3, bytes.length);
	}
}
