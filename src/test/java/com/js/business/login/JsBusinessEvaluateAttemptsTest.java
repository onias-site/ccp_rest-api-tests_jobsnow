package com.js.business.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JsBusinessEvaluateAttemptsTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void builderTest() {
		assertNotNull(JsBusinessEvaluateAttempts.builder());
	}

	@Test
	public void newBuilderInstanceTest() { 
		assertNotNull(new Builder());
	}

	// ── Builder fluent API — null-parameter tests ────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void builderEntityToGetTheAttemptsNullTest() {
		JsBusinessEvaluateAttempts.builder().entityToGetTheAttempts(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderEntityToGetTheSecretNullTest() {
		JsBusinessEvaluateAttempts.builder().entityToGetTheSecret(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderDatabaseFieldNameNullTest() {
		JsBusinessEvaluateAttempts.builder().databaseFieldName(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderUserFieldNameNullTest() {
		JsBusinessEvaluateAttempts.builder().userFieldName(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderStatusWhenExceedAttemptsNullTest() {
		JsBusinessEvaluateAttempts.builder().statusWhenExceedAttempts(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderStatusWhenWrongTypeNullTest() {
		JsBusinessEvaluateAttempts.builder().statusWhenWrongType(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderLockUsingNullTest() {
		JsBusinessEvaluateAttempts.builder().lockUsing(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderOnSuccessNullTest() {
		JsBusinessEvaluateAttempts.builder().onSuccess(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderAttemptsFieldNameNullTest() {
		JsBusinessEvaluateAttempts.builder().attemptsFieldName(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderEmailFieldNameNullTest() {
		JsBusinessEvaluateAttempts.builder().emailFieldName(null);
	}
}
