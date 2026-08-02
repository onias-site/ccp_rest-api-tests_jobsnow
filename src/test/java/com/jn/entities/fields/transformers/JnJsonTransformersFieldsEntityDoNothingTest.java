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
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void construtorTest() {
		assertNotNull(new JnJsonTransformersFieldsEntityDoNothing());
	}

	@Test
	public void applyReturnsSameJsonTest() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation r = new JnJsonTransformersFieldsEntityDoNothing().apply(input);
		assertSame(input, r);
	}

	@Test
	public void canBePrimaryKeyTest() {
		assertTrue(new JnJsonTransformersFieldsEntityDoNothing().canBePrimaryKey());
	}

	@Test
	public void nameTest() {
		assertEquals("doNothing", new JnJsonTransformersFieldsEntityDoNothing().name());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new JnJsonTransformersFieldsEntityDoNothing().apply(null);
	}
}
