package com.ccp.implementations.main.authentication.gcp.oauth;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.main.authentication.CcpAuthenticationProvider;

public class CcpGcpMainAuthenticationTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGcpMainAuthentication.INSTANCE);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void constructorProviderTest() {
		assertNotNull(CcpGcpMainAuthentication.INSTANCE);
	}

	@Test
	public void getInstanceTest() {
		CcpAuthenticationProvider instance = CcpGcpMainAuthentication.INSTANCE.getInstance();
		assertNotNull(instance);
	}

	// getJwtToken() has no parameters → there is no null-parameter test;
	// it depends on external credentials (GOOGLE_APPLICATION_CREDENTIALS) → null-return is not
	// testable without a configured environment.
}
