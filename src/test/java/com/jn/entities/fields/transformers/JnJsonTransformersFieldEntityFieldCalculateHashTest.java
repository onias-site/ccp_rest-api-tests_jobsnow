package com.jn.entities.fields.transformers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnJsonTransformersFieldEntityFieldCalculateHashTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void construtorTokenHashTest() {
		assertNotNull(new JnJsonTransformersFieldEntityFieldCalculateHash.JnJsonTransformersFieldEntityTokenHash());
	}

	@Test
	public void canBePrimaryKeyTest() {
		assertTrue(new JnJsonTransformersFieldEntityFieldCalculateHash.JnJsonTransformersFieldEntityTokenHash().canBePrimaryKey());
	}

	@Test
	public void nameTest() {
		assertNotNull(new JnJsonTransformersFieldEntityFieldCalculateHash.JnJsonTransformersFieldEntityTokenHash().name());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new JnJsonTransformersFieldEntityFieldCalculateHash.JnJsonTransformersFieldEntityTokenHash().apply(null);
	}
}
