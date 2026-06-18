package com.jn.services.login;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.status.login.JnProcessStatusExecuteLogout;

public class ExecuteLogout extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void usuarioNaoLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExecuteLogout.missingLogin);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste("onias85@gmail.com");
		this.fluxoEsperado(variaveisParaTeste);
	}

	public void fluxoEsperado(VariaveisParaTeste variaveisParaTeste) {
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpJsonRepresentation withToken = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginSessionValidation.Fields.token, "12345678");
		JnEntityLoginSessionValidation.ENTITY.save(withToken);
		this.execute(withToken, JnProcessStatusExecuteLogout.expectedStatus);
	}
}
