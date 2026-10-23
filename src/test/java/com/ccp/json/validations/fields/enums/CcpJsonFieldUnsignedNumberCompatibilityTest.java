package com.ccp.json.validations.fields.enums;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/** {@link CcpJsonFieldUnsignedNumberCompatibility} accepts only an integer equal to or greater than zero. */
public class CcpJsonFieldUnsignedNumberCompatibilityTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static boolean isCompatible(Object value) {
		CcpJsonFieldUnsignedNumberCompatibility compatibility = new CcpJsonFieldUnsignedNumberCompatibility("amount");
		return compatibility.test(CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("amount"), value));
	}

	@Test
	public void zeroAndPositiveIntegersAreCompatible() {
		assertTrue(isCompatible(0));
		assertTrue(isCompatible(15L));
		assertTrue(isCompatible("8"));
	}

	@Test
	public void aNegativeIntegerIsNotCompatible() {
		assertFalse(isCompatible(-1));
	}

	@Test
	public void aTextOrADecimalIsNotCompatible() {
		assertFalse(isCompatible("abc"));
		assertFalse(isCompatible(2.5));
	}
}
