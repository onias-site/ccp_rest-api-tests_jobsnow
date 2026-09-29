package com.ccp.rest.api.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CcpRestApiUtilsTest {

	@Test
	public void constructorTest() {
		assertNotNull(new CcpRestApiUtils());
	}

	// isLocalEnvironment() is static and has no parameters — no null-parameter test.
	// It depends on an external application_properties — no applicable null-return test.
}
