package com.ccp.jn.cron.controller;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpCronTasksControllerTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static class NoopBusiness implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	// ── construtor ────────────────────────────────────────────────────────────

	@Test
	public void construtorTest() {
		new CcpCronTasksController();
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void mainNotifyErrorNullTest() throws Exception {
		CcpCronTasksController.main(null, "topic", "{}");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mainTopicNullTest() throws Exception {
		CcpCronTasksController.main(new NoopBusiness(), null, "{}");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mainParametersNullTest() throws Exception {
		CcpCronTasksController.main(new NoopBusiness(), "topic", null);
	}
}
