package com.jn.services.login;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;

public class TelaQuePedeEmail extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExistsLoginEmail.lockedToken);
	}

	@Test
	public void tokenFaltando() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExistsLoginEmail.missingEmail);
	}

	@Test
	public void senhaBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExistsLoginEmail.lockedPassword);
	}

	@Test
	public void usuarioJaLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExistsLoginEmail.loginConflict);
	}

	@Test
	public void faltandoCadastrarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginAnswers.ENTITY.save(variaveisParaTeste.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExistsLoginEmail.missingPassword);
	}

	@Test
	public void faltandoPreRegistro() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExistsLoginEmail.missingAnswers);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(variaveisParaTeste.ANSWERS_JSON);
		JnEntityLoginPassword.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusExistsLoginEmail.expectedStatus);
	}

	public void execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus) {
		executarServico(JnServiceLogin.ExistsLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, expectedStatus);
	}
}
