package com.jn.services.login;

import org.junit.Test;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusUpdatePassword;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class SavePassword extends JnServiceLoginTestTemplate {

	@Test(expected = CcpJsonValidationError.class)
	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, "abcdefgh");
		this.execute(body, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		String token = getTokenToValidateLogin(testVariables);
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, token);
		this.execute(body, JnProcessStatusUpdatePassword.lockedToken);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, "12345678");
		this.execute(body, JnProcessStatusUpdatePassword.missingEmail);
	}

	@Test
	public void performUnlocks() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.expectedFlow(testVariables);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables("onias85@gmail.com");
		this.expectedFlow(testVariables);
	}

	@Test(expected = CcpJsonValidationError.class)
	public void invalidJson() {
		this.execute(CcpOtherConstants.EMPTY_JSON, JnProcessStatusUpdatePassword.invalidJson);
	}

	@Test
	public void missTokenThenGetItRight() {
		TestVariables testVariables = new TestVariables();
		String token = getToken(testVariables);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		for (int k = 1; k < 3; k++) {
			CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
					.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
					.put(JnEntityLoginToken.Fields.token, "abcdefgh");
			this.execute(body, JnProcessStatusUpdatePassword.wrongToken);
		}
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, token);
		this.execute(body, JnProcessStatusUpdatePassword.expectedStatus);
	}

	@Test
	public void recentlyLockedToken() {
		TestVariables testVariables = new TestVariables();
		String token = getToken(testVariables);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		for (int k = 1; k <= 2; k++) {
			CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
					.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
					.put(JnEntityLoginToken.Fields.token, "abcdefgh");
			this.execute(body, JnProcessStatusUpdatePassword.wrongToken);
		}
		CcpJsonRepresentation bodyWrong = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, "abcdefgh");
		this.execute(bodyWrong, JnProcessStatusUpdatePassword.tokenLockedRecently);
		CcpJsonRepresentation bodyRight = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, token);
		this.execute(bodyRight, JnProcessStatusUpdatePassword.lockedToken);
	}

	public CcpJsonRepresentation expectedFlow(TestVariables testVariables) {
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);

		String token = this.getToken(testVariables);
		CcpJsonRepresentation body = testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, token);
		return this.execute(body, JnProcessStatusUpdatePassword.expectedStatus);
	}

	/**
	 * The plain token already comes in the request json, put there by the token transformer, and save
	 * reuses it instead of generating another one. Saving the transformed json would mean saving the
	 * e-mail already converted into a hash, which the entity's validations — applied before the
	 * transformers — would refuse.
	 */
	private String getToken(TestVariables testVariables) {
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		String token = testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.originalToken);
		return token;
	}

	private String getTokenToValidateLogin(TestVariables testVariables) {
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		return "12345678";
	}
}
