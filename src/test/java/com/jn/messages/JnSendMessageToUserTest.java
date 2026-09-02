package com.jn.messages;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class JnSendMessageToUserTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}



	// ── construtor + factories principais ────────────────────────────────────

	@Test
	public void construtorTest() {
		assertNotNull(new JnSendMessageToUser());
	}

	@Test
	public void createStepTest() {
		assertNotNull(new JnSendMessageToUser().createStep());
	}

	// ── subclasses públicas ──────────────────────────────────────────────────


	// ── null-parameter tests — JnCreateStep ──────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void createStepWithTheProcessNullTest() {
		new JnSendMessageToUser().createStep().withTheProcess(null);
	}

	// ── null-parameter tests — JnWithTheProcess (construtor público) ─────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorWithTheProcessCreateStepNullTest() {
		new JnWithTheProcess(null, new JnSendMessageToUserTestNoopBusiness());
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorWithTheProcessBusinessNullTest() {
		JnCreateStep step = new JnSendMessageToUser().createStep();
		new JnWithTheProcess(step, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void withTheProcessAndWithParametersEntityNullTest() {
		JnCreateStep step = new JnSendMessageToUser().createStep();
		JnWithTheProcess w = new JnWithTheProcess(step, new JnSendMessageToUserTestNoopBusiness());
		w.andWithTheParametersEntity(null);
	}
}
