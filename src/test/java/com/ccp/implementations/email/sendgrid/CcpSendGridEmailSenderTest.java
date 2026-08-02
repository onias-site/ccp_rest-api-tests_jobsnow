package com.ccp.implementations.email.sendgrid;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpSendGridEmailSenderTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpSendGridEmailSender());
	}

	private static CcpEmailSender getSender() {
		return CcpDependencyInjection.getDependency(CcpEmailSender.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpSendGridEmailSender());
	}

	@Test
	public void getInstanceTest() {
		CcpEmailSender instance = new CcpSendGridEmailSender().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) — SendGridEmailSender via interface ────────

	@Test(expected = CcpNullParameterException.class)
	public void sendSimpleTextEmailMessageTokenNullTest() {
		getSender().sendSimpleTextEmailMessage(null, "url", "tid", "from@x.com", "subj", "msg",
				CcpHttpContentType.TEXT_PLAIN, "to@x.com");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendSimpleTextEmailMessageUrlNullTest() {
		getSender().sendSimpleTextEmailMessage("tok", null, "tid", "from@x.com", "subj", "msg",
				CcpHttpContentType.TEXT_PLAIN, "to@x.com");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendSimpleTextEmailMessageTemplateNullTest() {
		getSender().sendSimpleTextEmailMessage("tok", "url", null, "from@x.com", "subj", "msg",
				CcpHttpContentType.TEXT_PLAIN, "to@x.com");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendSimpleTextEmailMessageSenderNullTest() {
		getSender().sendSimpleTextEmailMessage("tok", "url", "tid", null, "subj", "msg",
				CcpHttpContentType.TEXT_PLAIN, "to@x.com");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendSimpleTextEmailMessageSubjectNullTest() {
		getSender().sendSimpleTextEmailMessage("tok", "url", "tid", "from@x.com", null, "msg",
				CcpHttpContentType.TEXT_PLAIN, "to@x.com");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendSimpleTextEmailMessageMessageNullTest() {
		getSender().sendSimpleTextEmailMessage("tok", "url", "tid", "from@x.com", "subj", null,
				CcpHttpContentType.TEXT_PLAIN, "to@x.com");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendSimpleTextEmailMessageContentTypeNullTest() {
		getSender().sendSimpleTextEmailMessage("tok", "url", "tid", "from@x.com", "subj", "msg",
				null, "to@x.com");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendSimpleTextEmailMessageEmailsNullTest() {
		getSender().sendSimpleTextEmailMessage("tok", "url", "tid", "from@x.com", "subj", "msg",
				CcpHttpContentType.TEXT_PLAIN, (String[]) null);
	}
}
