package com.jn.services.login;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;

public class ValidarLogin extends JnServiceLoginTemplateDeTestes {

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste("onias85@gmail.com");

		CcpJsonRepresentation savePasswordResult = new TelaDoCadastroDeSenha().fluxoEsperado(variaveisParaTeste);
		String sessionToken1 = savePasswordResult.getAsString(new CcpFieldName("sessionToken"));
		CcpJsonRepresentation json1 = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginAnswers.Fields.email, variaveisParaTeste.VALID_EMAIL)
				.put(JnEntityLoginSessionValidation.Fields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnEntityLoginSessionValidation.Fields.ip, "127.0.0.1")
				.put(new CcpFieldName("sessionToken"), sessionToken1);
		this.executarServico(JnServiceLogin.ValidateLogin, json1, CcpProcessStatusDefault.OK);
  
		CcpJsonRepresentation loginResult = new TelaQuePedeSenhaParaEntrarNoSistema().fluxoEsperado(variaveisParaTeste);
		String sessionToken2 = loginResult.getAsString(new CcpFieldName("sessionToken"));
		CcpJsonRepresentation json2 = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginAnswers.Fields.email, variaveisParaTeste.VALID_EMAIL)
				.put(new CcpFieldName("sessionToken"), sessionToken2)
				.put(JnEntityLoginSessionValidation.Fields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnEntityLoginSessionValidation.Fields.ip, "127.0.0.1")
;
		executarServico(JnServiceLogin.ValidateLogin, json2
				, CcpProcessStatusDefault.OK);
	}

	@Test
	public void tokenEmFormatoInvalido() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpJsonRepresentation json9Chars = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginAnswers.Fields.email, variaveisParaTeste.VALID_EMAIL)
				.put(new CcpFieldName("sessionToken"), "123456789")
				.put(JnEntityLoginSessionValidation.Fields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnEntityLoginSessionValidation.Fields.ip, "127.0.0.1")
				;
		executarServico(JnServiceLogin.ValidateLogin, json9Chars, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
		CcpJsonRepresentation json7Chars = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginAnswers.Fields.email, variaveisParaTeste.VALID_EMAIL)
				.put(new CcpFieldName("sessionToken"), "1234567")
				.put(JnEntityLoginSessionValidation.Fields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnEntityLoginSessionValidation.Fields.ip, "127.0.0.1")
				;
		executarServico(JnServiceLogin.ValidateLogin, json7Chars, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void tokenInvalido() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginAnswers.Fields.email, variaveisParaTeste.VALID_EMAIL)
				.put(new CcpFieldName("sessionToken"), "A8B7C6D5")
				.put(JnEntityLoginSessionValidation.Fields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JnEntityLoginSessionValidation.Fields.ip, "127.0.0.1")
				;
		executarServico(JnServiceLogin.ValidateLogin, json, CcpProcessStatusDefault.UNHAUTHORIZED);
	}
}
