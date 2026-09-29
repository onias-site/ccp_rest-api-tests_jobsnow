	package com.jn.services.login;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.rest.api.commons.TestVariables;
import com.jn.services.JnServiceLogin;

public class ValidateLogin extends JnServiceLoginTestTemplate {

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables("onias85@gmail.com");
		CcpJsonRepresentation session = this.executeLogin(testVariables);
		this.execute(session, CcpProcessStatusDefault.OK);
	}

	private CcpJsonRepresentation executeLogin(TestVariables testVariables) {
		SavePassword savePassword = new SavePassword();
		savePassword.expectedFlow(testVariables);
		ExecuteLogout executeLogout = new ExecuteLogout();
		executeLogout.expectedFlow(testVariables);
		CcpJsonRepresentation loginResponse = JnServiceLogin.ExecuteLogin.execute(CcpOtherConstants
				.EMPTY_JSON 
				.put(JnJsonCommonsFields.email, testVariables.VALID_EMAIL)
				.put(JnJsonCommonsFields.password, TestVariables.CORRECT_PASSWORD)
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				);
		CcpFieldName field = new CcpFieldName("sessionToken");
		String sessionToken = loginResponse.getAsString(field);

		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, testVariables.VALID_EMAIL)
				.put(field, sessionToken)
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
;
		return session;
	}

	@Test(expected = CcpJsonValidationError.class)
	public void tokenInInvalidFormat() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation session = this.executeLogin(testVariables);
		this.execute(session.put(new CcpFieldName("sessionToken"), "1234567"), CcpProcessStatusDefault.UNPROCESSABLE_ENTITY); 
	}

	@Test
	public void invalidToken() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, testVariables.VALID_EMAIL)
				.put(new CcpFieldName("sessionToken"), "A8B7C6D5")
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				;
		this.execute(json, CcpProcessStatusDefault.UNHAUTHORIZED);
	}
}
