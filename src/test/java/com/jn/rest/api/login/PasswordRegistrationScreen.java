package com.jn.rest.api.login;

import java.util.function.Function;
import org.junit.Test;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.rest.api.commons.JnTestTemplate;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusUpdatePassword;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class PasswordRegistrationScreen extends JnTestTemplate{
	
	@Test
	public void invalidEmail() {
		this.sendFakeRequest(TestVariables.INVALID_EMAIL, "abcdefgh", JnProcessStatusUpdatePassword.invalidEmail);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save( testVariables.REQUEST_TO_LOGIN);
		String token = this.getTokenToValidateLogin(testVariables);
		this.execute(testVariables, JnProcessStatusUpdatePassword.lockedToken, x -> token);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		String token = this.getTokenToValidateLogin(testVariables);
		JnEntityLoginEmail.ENTITY.delete( testVariables.REQUEST_TO_LOGIN);
		this.execute(testVariables, JnProcessStatusUpdatePassword.missingEmail, x -> token);
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
		TestVariables testVariables = new TestVariables();
		String flowResult = this.expectedFlow(testVariables);
		System.out.println(flowResult);
	}
	
	@Test
	public void invalidJson() {
		TestVariables testVariables = new TestVariables();
		String uri = "login/" + testVariables.VALID_EMAIL + "/password";
		this.testEndpoint(JnProcessStatusUpdatePassword.invalidJson, CcpOtherConstants.EMPTY_JSON, uri,  CcpHttpResponseType.singleRecord);

	}
	
	public String expectedFlow(TestVariables testVariables) {
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		String token = this.getToken(testVariables);
		String result = this.execute(testVariables,JnProcessStatusUpdatePassword.expectedStatus, x -> token);
		return result;
	}

	public static void main(String[] args) {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		
	}
	
	/**
	 * The test chooses the token and puts it in {@code originalToken}: the token transformer uses that
	 * value instead of drawing another one. Until 2026-09-27 the test pre-transformed the json with
	 * {@code getHandledJson} to find out the drawn token and saved the already transformed json — with the
	 * e-mail hashed, which the validator (outside the transformer) rejected.
	 */
	private String getToken(TestVariables testVariables) {
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);

		String token = JnJsonTransformersFieldsEntityDefault.getOriginalToken();
		CcpJsonRepresentation jsonWithToken = testVariables.REQUEST_TO_LOGIN.put(LogoutScreenConstants.originalToken, token);
		JnEntityLoginToken.ENTITY.save(jsonWithToken);
		return token;
	}

	@Test
	public void missTokenThenGetItRight() {
		TestVariables testVariables = new TestVariables();
		String token = this.getToken(testVariables);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		for(int k = 1; k < 3; k++) {
			this.execute(testVariables, JnProcessStatusUpdatePassword.wrongToken, x -> "abcdefgh");
		}
		this.execute(testVariables, JnProcessStatusUpdatePassword.expectedStatus, x -> token);
	}
	
	@Test
	public void recentlyLockedToken() {
		TestVariables testVariables = new TestVariables();
		String token = this.getToken(testVariables);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		for(int k = 1; k <= 2; k++) {
			this.execute(testVariables, JnProcessStatusUpdatePassword.wrongToken, x -> "abcdefgh");
		}
		this.execute(testVariables, JnProcessStatusUpdatePassword.tokenLockedRecently, x -> "abcdefgh");
		this.execute(testVariables, JnProcessStatusUpdatePassword.lockedToken, x -> token);
	}

	public String execute(TestVariables testVariables, CcpProcessStatus expectedStatus, Function<TestVariables, String> producer) {
		String tokenToValidateLogin = producer.apply(testVariables);
		String uri = "login/"
		+ testVariables.VALID_EMAIL
		+ "/password";
		
		CcpJsonRepresentation body =  testVariables.REQUEST_TO_LOGIN
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, tokenToValidateLogin);
		CcpJsonRepresentation responseJson = this.testEndpoint(expectedStatus, body, uri,  CcpHttpResponseType.singleRecord);
		return responseJson.toString();
	}

	private void sendFakeRequest(String email, String tokenToValidateLogin, CcpProcessStatus expectedStatus) {
		String uri = "login/"
		+ email
		+ "/password";
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation body =  testVariables.REQUEST_TO_LOGIN.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, tokenToValidateLogin);
		this.testEndpoint(expectedStatus, body, uri,  CcpHttpResponseType.singleRecord);
	}

	
	private String getTokenToValidateLogin(TestVariables testVariables) {
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		return "12345678";

	}
	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}


}
