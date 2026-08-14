package com.jn.entities.fields.transformers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnJsonTransformersFieldEntityMessageHashTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void construtorTest() {
		assertNotNull(new JnJsonTransformersFieldEntityMessageHash());
	}

	@Test
	public void canBePrimaryKeyTest() {
		assertTrue(new JnJsonTransformersFieldEntityMessageHash().canBePrimaryKey());
	}

	@Test
	public void nameTest() {
		assertNotNull(new JnJsonTransformersFieldEntityMessageHash().name());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new JnJsonTransformersFieldEntityMessageHash().execute(null);
	}
}
