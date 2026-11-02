package com.js.services;

import com.jn.business.messages.JnMessageType;
import com.jn.utils.JnSystemProperties;

/** Reads the token of the support bot, which the tests compare without ever printing it. */
final class JsBotTokenForTest {

	private JsBotTokenForTest() {}

	static String supportBotToken() {
		String botToken = JnSystemProperties.INSTANCE.getSystemInnerProperty(JnMessageType.InstantMessengerApiFields.bots, () -> "support");
		return botToken;
	}
}
