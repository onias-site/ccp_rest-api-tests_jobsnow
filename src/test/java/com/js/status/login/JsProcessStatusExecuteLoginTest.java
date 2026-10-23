package com.js.status.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JsProcessStatusExecuteLoginTest {

	@Test
	public void allEnumValuesTest() {
		assertNotNull(JsProcessStatusExecuteLogin.values());
		assertEquals(429, JsProcessStatusExecuteLogin.passwordLockedRecently.asNumber());
		assertEquals(401, JsProcessStatusExecuteLogin.missingSessionToken.asNumber());
		assertEquals(423, JsProcessStatusExecuteLogin.lockedPassword.asNumber());
		assertEquals(200, JsProcessStatusExecuteLogin.expectedStatus.asNumber());
		assertEquals(427, JsProcessStatusExecuteLogin.wrongPassword.asNumber());
		assertEquals(409, JsProcessStatusExecuteLogin.loginConflict.asNumber());
	}

	@Test
	public void flowDisturbTest() {
		assertNotNull(JsProcessStatusExecuteLogin.expectedStatus.flowDisturb());
	}

	@Test
	public void valueOfTest() {
		assertNotNull(JsProcessStatusExecuteLogin.valueOf("expectedStatus"));
	}
}
