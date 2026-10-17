package com.js.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.business.login.JnBusinessValidateLogin;

public class JsServiceLoginTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── enum estrutura ───────────────────────────────────────────────────────

	@Test
	public void valuesTest() {
		assertEquals(9, JsServiceLogin.values().length);
	}

	@Test
	public void valueOfExecuteLoginTest() {
		assertNotNull(JsServiceLogin.valueOf("ExecuteLogin"));
	}

	/** Desde 2026-10-17 a validação da sessão é a classe JnBusinessValidateLogin, não mais um item do enum. */
	@Test(expected = IllegalArgumentException.class)
	public void validateLoginIsNoLongerAnItemTest() {
		JsServiceLogin.valueOf("ValidateLogin");
	}

	@Test
	public void validateLoginInstanceTest() {
		assertNotNull(JnBusinessValidateLogin.INSTANCE);
	}

	@Test
	public void allConstantsAccessibleTest() {
		assertNotNull(JsServiceLogin.ExecuteLogin);
		assertNotNull(JsServiceLogin.CreateLoginEmail);
		assertNotNull(JsServiceLogin.ExistsLoginEmail);
		assertNotNull(JsServiceLogin.ExecuteLogout);
		assertNotNull(JsServiceLogin.SaveAnswers);
		assertNotNull(JsServiceLogin.CreateLoginToken);
		assertNotNull(JsServiceLogin.SavePassword);
		assertNotNull(JsServiceLogin.ResendLoginToken);
		assertNotNull(JsServiceLogin.UnlockLoginToken);
	}

	// ── null-parameter tests (AOP): each value.apply(null) ───────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executeLoginApplyNullTest() {
		JsServiceLogin.ExecuteLogin.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validateLoginApplyNullTest() {
		JnBusinessValidateLogin.INSTANCE.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createLoginEmailApplyNullTest() {
		JsServiceLogin.CreateLoginEmail.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void existsLoginEmailApplyNullTest() {
		JsServiceLogin.ExistsLoginEmail.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executeLogoutApplyNullTest() {
		JsServiceLogin.ExecuteLogout.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveAnswersApplyNullTest() {
		JsServiceLogin.SaveAnswers.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createLoginTokenApplyNullTest() {
		JsServiceLogin.CreateLoginToken.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void savePasswordApplyNullTest() {
		JsServiceLogin.SavePassword.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void resendLoginTokenApplyNullTest() {
		JsServiceLogin.ResendLoginToken.execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void unlockLoginTokenApplyNullTest() {
		JsServiceLogin.UnlockLoginToken.execute((CcpJsonRepresentation) null);
	}

	// ── JsonFieldNames inner enum ────────────────────────────────────────────

	@Test
	public void jsonFieldNamesValuesTest() {
		assertNotNull(JsServiceLogin.JsonFieldNames.values());
	}

	@Test
	public void jsonFieldNamesValueOfTest() {
		assertNotNull(JsServiceLogin.JsonFieldNames.valueOf("inexistentField"));
	}

	/** sessionToken saiu do enum local e passou a viver no centralizador do centro de custo ccp. */
	@Test
	public void sessionTokenCentralizedTest() {
		assertNotNull(CcpJsonCommonsFields.valueOf("sessionToken"));
	}
}
