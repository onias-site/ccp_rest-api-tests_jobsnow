package com.js.status.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JsProcessStatusEnumsTest {

	@Test
	public void createLoginEmailValuesTest() {
		assertNotNull(JsProcessStatusCreateLoginEmail.values());
	}

	@Test
	public void createLoginEmailAsNumberTest() {
		JsProcessStatusCreateLoginEmail.values()[0].asNumber();
	}

	@Test
	public void createLoginTokenValuesTest() {
		assertNotNull(JsProcessStatusCreateLoginToken.values());
	}

	@Test
	public void createLoginTokenAsNumberTest() {
		JsProcessStatusCreateLoginToken.values()[0].asNumber();
	}

	@Test
	public void executeLogoutValuesTest() {
		assertNotNull(JsProcessStatusExecuteLogout.values());
	}

	@Test
	public void executeLogoutAsNumberTest() {
		JsProcessStatusExecuteLogout.values()[0].asNumber();
	}

	@Test
	public void existsLoginEmailValuesTest() {
		assertNotNull(JsProcessStatusExistsLoginEmail.values());
	}

	@Test
	public void existsLoginEmailAsNumberTest() {
		JsProcessStatusExistsLoginEmail.values()[0].asNumber();
	}

	@Test
	public void saveAnswersValuesTest() {
		assertNotNull(JsProcessStatusSaveAnswers.values());
	}

	@Test
	public void saveAnswersAsNumberTest() {
		JsProcessStatusSaveAnswers.values()[0].asNumber();
	}

	@Test
	public void unlockLoginTokenValuesTest() {
		assertNotNull(JsProcessStatusUnlockLoginToken.values());
	}

	@Test
	public void unlockLoginTokenAsNumberTest() {
		JsProcessStatusUnlockLoginToken.values()[0].asNumber();
	}

	@Test
	public void updatePasswordValuesTest() {
		assertNotNull(JsProcessStatusUpdatePassword.values());
	}

	@Test
	public void updatePasswordAsNumberTest() {
		JsProcessStatusUpdatePassword.values()[0].asNumber();
	}

	/**
	 * A locked password is 423 in every endpoint: until 2026-10-14 the e-mail steps answered 427, the status of the wrong
	 * password in the login.
	 */
	@Test
	public void lockedPasswordIs423InEveryEndpoint() {
		org.junit.Assert.assertEquals(423, JsProcessStatusCreateLoginEmail.lockedPassword.asNumber());
		org.junit.Assert.assertEquals(423, JsProcessStatusExistsLoginEmail.lockedPassword.asNumber());
		org.junit.Assert.assertEquals(423, JsProcessStatusSaveAnswers.lockedPassword.asNumber());
		org.junit.Assert.assertEquals(423, JsProcessStatusExecuteLogin.lockedPassword.asNumber());
		org.junit.Assert.assertEquals(427, JsProcessStatusExecuteLogin.wrongPassword.asNumber());
	}
}
