package com.ccp.implementations.main.authentication.gcp.oauth;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.main.authentication.CcpAuthenticationProvider;

public class CcpGcpMainAuthenticationTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGcpMainAuthentication());
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpGcpMainAuthentication());
	}

	@Test
	public void getInstanceTest() {
		CcpAuthenticationProvider instance = new CcpGcpMainAuthentication().getInstance();
		assertNotNull(instance);
	}

	// getJwtToken() não tem parâmetros → não há teste null-parameter;
	// depende de credenciais externas (GOOGLE_APPLICATION_CREDENTIALS) → null-return não é
	// testável sem ambiente configurado.
}
