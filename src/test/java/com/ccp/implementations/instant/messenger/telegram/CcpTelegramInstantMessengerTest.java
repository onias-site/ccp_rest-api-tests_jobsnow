package com.ccp.implementations.instant.messenger.telegram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpTelegramInstantMessengerTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpTelegramInstantMessenger());
	}

	private static CcpInstantMessenger getMessenger() {
		return CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
	}

	enum FakeBot implements CcpJsonFieldName {
		BOT_A
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void constructorProviderTest() {
		assertNotNull(new CcpTelegramInstantMessenger());
	}

	@Test
	public void getInstanceTest() {
		CcpInstantMessenger instance = new CcpTelegramInstantMessenger().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) — sendTextMessage ──────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void sendTextMessageBotTypeNullTest() {
		getMessenger().sendTextMessage(null, "tok", 1L, 1L, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendTextMessageTokenNullTest() {
		getMessenger().sendTextMessage(FakeBot.BOT_A, null, 1L, 1L, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendTextMessageChatIdNullTest() {
		getMessenger().sendTextMessage(FakeBot.BOT_A, "tok", null, 1L, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendTextMessageReplyToNullTest() {
		getMessenger().sendTextMessage(FakeBot.BOT_A, "tok", 1L, null, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendTextMessageMessageNullTest() {
		getMessenger().sendTextMessage(FakeBot.BOT_A, "tok", 1L, 1L, null);
	}

	// ── null-parameter tests (AOP) — sendFile ─────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void sendFileBotTypeNullTest() {
		getMessenger().sendFile(null, "tok", 1L, 1L, "file", "cap", new Byte[] { 0 });
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFileTokenNullTest() {
		getMessenger().sendFile(FakeBot.BOT_A, null, 1L, 1L, "file", "cap", new Byte[] { 0 });
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFileChatIdNullTest() {
		getMessenger().sendFile(FakeBot.BOT_A, "tok", null, 1L, "file", "cap", new Byte[] { 0 });
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFileReplyToNullTest() {
		getMessenger().sendFile(FakeBot.BOT_A, "tok", 1L, null, "file", "cap", new Byte[] { 0 });
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFileFileNameNullTest() {
		getMessenger().sendFile(FakeBot.BOT_A, "tok", 1L, 1L, null, "cap", new Byte[] { 0 });
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFileCaptionNullTest() {
		getMessenger().sendFile(FakeBot.BOT_A, "tok", 1L, 1L, "file", null, new Byte[] { 0 });
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFileFileContentNullTest() {
		getMessenger().sendFile(FakeBot.BOT_A, "tok", 1L, 1L, "file", "cap", null);
	}

	// ── sendFile against a replaced Telegram ──────────────────────────────────

	private static CapturingHttpRequester telegramReplaced() {
		CapturingHttpRequester telegram = new CapturingHttpRequester();
		CcpInstanceProvider<CcpHttpRequester> provider = () -> telegram;
		CcpDependencyInjection.loadAllDependencies(provider);
		return telegram;
	}

	@AfterClass
	public static void restoreTheRealHttpRequester() {
		CcpDependencyInjection.loadAllDependencies(new CcpApacheMimeHttp());
	}

	@Test
	public void aFileSentAsAnAnswerCarriesTheMessageItAnswers() {
		CapturingHttpRequester telegram = telegramReplaced();

		getMessenger().sendFile(FakeBot.BOT_A, "tok", 1L, 7L, "file", "cap", new Byte[] { 0 });

		assertEquals("7", telegram.getText("reply_to_message_id"));
	}

	@Test
	public void aFileSentWithoutAnswerCarriesNoMessageToAnswer() {
		CapturingHttpRequester telegram = telegramReplaced();

		getMessenger().sendFile(FakeBot.BOT_A, "tok", 1L, 0L, "file", "cap", new Byte[] { 0 });

		assertNull(telegram.getText("reply_to_message_id"));
		assertEquals("1", telegram.getText("chat_id"));
	}
}
