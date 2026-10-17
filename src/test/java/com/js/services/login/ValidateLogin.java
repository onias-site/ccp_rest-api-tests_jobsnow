	package com.js.services.login;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.ccp.especifications.db.utils.entity.decorators.ElasticsearchLocal;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnSystemProperties;
import com.jn.rest.api.commons.TestVariables;
import com.jn.business.login.JnBusinessValidateLogin;
import com.js.services.JsServiceLogin;

public class ValidateLogin extends JsServiceLoginTestTemplate {

	public ValidateLogin() {
		super(JnBusinessValidateLogin.INSTANCE);
	}

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
		CcpJsonRepresentation loginResponse = JsServiceLogin.ExecuteLogin.execute(CcpOtherConstants
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

	/**
	 * A session past its deadline is refused (401), and repeating it more times than the attempts limit does not lock the
	 * password: it is a session that existed, not a guess. Until 2026-10-13 each request of an expired session counted an
	 * attempt, and a tab left open locked the password of its owner.
	 */
	@Test
	public void expiredSessionIsRefusedWithoutCountingAttempts() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation session = this.executeLogin(testVariables);
		this.execute(session, CcpProcessStatusDefault.OK);
		this.expire(session);

		int moreThanTheAttemptsLimit = JnSystemProperties.INSTANCE.maxAttempts() + 1;
		for (int attempt = 1; attempt <= moreThanTheAttemptsLimit; attempt++) {
			this.execute(session, CcpProcessStatusDefault.UNHAUTHORIZED);
		}

		boolean passwordStillUnlocked = JnEntityLoginPassword.ENTITY.exists(testVariables.REQUEST_TO_LOGIN);
		assertTrue(passwordStillUnlocked);
	}

	/** A token of no session at all keeps counting: the attempts limit locks the password. */
	@Test
	public void tokenOfNoSessionLocksThePasswordAtTheAttemptsLimit() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation session = this.executeLogin(testVariables);
		CcpJsonRepresentation guessedSession = session.put(new CcpFieldName("sessionToken"), "A8B7C6D5");

		int attemptsLimit = JnSystemProperties.INSTANCE.maxAttempts();
		for (int attempt = 1; attempt <= attemptsLimit; attempt++) {
			this.execute(guessedSession, CcpProcessStatusDefault.UNHAUTHORIZED);
		}

		boolean passwordLocked = false == JnEntityLoginPassword.ENTITY.exists(testVariables.REQUEST_TO_LOGIN);
		assertTrue(passwordLocked);
	}

	/**
	 * A session lives one hour from its creation, not until the turn of the hour: after the turn the documents of the
	 * period are gone (of the session and of the current session of the e-mail), the disposable copies still valid keep
	 * it (200). Until 2026-10-14 the current session was read only from the document of the period, and every session
	 * died at the turn of the hour (401), while the login of the same e-mail went on answering 409.
	 */
	@Test
	public void sessionSurvivesTheTurnOfTheHourWhileItsCopyIsValid() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation session = this.executeLogin(testVariables);
		this.execute(session, CcpProcessStatusDefault.OK);

		CcpFieldName sessionTokenField = new CcpFieldName("sessionToken");
		String sessionToken = session.getAsString(sessionTokenField);
		CcpJsonRepresentation sessionRecord = session.put(JnEntityLoginSessionValidation.Fields.token, sessionToken);
		this.deleteTheDocumentOfThePeriod(JnEntityLoginSessionValidation.ENTITY, sessionRecord);
		this.deleteTheDocumentOfThePeriod(JnEntityLoginSessionConflict.ENTITY, session);

		this.execute(session, CcpProcessStatusDefault.OK);
	}

	/**
	 * The right token of the current session sent by another user agent is refused (401) without counting an attempt: it
	 * is not a guess against the password. Until 2026-10-14 the attempts limit locked the password (423) while the session
	 * went on valid on its own user agent.
	 */
	@Test
	public void currentSessionFromAnotherUserAgentIsRefusedWithoutCountingAttempts() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation session = this.executeLogin(testVariables);
		CcpJsonRepresentation sessionFromAnotherUserAgent = session.put(JnJsonCommonsFields.userAgent, "Mozilla/5.0 (another browser)");

		int moreThanTheAttemptsLimit = JnSystemProperties.INSTANCE.maxAttempts() + 1;
		for (int attempt = 1; attempt <= moreThanTheAttemptsLimit; attempt++) {
			this.execute(sessionFromAnotherUserAgent, CcpProcessStatusDefault.UNHAUTHORIZED);
		}

		boolean passwordStillUnlocked = JnEntityLoginPassword.ENTITY.exists(testVariables.REQUEST_TO_LOGIN);
		assertTrue(passwordStillUnlocked);
		this.execute(session, CcpProcessStatusDefault.OK);
	}

	/** The turn of the period: the document of the current period is gone, the disposable copy stays. */
	private void deleteTheDocumentOfThePeriod(CcpEntity entity, CcpJsonRepresentation record) {
		CcpEntityMetaData metaData = entity.getEntityMetaData();
		CcpJsonRepresentation transformedRecord = entity.getHandledJson(record);
		String periodDocumentId = entity.calculateId(transformedRecord);
		ElasticsearchLocal.delete(metaData.entityName, periodDocumentId);
	}

	/**
	 * Takes the session past its deadline the way time does: the document of the period is gone (the turn of the hour)
	 * and the disposable copy is expired.
	 */
	private void expire(CcpJsonRepresentation session) {
		CcpFieldName sessionTokenField = new CcpFieldName("sessionToken");
		String sessionToken = session.getAsString(sessionTokenField);
		CcpJsonRepresentation record = session.put(JnEntityLoginSessionValidation.Fields.token, sessionToken);
		CcpEntity entity = JnEntityLoginSessionValidation.ENTITY;
		CcpEntityMetaData metaData = entity.getEntityMetaData();
		CcpJsonRepresentation transformedRecord = entity.getHandledJson(record);

		String periodDocumentId = entity.calculateId(transformedRecord);
		ElasticsearchLocal.delete(metaData.entityName, periodDocumentId);

		String serializedPrimaryKey = metaData.getPrimaryKeyValues(transformedRecord.getJsonSupplier()).asUgglyJson();
		CcpJsonRepresentation copyKey = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, metaData.entityName)
				.put(JnJsonCommonsFields.id, serializedPrimaryKey);
		String copyId = JnEntityDisposableRecord.ENTITY.calculateId(copyKey);
		long now = System.currentTimeMillis();
		CcpJsonRepresentation expiredTimestamp = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.timestamp, now - 1000);
		ElasticsearchLocal.update("jn_disposable_record", copyId, expiredTimestamp);
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
