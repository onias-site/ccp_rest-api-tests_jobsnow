package com.js.entities.fields.transformers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JsJsonTransformersFieldEntityTokenHashTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void constructorTokenHashTest() {
		assertNotNull(new JsJsonTransformersFieldEntityTokenHash());
	}

	@Test
	public void canBePrimaryKeyTest() {
		assertTrue(new JsJsonTransformersFieldEntityTokenHash().canBePrimaryKey());
	}

	@Test
	public void nameTest() {
		assertNotNull(new JsJsonTransformersFieldEntityTokenHash().name());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new JsJsonTransformersFieldEntityTokenHash().execute(null);
	}
}
