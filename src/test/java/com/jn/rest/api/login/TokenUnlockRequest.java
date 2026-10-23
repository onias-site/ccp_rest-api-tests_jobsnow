package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenRequestUnlock;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusUnlockLoginToken;

public class TokenUnlockRequest extends JnTestTemplate {

	@Test
	public void tokenNotLocked() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JsProcessStatusUnlockLoginToken.statusTokenNotLocked);
	}

	@Test
	public void unlockAlreadyDone() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.getTwinEntity().save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, CcpProcessStatusDefault.OK);
		JsEntityLoginTokenRequestUnlock.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked);
	}

	@Test
	public void unlockAlreadyRequested() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.getTwinEntity().save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginTokenRequestUnlock.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.getTwinEntity().save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, CcpProcessStatusDefault.OK);
	}

	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		this.requestUnlock(testVariables.VALID_EMAIL, expectedStatus);
		String producedValue = producer.apply(testVariables);
		return producedValue;
	}

	private void requestUnlock(String email, CcpProcessStatus expectedStatus) {
		String uri = "login/" + email + "/token/request/unlocking";
		this.testEndpoint(uri, expectedStatus);
	}

	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}
}
