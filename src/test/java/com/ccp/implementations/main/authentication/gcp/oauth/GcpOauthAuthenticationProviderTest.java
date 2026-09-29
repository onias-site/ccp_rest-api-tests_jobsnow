package com.ccp.implementations.main.authentication.gcp.oauth;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class GcpOauthAuthenticationProviderTest {

	@Test
	public void constructorTest() {
		assertNotNull(new GcpOauthAuthenticationProvider());
	}

	// getJwtToken() has no parameters that could be null and depends on external credentials;
	// AOP-null-parameter and AOP-null-return do not apply here.
}
