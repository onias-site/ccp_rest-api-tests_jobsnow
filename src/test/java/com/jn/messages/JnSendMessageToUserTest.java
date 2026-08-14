package com.jn.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.business.http.JnBusinessSendHttpRequest;

public class JnSendMessageToUserTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static class NoopBusiness extends JnBusinessSendHttpRequest {
		NoopBusiness() {
			super(json -> json);
		}
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

	@Test
	public void construtorJustErrorsTest() {
		assertNotNull(new JnSendMessageToUser.JnSendMessageAndJustErrors());
	}

	@Test
	public void construtorIgnoringErrorsTest() {
		assertNotNull(new JnSendMessageToUser.JnSendMessageIgnoringProcessErrors());
	}

	// ── null-parameter tests — JnSendMessageAndJustErrors.addOneStep ─────────

	@Test(expected = CcpNullParameterException.class)
	public void justErrorsAddOneStepStepNullTest() {
		new JnSendMessageToUser.JnSendMessageAndJustErrors()
				.addOneStep(null, null, null, null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ignoringErrorsAddOneStepStepNullTest() {
		new JnSendMessageToUser.JnSendMessageIgnoringProcessErrors()
				.addOneStep(null, null, null, null, null);
	}

	// ── null-parameter tests — JnCreateStep ──────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void createStepWithTheProcessNullTest() {
		new JnSendMessageToUser().createStep().withTheProcess(null);
	}

	// ── null-parameter tests — JnWithTheProcess (construtor público) ─────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorWithTheProcessCreateStepNullTest() {
		new JnSendMessageToUser.JnWithTheProcess(null, new NoopBusiness());
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorWithTheProcessBusinessNullTest() {
		JnSendMessageToUser.JnCreateStep step = new JnSendMessageToUser().createStep();
		new JnSendMessageToUser.JnWithTheProcess(step, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void withTheProcessAndWithParametersEntityNullTest() {
		JnSendMessageToUser.JnCreateStep step = new JnSendMessageToUser().createStep();
		JnSendMessageToUser.JnWithTheProcess w = new JnSendMessageToUser.JnWithTheProcess(step, new NoopBusiness());
		w.andWithTheParametersEntity(null);
	}
}
