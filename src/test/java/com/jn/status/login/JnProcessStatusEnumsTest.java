package com.jn.status.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JnProcessStatusEnumsTest {

	@Test
	public void createLoginEmailValuesTest() {
		assertNotNull(JnProcessStatusCreateLoginEmail.values());
	}

	@Test
	public void createLoginEmailAsNumberTest() {
		JnProcessStatusCreateLoginEmail.values()[0].asNumber();
	}

	@Test
	public void createLoginTokenValuesTest() {
		assertNotNull(JnProcessStatusCreateLoginToken.values());
	}

	@Test
	public void createLoginTokenAsNumberTest() {
		JnProcessStatusCreateLoginToken.values()[0].asNumber();
	}

	@Test
	public void executeLogoutValuesTest() {
		assertNotNull(JnProcessStatusExecuteLogout.values());
	}

	@Test
	public void executeLogoutAsNumberTest() {
		JnProcessStatusExecuteLogout.values()[0].asNumber();
	}

	@Test
	public void existsLoginEmailValuesTest() {
		assertNotNull(JnProcessStatusExistsLoginEmail.values());
	}

	@Test
	public void existsLoginEmailAsNumberTest() {
		JnProcessStatusExistsLoginEmail.values()[0].asNumber();
	}

	@Test
	public void saveAnswersValuesTest() {
		assertNotNull(JnProcessStatusSaveAnswers.values());
	}

	@Test
	public void saveAnswersAsNumberTest() {
		JnProcessStatusSaveAnswers.values()[0].asNumber();
	}

	@Test
	public void unlockLoginTokenValuesTest() {
		assertNotNull(JnProcessStatusUnlockLoginToken.values());
	}

	@Test
	public void unlockLoginTokenAsNumberTest() {
		JnProcessStatusUnlockLoginToken.values()[0].asNumber();
	}

	@Test
	public void updatePasswordValuesTest() {
		assertNotNull(JnProcessStatusUpdatePassword.values());
	}

	@Test
	public void updatePasswordAsNumberTest() {
		JnProcessStatusUpdatePassword.values()[0].asNumber();
	}
}
