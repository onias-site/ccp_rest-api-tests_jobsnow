package com.jn.entities.fields.transformers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnJsonTransformersFieldEntityMessageHashTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void constructorTest() {
		assertNotNull(JnJsonTransformersFieldEntityMessageHash.INSTANCE);
	}

	@Test
	public void canBePrimaryKeyTest() {
		assertTrue(JnJsonTransformersFieldEntityMessageHash.INSTANCE.canBePrimaryKey());
	}

	@Test
	public void nameTest() {
		assertNotNull(JnJsonTransformersFieldEntityMessageHash.INSTANCE.name());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnJsonTransformersFieldEntityMessageHash.INSTANCE.execute(null);
	}
}
