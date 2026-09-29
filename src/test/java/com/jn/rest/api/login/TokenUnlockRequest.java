package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;

public class TokenUnlockRequest extends JnTestTemplate {

	@Test
	public void tokenNotLocked() {
		TestVariables testVariables = new TestVariables();
		this.execute(testVariables, JnProcessStatusUnlockLoginToken.statusTokenNotLocked);
	}

	@Test
	public void unlockAlreadyDone() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, CcpProcessStatusDefault.OK);
		JnEntityLoginTokenRequestUnlock.ENTITY.delete(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked);
	}

	@Test
	public void unlockAlreadyRequested() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestUnlock.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(testVariables.REQUEST_TO_LOGIN);
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
