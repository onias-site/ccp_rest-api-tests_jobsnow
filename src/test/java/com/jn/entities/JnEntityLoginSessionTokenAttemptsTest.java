package com.jn.entities;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnEntityLoginSessionTokenAttemptsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static class NoopBusiness implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	@Test
	public void incrementAttemptsReturnsNonNullTest() {
		CcpBusiness r = JnEntityLoginSessionTokenAttempts.incrementAttempts(3, new NoopBusiness());
		assertNotNull(r);
	}

	@Test
	public void resetAttemptsReturnsNonNullTest() {
		CcpBusiness r = JnEntityLoginSessionTokenAttempts.resetAttempts();
		assertNotNull(r);
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementAttemptsMaxNullTest() {
		JnEntityLoginSessionTokenAttempts.incrementAttempts(null, new NoopBusiness());
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementAttemptsWhenExceedNullTest() {
		JnEntityLoginSessionTokenAttempts.incrementAttempts(3, null);
	}
}
