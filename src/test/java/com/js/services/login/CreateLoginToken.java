package com.js.services.login;

import org.junit.Test;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.process.CcpProcessStatusDefault;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusCreateLoginToken;
import com.jn.utils.JnLanguage;

public class CreateLoginToken extends JsServiceLoginTestTemplate {

	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, JsProcessStatusCreateLoginToken.statusLockedToken);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, JsProcessStatusCreateLoginToken.statusMissingEmail);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, JsProcessStatusCreateLoginToken.expectedStatus);
	}

	/**
	 * Asking for the token again right after it was sent answers the token creation's own status (429); until 2026-10-06
	 * it answered {@code JsProcessStatusUpdatePassword.tokenAlreadySent} (409), the status of another service.
	 */
	@Test
	public void tokenAlreadySent() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, JsProcessStatusCreateLoginToken.expectedStatus);

		this.execute(jsonWithLanguage, JsProcessStatusCreateLoginToken.statusAlreadySentToken);
	}

	private com.ccp.decorators.CcpJsonRepresentation withLanguage(TestVariables testVariables) {
		return testVariables.REQUEST_TO_LOGIN.put(new CcpFieldName("language"), JnLanguage.portuguese.name());
	}
}
