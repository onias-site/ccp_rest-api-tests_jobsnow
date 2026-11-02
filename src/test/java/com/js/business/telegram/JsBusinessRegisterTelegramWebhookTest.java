package com.js.business.telegram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * The webhook is registered with the secret of the bot, one connection and only the updates with a message, and never
 * without a secret. Telegram is a test double: nothing leaves the machine.
 */
public class JsBusinessRegisterTelegramWebhookTest {

	private static final String SUPPORT = "support";

	private static final class FakeTelegram implements CcpHttpRequester {

		private final int httpStatus;

		private String lastBody = "";

		private String lastUrl = "";

		private FakeTelegram(int httpStatus) {
			this.httpStatus = httpStatus;
		}

		public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
			this.lastUrl = url;
			this.lastBody = body;
			return new CcpHttpResponse("{\"ok\":true,\"result\":true}", this.httpStatus, "curl");
		}

		public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
			throw new UnsupportedOperationException();
		}
	}

	private FakeTelegram telegramAnswering(int httpStatus) {
		FakeTelegram telegram = new FakeTelegram(httpStatus);
		CcpInstanceProvider<CcpHttpRequester> provider = () -> telegram;
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, provider);
		return telegram;
	}

	private CcpJsonRepresentation request(String botName) {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, botName)
				.put(JnJsonCommonsFields.url, "https://api.example.com");
		return request;
	}

	@Test
	public void theWebhookIsRegisteredWithTheSecretOfTheBot() {
		FakeTelegram telegram = this.telegramAnswering(200);

		JsBusinessRegisterTelegramWebhook.INSTANCE.execute(this.request(SUPPORT));

		CcpJsonRepresentation body = new CcpJsonRepresentation(telegram.lastBody);
		assertTrue(telegram.lastUrl.endsWith("/setWebhook"));
		assertEquals("https://api.example.com/telegram-webhook/support", body.getAsString(JnJsonCommonsFields.url));
		assertEquals(JsTelegramWebhookSecret.INSTANCE.getSecret(SUPPORT), body.getAsString(JsBusinessRegisterTelegramWebhook.Fields.secret_token));
		assertEquals(1, body.getAsIntegerNumber(JsBusinessRegisterTelegramWebhook.Fields.max_connections).intValue());
		assertEquals(List.of("message"), body.getAsStringList(JsBusinessRegisterTelegramWebhook.Fields.allowed_updates));
	}

	/** A failed registration ends in a message on the terminal, and the secret must not be in it. */
	@Test
	public void aFailedRegistrationDoesNotShowTheSecret() {
		this.telegramAnswering(400);
		String secret = JsTelegramWebhookSecret.INSTANCE.getSecret(SUPPORT);

		try {
			JsBusinessRegisterTelegramWebhook.INSTANCE.execute(this.request(SUPPORT));
			fail("the status 400 was accepted");
		} catch (CcpErrorHttp e) {
			assertFalse("the secret is in the error", e.getMessage().contains(secret));
			assertFalse("the secret is in the details", e.entity.asUgglyJson().contains(secret));
		}
	}

	/** A webhook registered without a secret would refuse everything, and it must fail here, not silently in production. */
	@Test
	public void aBotWithoutSecretIsNotRegistered() {
		FakeTelegram telegram = this.telegramAnswering(200);

		try {
			JsBusinessRegisterTelegramWebhook.INSTANCE.execute(this.request("botWithoutSecret"));
			fail("a webhook without secret was registered");
		} catch (JsErrorTelegramBotHasNoWebhookSecret e) {
			assertTrue(telegram.lastUrl.isEmpty());
		}
	}

	@Test
	public void deletingTheWebhookCallsTheResourceOfTheBot() {
		FakeTelegram telegram = this.telegramAnswering(200);

		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.botName, SUPPORT);
		JsBusinessDeleteTelegramWebhook.INSTANCE.execute(request);

		assertTrue(telegram.lastUrl.endsWith("/deleteWebhook"));
	}
}
