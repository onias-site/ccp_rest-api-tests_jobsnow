package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;

public class TokenResendRequest extends JnTestTemplate {

	@Test
	public void resendAlreadyDone() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, CcpProcessStatusDefault.OK);
		JnEntityLoginTokenRequestResend.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusUnlockLoginToken.statusTokenAlredyResent);
	}

	@Test
	public void resendAlreadyRequested() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestResend.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}
	@Test
	public void tokenDoesNotExist() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginTokenRequestResend.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusUnlockLoginToken.statusTokenNotExists);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginToken.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
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
