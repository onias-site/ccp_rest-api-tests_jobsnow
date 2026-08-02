package com.jb.business.bots.login.token;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JbBotSolveLoginTokenTicketTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void construtorTest() {
		assertNotNull(new JbBotSolveLoginTokenTicket());
	}
 
	@Test
	public void getJsonValidationClassTest() {
		assertNotNull(new JbBotSolveLoginTokenTicket().getJsonValidationClass());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new JbBotSolveLoginTokenTicket().apply(null);
	}
}
