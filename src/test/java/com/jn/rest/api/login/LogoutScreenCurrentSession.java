package com.jn.rest.api.login;

import java.util.function.Function;

import com.jn.rest.api.commons.TestVariables;

/** Token producer of {@link LogoutScreen}: saves the current session with the given token and hands that token. */
class LogoutScreenCurrentSession implements Function<TestVariables, String> {

	/** The token of the session. */
	private final String token;

	LogoutScreenCurrentSession(String token) {
		this.token = token;
	}

	public String apply(TestVariables variables) {
		variables.saveCurrentSession(this.token);
		return this.token;
	}
}
