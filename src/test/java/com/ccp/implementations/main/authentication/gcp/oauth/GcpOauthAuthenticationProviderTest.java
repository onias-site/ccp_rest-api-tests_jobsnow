package com.ccp.implementations.main.authentication.gcp.oauth;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class GcpOauthAuthenticationProviderTest {

	@Test
	public void construtorTest() {
		assertNotNull(new GcpOauthAuthenticationProvider());
	}

	// getJwtToken() não tem parâmetros nulos possíveis e depende de credenciais externas;
	// AOP-null-parameter e AOP-null-return não se aplicam aqui.
}
