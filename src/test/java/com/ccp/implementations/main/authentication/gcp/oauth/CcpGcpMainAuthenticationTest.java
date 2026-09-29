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
	public void constructorProviderTest() {
		assertNotNull(new CcpGcpMainAuthentication());
	}

	@Test
	public void getInstanceTest() {
		CcpAuthenticationProvider instance = new CcpGcpMainAuthentication().getInstance();
		assertNotNull(instance);
	}

	// getJwtToken() has no parameters → there is no null-parameter test;
	// it depends on external credentials (GOOGLE_APPLICATION_CREDENTIALS) → null-return is not
	// testable without a configured environment.
}
