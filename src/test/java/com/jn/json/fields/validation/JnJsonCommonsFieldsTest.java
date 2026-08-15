package com.jn.json.fields.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class JnJsonCommonsFieldsTest {

	// ── enum values ──────────────────────────────────────────────────────────

	@Test
	public void valuesTest() {
		JnJsonCommonsFields[] values = JnJsonCommonsFields.values();
		assertNotNull(values);
		assertEquals(34, values.length);
	}

	@Test
	public void valueOfRequestTest() {
		assertNotNull(JnJsonCommonsFields.valueOf("request"));
	}

	@Test
	public void valueOfPasswordTest() {
		assertNotNull(JnJsonCommonsFields.valueOf("password"));
	}

	@Test
	public void valueOfEmailTest() {
		assertNotNull(JnJsonCommonsFields.valueOf("email"));
	}

	// ── constantes explícitas ────────────────────────────────────────────────

	@Test
	public void allConstantsAccessibleTest() {
		assertNotNull(JnJsonCommonsFields.request);
		assertNotNull(JnJsonCommonsFields.password);
		assertNotNull(JnJsonCommonsFields.description);
		assertNotNull(JnJsonCommonsFields.explanation);
		assertNotNull(JnJsonCommonsFields.operation);
		assertNotNull(JnJsonCommonsFields.response);
		assertNotNull(JnJsonCommonsFields.timestamp);
		assertNotNull(JnJsonCommonsFields.date);
		assertNotNull(JnJsonCommonsFields.entity);
		assertNotNull(JnJsonCommonsFields.id);
		assertNotNull(JnJsonCommonsFields.json);
		assertNotNull(JnJsonCommonsFields.subjectType);
		assertNotNull(JnJsonCommonsFields.email);
		assertNotNull(JnJsonCommonsFields.subject);
		assertNotNull(JnJsonCommonsFields.message);
		assertNotNull(JnJsonCommonsFields.sender);
		assertNotNull(JnJsonCommonsFields.moreParameters);
		assertNotNull(JnJsonCommonsFields.templateId);
		assertNotNull(JnJsonCommonsFields.language);
		assertNotNull(JnJsonCommonsFields.url);
		assertNotNull(JnJsonCommonsFields.method);
		assertNotNull(JnJsonCommonsFields.headers);
		assertNotNull(JnJsonCommonsFields.apiName);
		assertNotNull(JnJsonCommonsFields.details);
		assertNotNull(JnJsonCommonsFields.status);
		assertNotNull(JnJsonCommonsFields.cause);
		assertNotNull(JnJsonCommonsFields.stackTrace);
		assertNotNull(JnJsonCommonsFields.attempts);
		assertNotNull(JnJsonCommonsFields.ip);
		assertNotNull(JnJsonCommonsFields.coordinates);
		assertNotNull(JnJsonCommonsFields.macAddress);
		assertNotNull(JnJsonCommonsFields.userAgent);
		assertNotNull(JnJsonCommonsFields.httpStatus);
		assertNotNull(JnJsonCommonsFields.contentType);
	}

	// ── name (herdado de Enum) ───────────────────────────────────────────────

	@Test
	public void nameTest() {
		assertEquals("email", JnJsonCommonsFields.email.name());
		assertEquals("password", JnJsonCommonsFields.password.name());
	}

	@Test
	public void ordinalTest() {
		assertEquals(0, JnJsonCommonsFields.request.ordinal());
	}

	// ── @CcpJsonFieldTypeString presente via reflexão ────────────────────────

	@Test
	public void emailFieldHasTypeStringAnnotationTest() throws Exception {
		java.lang.reflect.Field f = JnJsonCommonsFields.class.getField("email");
		assertNotNull(f.getAnnotation(com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString.class));
	}

	@Test
	public void timestampFieldHasNumberUnsignedAnnotationTest() throws Exception {
		java.lang.reflect.Field f = JnJsonCommonsFields.class.getField("timestamp");
		assertNotNull(f.getAnnotation(com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned.class));
	}
}
