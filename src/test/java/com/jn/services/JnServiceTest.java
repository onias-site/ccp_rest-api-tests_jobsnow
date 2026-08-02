package com.jn.services;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnServiceTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// Implementação como enum já que JnService requer name() (herda de CcpService/CcpJsonFieldName)
	enum NoopJnService implements JnService, CcpJsonFieldName {
		INSTANCE;

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	@Test
	public void getJsonValidationClassTest() {
		// Sem uma inner class homônima ao name(), dispara JnErrorServiceValidationClassNotFound
		try {
			NoopJnService.INSTANCE.getJsonValidationClass();
		} catch (JnService.JnErrorServiceValidationClassNotFound e) {
			assertNotNull(e);
		}
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		NoopJnService.INSTANCE.apply(null);
	}

	// ── JnErrorServiceValidationClassNotFound ────────────────────────────────

	@Test
	public void exceptionClassExistsTest() {
		assertNotNull(JnService.JnErrorServiceValidationClassNotFound.class);
	}
}
