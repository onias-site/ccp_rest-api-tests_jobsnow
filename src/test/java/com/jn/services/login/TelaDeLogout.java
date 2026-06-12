package com.jn.services.login;

import org.junit.Test;

import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;
import com.jn.status.login.JnProcessStatusExecuteLogout;

public class TelaDeLogout extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		executarServico(JnServiceLogin.ExecuteLogout, variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void usuarioNaoLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		executarServico(JnServiceLogin.ExecuteLogout, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExecuteLogout.missingLogin);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		com.ccp.decorators.CcpJsonRepresentation withToken = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginSessionValidation.Fields.token, "12345678");
		JnEntityLoginSessionValidation.ENTITY.save(withToken);
		executarServico(JnServiceLogin.ExecuteLogout, withToken, JnProcessStatusExecuteLogout.expectedStatus);
	}
}
