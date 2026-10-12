	package com.jn.services.login;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnSystemProperties;
import com.jn.rest.api.commons.TestVariables;
import com.jn.services.JnServiceLogin;

public class ValidateLogin extends JnServiceLoginTestTemplate {

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables("onias85@gmail.com");
		CcpJsonRepresentation session = this.executeLogin(testVariables);
		this.execute(session, CcpProcessStatusDefault.OK);
	}

	private CcpJsonRepresentation executeLogin(TestVariables testVariables) {
		SavePassword savePassword = new SavePassword();
		savePassword.expectedFlow(testVariables);
		ExecuteLogout executeLogout = new ExecuteLogout();
		executeLogout.expectedFlow(testVariables);
		CcpJsonRepresentation loginResponse = JnServiceLogin.ExecuteLogin.execute(CcpOtherConstants
				.EMPTY_JSON 
				.put(JnJsonCommonsFields.email, testVariables.VALID_EMAIL)
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				);
		CcpJsonRepresentation session = this.getSession(testVariables, loginResponse);
		return session;
	}

	private CcpJsonRepresentation getSession(TestVariables testVariables, CcpJsonRepresentation responseWithTheSessionToken) {
		CcpFieldName field = new CcpFieldName("sessionToken");
		String sessionToken = responseWithTheSessionToken.getAsString(field);

		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, testVariables.VALID_EMAIL)
				.put(field, sessionToken)
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
;
		return session;
	}

	/**
	 * A password save ends every other session of the e-mail: the session it replaced is refused (401), and repeating it
	 * more times than the attempts limit does not lock the password, because it was valid until it was replaced. Until
	 * 2026-10-11 the old session stayed valid.
	 */
	@Test
	public void sessionReplacedByAPasswordSaveIsRefusedWithoutCountingAttempts() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation replacedSession = this.executeLogin(testVariables);
		SavePassword savePassword = new SavePassword();
		CcpJsonRepresentation passwordSaveResponse = savePassword.expectedFlow(testVariables);
		CcpJsonRepresentation currentSession = this.getSession(testVariables, passwordSaveResponse);

		int moreThanTheAttemptsLimit = JnSystemProperties.INSTANCE.maxAttempts() + 1;
		for (int attempt = 1; attempt <= moreThanTheAttemptsLimit; attempt++) {
			this.execute(replacedSession, CcpProcessStatusDefault.UNHAUTHORIZED);
		}

		boolean passwordStillUnlocked = JnEntityLoginPassword.ENTITY.exists(testVariables.REQUEST_TO_LOGIN);
		assertTrue(passwordStillUnlocked);
		this.execute(currentSession, CcpProcessStatusDefault.OK);
	}

	@Test(expected = CcpJsonValidationError.class)
	public void tokenInInvalidFormat() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation session = this.executeLogin(testVariables);
		this.execute(session.put(new CcpFieldName("sessionToken"), "1234567"), CcpProcessStatusDefault.UNPROCESSABLE_ENTITY); 
	}

	@Test
	public void invalidToken() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, testVariables.VALID_EMAIL)
				.put(new CcpFieldName("sessionToken"), "A8B7C6D5")
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				;
		this.execute(json, CcpProcessStatusDefault.UNHAUTHORIZED);
	}

	@Test
	public void invalidTokenTwice() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, testVariables.VALID_EMAIL)
				.put(new CcpFieldName("sessionToken"), "A8B7C6D5")
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				;
		// the cache of the local API (the mock one stores nothing): the second attempt reads from it the counter saved
		// by the first one, an Integer instead of the Double parsed from the database
		CcpDependencyInjection.loadAllDependencies(CcpLocalCacheInstances.map);
		try {
			this.execute(json, CcpProcessStatusDefault.UNHAUTHORIZED);
			this.execute(json, CcpProcessStatusDefault.UNHAUTHORIZED);
		} finally {
			CcpDependencyInjection.loadAllDependencies(CcpLocalCacheInstances.mock);
		}
	}
}
