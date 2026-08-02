package com.jn.status.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JnProcessStatusExecuteLoginTest {

	@Test
	public void allEnumValuesTest() {
		assertNotNull(JnProcessStatusExecuteLogin.values());
		assertEquals(429, JnProcessStatusExecuteLogin.passwordLockedRecently.asNumber());
		assertEquals(401, JnProcessStatusExecuteLogin.missingSessionToken.asNumber());
		assertEquals(423, JnProcessStatusExecuteLogin.lockedPassword.asNumber());
		assertEquals(200, JnProcessStatusExecuteLogin.expectedStatus.asNumber());
		assertEquals(427, JnProcessStatusExecuteLogin.wrongPassword.asNumber());
		assertEquals(409, JnProcessStatusExecuteLogin.loginConflict.asNumber());
	}

	@Test
	public void flowDisturbTest() {
		assertNotNull(JnProcessStatusExecuteLogin.expectedStatus.flowDisturb());
	}

	@Test
	public void valueOfTest() {
		assertNotNull(JnProcessStatusExecuteLogin.valueOf("expectedStatus"));
	}
}
