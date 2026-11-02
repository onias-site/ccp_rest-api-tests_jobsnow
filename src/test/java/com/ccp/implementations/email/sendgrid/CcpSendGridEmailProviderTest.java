package com.ccp.implementations.email.sendgrid;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpSendGridEmailProviderTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpSendGridEmailProvider.INSTANCE);
	}

	private static CcpEmailProvider getSender() {
		return CcpDependencyInjection.getDependency(CcpEmailProvider.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void constructorProviderTest() {
		assertNotNull(CcpSendGridEmailProvider.INSTANCE);
	}

	@Test
	public void getInstanceTest() {
		CcpEmailProvider instance = CcpSendGridEmailProvider.INSTANCE.getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) — SendGridEmailProvider via interface ────────

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
