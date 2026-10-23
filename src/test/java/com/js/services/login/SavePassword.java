package com.js.services.login;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.js.entities.JsEntityLoginAnswers;
import com.js.entities.JsEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusUpdatePassword;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class SavePassword extends JsServiceLoginTestTemplate {

	@Test(expected = CcpJsonValidationError.class)
	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JsEntityLoginToken.Fields.token, "abcdefgh");
		this.execute(body, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		String token = getTokenToValidateLogin(testVariables);
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JsEntityLoginToken.Fields.token, token);
		this.execute(body, JsProcessStatusUpdatePassword.lockedToken);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JsEntityLoginEmail.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JsEntityLoginToken.Fields.token, "12345678");
		this.execute(body, JsProcessStatusUpdatePassword.missingEmail);
	}

	@Test
	public void performUnlocks() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JsEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.expectedFlow(testVariables);
		// the status alone does not prove the unlock: the password must leave the locked twin for the main entity
		boolean stillLocked = mirrorEntity.exists(testVariables.REQUEST_TO_LOGIN);
		boolean savedInTheMainEntity = JsEntityLoginPassword.ENTITY.exists(testVariables.REQUEST_TO_LOGIN);
		assertFalse(stillLocked);
		assertTrue(savedInTheMainEntity);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables("onias85@gmail.com");
		this.expectedFlow(testVariables);
	}

	@Test(expected = CcpJsonValidationError.class)
	public void invalidJson() {
		this.execute(CcpOtherConstants.EMPTY_JSON, JsProcessStatusUpdatePassword.invalidJson);
	}

	@Test
	public void missTokenThenGetItRight() {
		TestVariables testVariables = new TestVariables();
		String token = getToken(testVariables);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		for (int k = 1; k < 3; k++) {
			CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
					.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
					.put(JsEntityLoginToken.Fields.token, "abcdefgh");
			this.execute(body, JsProcessStatusUpdatePassword.wrongToken);
		}
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JsEntityLoginToken.Fields.token, token);
		this.execute(body, JsProcessStatusUpdatePassword.expectedStatus);
	}

	@Test
	public void recentlyLockedToken() {
		TestVariables testVariables = new TestVariables();
		String token = getToken(testVariables);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		for (int k = 1; k <= 2; k++) {
			CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
					.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
					.put(JsEntityLoginToken.Fields.token, "abcdefgh");
			this.execute(body, JsProcessStatusUpdatePassword.wrongToken);
		}
		CcpJsonRepresentation bodyWrong = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JsEntityLoginToken.Fields.token, "abcdefgh");
		this.execute(bodyWrong, JsProcessStatusUpdatePassword.tokenLockedRecently);
		CcpJsonRepresentation bodyRight = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JsEntityLoginToken.Fields.token, token);
		this.execute(bodyRight, JsProcessStatusUpdatePassword.lockedToken);
	}

	public CcpJsonRepresentation expectedFlow(TestVariables testVariables) {
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);

		String token = this.getToken(testVariables);
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JsEntityLoginToken.Fields.token, token);
		return this.execute(body, JsProcessStatusUpdatePassword.expectedStatus);
	}

	/**
	 * The plain token already comes in the request json, put there by the token transformer, and save
	 * reuses it instead of generating another one. Saving the transformed json would mean saving the
	 * e-mail already converted into a hash, which the entity's validations — applied before the
	 * transformers — would refuse.
	 */
	private String getToken(TestVariables testVariables) {
		JsEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		String token = testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.originalToken);
		return token;
	}

	private String getTokenToValidateLogin(TestVariables testVariables) {
		JsEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		return "12345678";
	}
}
