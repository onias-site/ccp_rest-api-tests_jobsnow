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
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}



	@Test
	public void substringTest() {
		CcpJsonRepresentation j = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("k"), "0123456789");
		CcpJsonRepresentation r = Impl.INSTANCE.substring(j, "k", 3);
		assertEquals("012", r.getAsString(new CcpFieldName("k")));
	}

	@Test
	public void addRequiredAtLeastOneSetsTheFieldOnlyWhenNoneOfTheOthersIsPresent() {
		CcpJsonRepresentation without = Impl.INSTANCE.addRequiredAtLeastOne(CcpOtherConstants.EMPTY_JSON, "clt", 1000, "clt", "pj");
		assertEquals(Integer.valueOf(1000), without.getAsIntegerNumber(new CcpFieldName("clt")));

		CcpJsonRepresentation withOne = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("pj"), 5);
		assertEquals(withOne, Impl.INSTANCE.addRequiredAtLeastOne(withOne, "clt", 1000, "clt", "pj"));
	}

	@Test
	public void putMinValueTest() {
		CcpJsonRepresentation j = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("k"), 1);
		CcpJsonRepresentation r = Impl.INSTANCE.putMinValue(j, "k", 5);
		assertEquals(Double.valueOf(5), r.getAsDoubleNumber(new CcpFieldName("k")));
	}

	@Test
	public void addLongValueTest() {
		CcpJsonRepresentation j = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("k"), "abc");
		CcpJsonRepresentation r = Impl.INSTANCE.addLongValue(j, "k", 42L);
		assertEquals(Long.valueOf(42), r.getAsLongNumber(new CcpFieldName("k")));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void substringJsonNullTest() {
		Impl.INSTANCE.substring(null, "k", 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void substringFieldNullTest() {
		Impl.INSTANCE.substring(CcpOtherConstants.EMPTY_JSON, null, 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putMinValueJsonNullTest() {
		Impl.INSTANCE.putMinValue(null, "k", 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putMinValueFieldNullTest() {
		Impl.INSTANCE.putMinValue(CcpOtherConstants.EMPTY_JSON, null, 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addLongValueJsonNullTest() {
		Impl.INSTANCE.addLongValue(null, "k", 1L);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addLongValueFieldNullTest() {
		Impl.INSTANCE.addLongValue(CcpOtherConstants.EMPTY_JSON, null, 1L);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addLongValueLongNullTest() {
		Impl.INSTANCE.addLongValue(CcpOtherConstants.EMPTY_JSON, "k", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addRequiredAtLeastOneJsonNullTest() {
		Impl.INSTANCE.addRequiredAtLeastOne(null, "k", "v", "f1");
	}

	@Test(expected = CcpNullParameterException.class)
	public void addRequiredAtLeastOneFieldNullTest() {
		Impl.INSTANCE.addRequiredAtLeastOne(CcpOtherConstants.EMPTY_JSON, null, "v", "f1");
	}

	@Test(expected = CcpNullParameterException.class)
	public void addRequiredAtLeastOneValueNullTest() {
		Impl.INSTANCE.addRequiredAtLeastOne(CcpOtherConstants.EMPTY_JSON, "k", null, "f1");
	}

	@Test(expected = CcpNullParameterException.class)
	public void addRequiredAtLeastOneFieldsNullTest() {
		Impl.INSTANCE.addRequiredAtLeastOne(CcpOtherConstants.EMPTY_JSON, "k", "v", (String[]) null);
	}
}
