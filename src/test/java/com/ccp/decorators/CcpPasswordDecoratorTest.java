package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CcpPasswordDecoratorTest {

	@Test
	public void senhaForteTodosOsRequisitosTest() {
		// maiúscula + minúscula + número + especial + 8-20 chars
		CcpPasswordDecorator p = new CcpStringDecorator("Abc@1234").password();
		assertTrue(p.isStrong());
	}

	@Test
	public void semNumeroNaoEhForteTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("Abcdefg@").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void semMaiusculaNaoEhForteTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("abc@1234").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void semMinusculaNaoEhForteTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("ABC@1234").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void semCaractereEspecialNaoEhForteTest() {
		CcpPasswordDecorator p = new CcpStringDecorator("Abcd1234").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void muitoCurtaNaoEhForteTest() {
		// menos de 8 caracteres
		CcpPasswordDecorator p = new CcpStringDecorator("Ab@1").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void muitoLongaNaoEhForteTest() {
		// mais de 20 caracteres
		CcpPasswordDecorator p = new CcpStringDecorator("Abc@1234567890123456789").password();
		assertFalse(p.isStrong());
	}

	@Test
	public void toStringTest() {
		String senha = "Abc@1234";
		CcpPasswordDecorator p = new CcpStringDecorator(senha).password();
		assertEquals(p.toString(), senha);
	}

	@Test
	public void getContentTest() {
		String senha = "Xyz@9876";
		CcpPasswordDecorator p = new CcpStringDecorator(senha).password();
		assertEquals(p.getContent(), senha);
	}
}
