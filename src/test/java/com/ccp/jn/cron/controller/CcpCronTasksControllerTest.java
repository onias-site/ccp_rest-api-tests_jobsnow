package com.ccp.jn.cron.controller;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class CcpCronTasksControllerTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
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
