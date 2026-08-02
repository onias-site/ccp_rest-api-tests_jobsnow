package com.ccp.especifications.http;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpErrorHttpTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void clientCtorTest() {
		assertNotNull(new CcpErrorHttpClient(CcpOtherConstants.EMPTY_JSON));
	}

	@Test
	public void serverCtorTest() {
		assertNotNull(new CcpErrorHttpServer(CcpOtherConstants.EMPTY_JSON));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void clientCtorNullTest() {
		new CcpErrorHttpClient(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void serverCtorNullTest() {
		new CcpErrorHttpServer(null);
	}
}
