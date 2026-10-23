package com.jn.entities.fields.transformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnJsonTransformersFieldsEntityDoNothingTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void constructorTest() {
		assertNotNull(JnJsonTransformersFieldsEntityDoNothing.INSTANCE);
	}

	@Test
	public void applyReturnsSameJsonTest() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation r = JnJsonTransformersFieldsEntityDoNothing.INSTANCE.execute(input);
		assertSame(input, r);
	}

	@Test
	public void canBePrimaryKeyTest() {
		assertTrue(JnJsonTransformersFieldsEntityDoNothing.INSTANCE.canBePrimaryKey());
	}

	@Test
	public void nameTest() {
		assertEquals("doNothing", JnJsonTransformersFieldsEntityDoNothing.INSTANCE.name());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnJsonTransformersFieldsEntityDoNothing.INSTANCE.execute(null);
	}
}
