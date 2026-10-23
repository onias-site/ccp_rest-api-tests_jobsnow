package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.js.status.login.JsProcessStatusUnlockLoginToken;

public class TokenResendRequest extends JnTestTemplate {

	@Test
	public void resendAlreadyDone() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, CcpProcessStatusDefault.OK);
		JsEntityLoginTokenRequestResend.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusUnlockLoginToken.statusTokenAlredyResent);
	}

	@Test
	public void resendAlreadyRequested() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JsEntityLoginTokenRequestResend.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}
	@Test
	public void tokenDoesNotExist() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginTokenRequestResend.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JsProcessStatusUnlockLoginToken.statusTokenNotExists);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JsEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, CcpProcessStatusDefault.OK);
	}

	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		this.requestResend(testVariables.VALID_EMAIL, expectedStatus);
		String producedValue = producer.apply(testVariables);
		return producedValue;
	}

	private void requestResend(String email, CcpProcessStatus expectedStatus) {
		String uri = "login/" + email + "/token/request/resending";
		this.testEndpoint(uri, expectedStatus);
	}

	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}
}
