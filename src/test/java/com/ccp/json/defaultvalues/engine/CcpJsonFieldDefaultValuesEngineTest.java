package com.ccp.json.defaultvalues.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.defaultvalues.annotations.RulesDefaultValueStrings;

/**
 * Verifies the engine that applies {@code @CcpJsonFieldDefaultValue}, called directly and not through
 * {@code CcpBusiness.execute}.
 */
public class CcpJsonFieldDefaultValuesEngineTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void singleInstanceTest() {
		assertNotNull(CcpJsonFieldDefaultValuesEngine.INSTANCE);
	}

	/**
	 * A class without any annotated field has nothing to fill, so the received JSON itself is returned, without an
	 * intermediate copy.
	 */
	@Test
	public void classWithoutAnnotatedFieldsReturnsTheSameJsonTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation returned = CcpJsonFieldDefaultValuesEngine.INSTANCE.putDefaultValues(Object.class, json);
		assertSame(json, returned);
	}

	@Test
	public void appliesDefaultValueWithoutGoingThroughExecuteTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesDefaultValueStrings.name, "onias");
		CcpJsonRepresentation returned = CcpJsonFieldDefaultValuesEngine.INSTANCE.putDefaultValues(RulesDefaultValueStrings.class, json);
		String value = returned.getAsString(RulesDefaultValueStrings.comTemplate);
		assertEquals("ola onias", value);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void putDefaultValuesNullClassTest() {
		CcpJsonFieldDefaultValuesEngine.INSTANCE.putDefaultValues(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putDefaultValuesNullJsonTest() {
		CcpJsonFieldDefaultValuesEngine.INSTANCE.putDefaultValues(Object.class, null);
	}
}
