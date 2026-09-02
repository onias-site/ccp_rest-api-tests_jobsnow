	package com.jn.services.login;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;

public class ValidateLogin extends JnServiceLoginTemplateDeTestes {

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste("onias85@gmail.com");
		CcpJsonRepresentation session = this.executeLogin(variaveisParaTeste);
		this.execute(session, CcpProcessStatusDefault.OK);
	}

	private CcpJsonRepresentation executeLogin(VariaveisParaTeste variaveisParaTeste) {
		SavePassword savePassword = new SavePassword();
		savePassword.fluxoEsperado(variaveisParaTeste);
		ExecuteLogout executeLogout = new ExecuteLogout();
		executeLogout.fluxoEsperado(variaveisParaTeste);
		CcpJsonRepresentation login = JnServiceLogin.ExecuteLogin.execute(CcpOtherConstants
				.EMPTY_JSON 
				.put(JnJsonCommonsFields.email, variaveisParaTeste.VALID_EMAIL)
				.put(JnJsonCommonsFields.password, VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				);
		CcpFieldName field = new CcpFieldName("sessionToken");
		String sessionToken = login.getAsString(field);

		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, variaveisParaTeste.VALID_EMAIL)
				.put(field, sessionToken)
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
;
		return session;
	}

	@Test(expected = CcpJsonValidationError.class)
	public void tokenEmFormatoInvalido() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpJsonRepresentation session = this.executeLogin(variaveisParaTeste);
		this.execute(session.put(new CcpFieldName("sessionToken"), "1234567"), CcpProcessStatusDefault.UNPROCESSABLE_ENTITY); 
	}

	@Test
	public void tokenInvalido() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, variaveisParaTeste.VALID_EMAIL)
				.put(new CcpFieldName("sessionToken"), "A8B7C6D5")
				.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				;
		this.execute(json, CcpProcessStatusDefault.UNHAUTHORIZED);
	}
}
