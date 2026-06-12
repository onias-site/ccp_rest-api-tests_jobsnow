package com.jn.services.login;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;
import com.jn.status.login.JnProcessStatusCreateLoginEmail;

public class TelaParaConfirmacaoDeEmail extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		executarServico(JnServiceLogin.CreateLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.CreateLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusCreateLoginEmail.lockedToken);
	}

	@Test
	public void senhaBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.CreateLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusCreateLoginEmail.lockedPassword);
	}

	@Test
	public void usuarioJaLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginSessionConflict.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.CreateLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusCreateLoginEmail.loginConflict);
	}

	@Test
	public void faltandoCadastrarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginAnswers.ENTITY.save(variaveisParaTeste.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.CreateLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusCreateLoginEmail.missingSavePassword);
	}

	@Test
	public void faltandoPreRegistro() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginPassword.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.CreateLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusCreateLoginEmail.missingSaveAnswers);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginPassword.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(variaveisParaTeste.ANSWERS_JSON);
		executarServico(JnServiceLogin.CreateLoginEmail, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusCreateLoginEmail.expectedStatus);
	}
}
