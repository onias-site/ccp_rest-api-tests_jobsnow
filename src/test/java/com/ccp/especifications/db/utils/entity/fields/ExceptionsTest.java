package com.ccp.especifications.db.utils.entity.fields;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;

public class ExceptionsTest {

	@Test
	public void ctorJsonTransformerErrorTest() {
		assertNotNull(new CcpEntityJsonTransformerError("msg"));
	}

	@Test
	public void ctorIncorrectEntityFieldsTest() {
		assertNotNull(new CcpErrorDbUtilsIncorrectEntityFields("msg"));
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonTransformerErrorNullTest() {
		new CcpEntityJsonTransformerError(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorIncorrectEntityFieldsNullTest() {
		new CcpErrorDbUtilsIncorrectEntityFields(null);
	}
}
