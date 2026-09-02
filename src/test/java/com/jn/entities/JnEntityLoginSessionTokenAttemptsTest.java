package com.jn.entities;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class JnEntityLoginSessionTokenAttemptsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
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
