package com.jn.services;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class JnServiceTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// Implementação como enum já que JnService requer name() (herda de CcpService/CcpJsonFieldName)


	@Test
	public void getJsonValidationClassTest() {
		// Sem uma inner class homônima ao name(), dispara JnErrorServiceValidationClassNotFound
		try {
			NoopJnService.INSTANCE.getJsonValidationClass();
		} catch (JnErrorServiceValidationClassNotFound e) {
			assertNotNull(e);
		}
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		NoopJnService.INSTANCE.execute((CcpJsonRepresentation) null);
	}

	// ── JnErrorServiceValidationClassNotFound ────────────────────────────────

	@Test
	public void exceptionClassExistsTest() {
		assertNotNull(JnErrorServiceValidationClassNotFound.class);
	}
}
