package com.jn.json.fields.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JnJsonInstantMessengerFieldsTest {

	// ── enum values ──────────────────────────────────────────────────────────

	@Test
	public void valuesTest() {
		JnJsonInstantMessengerFields[] values = JnJsonInstantMessengerFields.values();
		assertNotNull(values);
		assertEquals(12, values.length);
	}

	@Test
	public void valueOfMessageTest() {
		assertNotNull(JnJsonInstantMessengerFields.valueOf("message"));
	}

	@Test
	public void valueOfChatIdTest() {
		assertNotNull(JnJsonInstantMessengerFields.valueOf("chatId"));
	}

	// ── constantes explícitas ────────────────────────────────────────────────

	@Test
	public void allConstantsAccessibleTest() {
		assertNotNull(JnJsonInstantMessengerFields.message);
		assertNotNull(JnJsonInstantMessengerFields.chatId);
		assertNotNull(JnJsonInstantMessengerFields.moreParameters);
		assertNotNull(JnJsonInstantMessengerFields.templateId);
		assertNotNull(JnJsonInstantMessengerFields.caption);
		assertNotNull(JnJsonInstantMessengerFields.contentType);
		assertNotNull(JnJsonInstantMessengerFields.fileName);
		assertNotNull(JnJsonInstantMessengerFields.instantMessageType);
		assertNotNull(JnJsonInstantMessengerFields.commandName);
		assertNotNull(JnJsonInstantMessengerFields.stepName);
		assertNotNull(JnJsonInstantMessengerFields.botToken);
	}

	// ── name / ordinal ───────────────────────────────────────────────────────

	@Test
	public void nameTest() {
		assertEquals("message", JnJsonInstantMessengerFields.message.name());
		assertEquals("chatId", JnJsonInstantMessengerFields.chatId.name());
	}

	// ── validação de anotações via reflexão ──────────────────────────────────

	@Test
	public void messageFieldHasTypeStringAnnotationTest() throws Exception {
		java.lang.reflect.Field f = JnJsonInstantMessengerFields.class.getField("message");
		assertNotNull(f.getAnnotation(com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString.class));
	}

	@Test
	public void chatIdFieldHasNumberAnnotationTest() throws Exception {
		java.lang.reflect.Field f = JnJsonInstantMessengerFields.class.getField("chatId");
		assertNotNull(f.getAnnotation(com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber.class));
	}

	@Test
	public void moreParametersFieldHasNestedJsonAnnotationTest() throws Exception {
		java.lang.reflect.Field f = JnJsonInstantMessengerFields.class.getField("moreParameters");
		assertNotNull(f.getAnnotation(com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson.class));
	}
}
