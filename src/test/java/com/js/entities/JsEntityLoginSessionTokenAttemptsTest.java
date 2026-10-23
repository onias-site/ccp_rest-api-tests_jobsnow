package com.js.entities;

import com.jn.entities.NoopBusiness;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class JsEntityLoginSessionTokenAttemptsTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}



	@Test
	public void incrementAttemptsReturnsNonNullTest() {
		CcpBusiness r = JsEntityLoginSessionTokenAttempts.incrementAttempts(3, NoopBusiness.INSTANCE);
		assertNotNull(r);
	}

	@Test
	public void resetAttemptsReturnsNonNullTest() {
		CcpBusiness r = JsEntityLoginSessionTokenAttempts.resetAttempts();
		assertNotNull(r);
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementAttemptsMaxNullTest() {
		JsEntityLoginSessionTokenAttempts.incrementAttempts(null, NoopBusiness.INSTANCE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void incrementAttemptsWhenExceedNullTest() {
		JsEntityLoginSessionTokenAttempts.incrementAttempts(3, null);
	}
}
