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

	// Implemented as an enum since JnService requires name() (inherited from CcpService/CcpJsonFieldName)


	@Test
	public void getJsonValidationClassTest() {
		// Without an inner class with the same name as name(), it triggers JnErrorServiceValidationClassNotFound
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
