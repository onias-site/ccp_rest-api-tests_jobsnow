package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.ccp.aop.CcpNullReturnException;

public class CcpPasswordDecoratorTest {

	@Test
	public void strongPasswordAllRequirementsTest() {
		// uppercase + lowercase + number + special + 8-20 chars
		CcpPasswordDecorator p = new CcpStringDecorator("Abc@1234").password();
		assertTrue(p.isStrong());
	}

	@Test
	public void withoutNumberIsNotStrongTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("Abcdefg@").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void withoutUppercaseIsNotStrongTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("abc@1234").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void withoutLowercaseIsNotStrongTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("ABC@1234").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void withoutSpecialCharacterIsNotStrongTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("Abcd1234").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void tooShortIsNotStrongTest() {
		// fewer than 8 characters
		CcpPasswordDecorator p = new CcpStringDecorator("Ab@1").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void tooLongIsNotStrongTest() {
		// more than 20 characters
		CcpPasswordDecorator p = new CcpStringDecorator("Abc@1234567890123456789").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void toStringTest() {
		String passwordText = "Abc@1234";
		CcpPasswordDecorator p = new CcpStringDecorator(passwordText).password();
		assertEquals(p.toString(), passwordText);
	}

	@Test
	public void getContentTest() {
		String passwordText = "Xyz@9876";
		CcpPasswordDecorator p = new CcpStringDecorator(passwordText).password();
		assertEquals(p.getContent(), passwordText);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────
	// Note: protected constructor and every public method without parameters or with primitives.

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpPasswordDecorator withNullContent() throws Exception {
		CcpPasswordDecorator d = new CcpStringDecorator("Abc@1234").password();
		Field f = CcpPasswordDecorator.class.getDeclaredField("content");
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
