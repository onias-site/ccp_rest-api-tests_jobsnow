package com.js.services.login;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.jn.entities.JnEntityEmailSuppressed;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusExistsLoginEmail;

public class ExistsLoginEmail extends JsServiceLoginTestTemplate {

	@Test(expected = CcpJsonValidationError.class)
	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		this.execute(testVariables.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.lockedToken);
	}

	/** 2026-11-23: an address the e-mail provider suppresses never receives the login token, even with a complete login. */
	@Test
	public void suppressedEmail() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation suppression = testVariables.REQUEST_TO_LOGIN.getJsonPiece(JnEntityEmailSuppressed.Fields.email)
				.put(JnEntityEmailSuppressed.Fields.suppressionList, CcpEmailSuppressionList.bounces.name())
				.put(JnEntityEmailSuppressed.Fields.created, 1700000000L);
		JnEntityEmailSuppressed.ENTITY.save(suppression);
		try {
			this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.suppressedEmail);
		} finally {
			JnEntityEmailSuppressed.ENTITY.delete(suppression);
		}
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.missingEmail);
	}

	@Test
	public void lockedPassword() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.lockedPassword);
	}

	@Test
	public void openSessionIsToldOnlyByThePasswordLogin() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.missingAnswers);
	}

	@Test
	public void missingPasswordRegistration() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.missingPassword);
	}

	@Test
	public void missingPreRegistration() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.missingAnswers);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JsEntityLoginPassword.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusExistsLoginEmail.expectedStatus);
	}

	public void execute(TestVariables testVariables, CcpProcessStatus expectedStatus) {
		this.execute(testVariables.REQUEST_TO_LOGIN, expectedStatus);
	}
}
