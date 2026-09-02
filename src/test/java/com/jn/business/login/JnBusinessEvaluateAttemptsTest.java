package com.jn.business.login;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessEvaluateAttemptsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void builderTest() {
		assertNotNull(JnBusinessEvaluateAttempts.builder());
	}

	@Test
	public void newBuilderInstanceTest() { 
		assertNotNull(new Builder());
	}

	// ── Builder fluent API — null-parameter tests ────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void builderEntityToGetTheAttemptsNullTest() {
		JnBusinessEvaluateAttempts.builder().entityToGetTheAttempts(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderEntityToGetTheSecretNullTest() {
		JnBusinessEvaluateAttempts.builder().entityToGetTheSecret(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderDatabaseFieldNameNullTest() {
		JnBusinessEvaluateAttempts.builder().databaseFieldName(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderUserFieldNameNullTest() {
		JnBusinessEvaluateAttempts.builder().userFieldName(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderStatusWhenExceedAttemptsNullTest() {
		JnBusinessEvaluateAttempts.builder().statusWhenExceedAttempts(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderStatusWhenWrongTypeNullTest() {
		JnBusinessEvaluateAttempts.builder().statusWhenWrongType(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderLockUsingNullTest() {
		JnBusinessEvaluateAttempts.builder().lockUsing(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderOnSuccessNullTest() {
		JnBusinessEvaluateAttempts.builder().onSuccess(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderAttemptsFieldNameNullTest() {
		JnBusinessEvaluateAttempts.builder().attemptsFieldName(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void builderEmailFieldNameNullTest() {
		JnBusinessEvaluateAttempts.builder().emailFieldName(null);
	}
}
