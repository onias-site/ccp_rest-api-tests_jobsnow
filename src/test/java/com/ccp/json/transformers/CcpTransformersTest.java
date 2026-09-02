package com.ccp.json.transformers;

import static org.junit.Assert.assertEquals;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class CcpTransformersTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}



	@Test
	public void substringTest() {
		CcpJsonRepresentation j = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("k"), "0123456789");
		CcpJsonRepresentation r = new Impl().substring(j, "k", 3);
		assertEquals("012", r.getAsString(new CcpFieldName("k")));
	}

	@Test
	public void putMinValueTest() {
		CcpJsonRepresentation j = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("k"), 1);
		CcpJsonRepresentation r = new Impl().putMinValue(j, "k", 5);
		assertEquals(Double.valueOf(5), r.getAsDoubleNumber(new CcpFieldName("k")));
	}

	@Test
	public void addLongValueTest() {
		CcpJsonRepresentation j = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("k"), "abc");
		CcpJsonRepresentation r = new Impl().addLongValue(j, "k", 42L);
		assertEquals(Long.valueOf(42), r.getAsLongNumber(new CcpFieldName("k")));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void substringJsonNullTest() {
		new Impl().substring(null, "k", 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void substringFieldNullTest() {
		new Impl().substring(CcpOtherConstants.EMPTY_JSON, null, 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putMinValueJsonNullTest() {
		new Impl().putMinValue(null, "k", 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putMinValueFieldNullTest() {
		new Impl().putMinValue(CcpOtherConstants.EMPTY_JSON, null, 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addLongValueJsonNullTest() {
		new Impl().addLongValue(null, "k", 1L);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addLongValueFieldNullTest() {
		new Impl().addLongValue(CcpOtherConstants.EMPTY_JSON, null, 1L);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addLongValueLongNullTest() {
		new Impl().addLongValue(CcpOtherConstants.EMPTY_JSON, "k", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addRequiredAtLeastOneJsonNullTest() {
		new Impl().addRequiredAtLeastOne(null, "k", "v", "f1");
	}

	@Test(expected = CcpNullParameterException.class)
	public void addRequiredAtLeastOneFieldNullTest() {
		new Impl().addRequiredAtLeastOne(CcpOtherConstants.EMPTY_JSON, null, "v", "f1");
	}

	@Test(expected = CcpNullParameterException.class)
	public void addRequiredAtLeastOneValueNullTest() {
		new Impl().addRequiredAtLeastOne(CcpOtherConstants.EMPTY_JSON, "k", null, "f1");
	}

	@Test(expected = CcpNullParameterException.class)
	public void addRequiredAtLeastOneFieldsNullTest() {
		new Impl().addRequiredAtLeastOne(CcpOtherConstants.EMPTY_JSON, "k", "v", (String[]) null);
	}
}
