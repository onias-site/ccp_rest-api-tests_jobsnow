package com.jn.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnServiceLoginTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── enum estrutura ───────────────────────────────────────────────────────

	@Test
	public void valuesTest() {
		assertEquals(10, JnServiceLogin.values().length);
	}

	@Test
	public void valueOfExecuteLoginTest() {
		assertNotNull(JnServiceLogin.valueOf("ExecuteLogin"));
	}

	@Test
	public void valueOfValidateLoginTest() {
		assertNotNull(JnServiceLogin.valueOf("ValidateLogin"));
	}

	@Test
	public void allConstantsAccessibleTest() {
		assertNotNull(JnServiceLogin.ExecuteLogin);
		assertNotNull(JnServiceLogin.ValidateLogin);
		assertNotNull(JnServiceLogin.CreateLoginEmail);
		assertNotNull(JnServiceLogin.ExistsLoginEmail);
		assertNotNull(JnServiceLogin.ExecuteLogout);
		assertNotNull(JnServiceLogin.SaveAnswers);
		assertNotNull(JnServiceLogin.CreateLoginToken);
		assertNotNull(JnServiceLogin.SavePassword);
		assertNotNull(JnServiceLogin.ResendLoginToken);
		assertNotNull(JnServiceLogin.UnlockLoginToken);
	}

	// ── null-parameter tests (AOP) — cada valor.apply(null) ──────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeLoginApplyNullTest() {
		JnServiceLogin.ExecuteLogin.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validateLoginApplyNullTest() {
		JnServiceLogin.ValidateLogin.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createLoginEmailApplyNullTest() {
		JnServiceLogin.CreateLoginEmail.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void existsLoginEmailApplyNullTest() {
		JnServiceLogin.ExistsLoginEmail.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeLogoutApplyNullTest() {
		JnServiceLogin.ExecuteLogout.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveAnswersApplyNullTest() {
		JnServiceLogin.SaveAnswers.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createLoginTokenApplyNullTest() {
		JnServiceLogin.CreateLoginToken.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void savePasswordApplyNullTest() {
		JnServiceLogin.SavePassword.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void resendLoginTokenApplyNullTest() {
		JnServiceLogin.ResendLoginToken.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unlockLoginTokenApplyNullTest() {
		JnServiceLogin.UnlockLoginToken.apply(null);
	}

	// ── JsonFieldNames inner enum ────────────────────────────────────────────

	@Test
	public void jsonFieldNamesValuesTest() {
		assertNotNull(JnServiceLogin.JsonFieldNames.values());
	}

	@Test
	public void jsonFieldNamesValueOfTest() {
		assertNotNull(JnServiceLogin.JsonFieldNames.valueOf("sessionToken"));
	}
}
