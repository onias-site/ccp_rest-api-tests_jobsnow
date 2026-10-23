package com.js.services.login;

import org.junit.Test;

import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusUnlockLoginToken;

public class ResendLoginToken extends JsServiceLoginTestTemplate {

	@Test(expected = CcpJsonValidationError.class)
	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		this.execute(testVariables.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	/**
	 * The fulfilled request is the one in the twin entity, and saving to it is what deletes the record
	 * from the main one. Asking for the delete on the twin would do the opposite: it would transfer the
	 * record from the twin back to the main entity, setting up the same scenario as {@link #resendAlreadyRequested()}.
	 */
	@Test
	public void resendAlreadyDone() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginTokenRequestResend.ENTITY.getTwinEntity().save(testVariables.REQUEST_TO_LOGIN);

		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusUnlockLoginToken.statusTokenAlredyResent);
	}

	@Test
	public void tokenDoesNotExist() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginTokenRequestResend.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusUnlockLoginToken.statusTokenNotExists);
	}

	@Test
	public void resendAlreadyRequested() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginTokenRequestResend.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, JsProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables.REQUEST_TO_LOGIN, CcpProcessStatusDefault.OK);
	}
}
