package com.ccp.rest.api.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CcpRestApiUtilsTest {

	@Test
	public void construtorTest() {
		assertNotNull(new CcpRestApiUtils());
	}

	// isLocalEnvironment() é estático e sem parâmetros — sem null-parameter test.
	// Depende de application_properties externo — sem null-return test aplicável.
}
