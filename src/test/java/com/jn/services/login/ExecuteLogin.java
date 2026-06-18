package com.jn.services.login;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;

public class ExecuteLogin extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.lockedToken);
	}

	@Test
	public void tokenFaltando() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.missingSavingEmail);
	}

	@Test
	public void faltandoCadastrarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		com.jn.entities.JnEntityLoginAnswers.ENTITY.save(variaveisParaTeste.ANSWERS_JSON);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.missingSavePassword);
	}

	@Test
	public void senhaBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.lockedPassword);
	}

	@Test
	public void usuarioJaLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.loginConflict);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.fluxoEsperado(variaveisParaTeste);
	}

	@Test
	public void bloquearSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		new SavePassword().fluxoEsperado(variaveisParaTeste);
		JnEntityLoginSessionConflict.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.WRONG_PASSWORD, JnProcessStatusExecuteLogin.wrongPassword);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.WRONG_PASSWORD, JnProcessStatusExecuteLogin.wrongPassword);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.WRONG_PASSWORD, JnProcessStatusExecuteLogin.passwordLockedRecently);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.WRONG_PASSWORD, JnProcessStatusExecuteLogin.lockedPassword);
		new ExistsLoginEmail().execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.lockedPassword);
	}

	@Test
	public void errarParaDepoisAcertarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		new SavePassword().fluxoEsperado(variaveisParaTeste);
		JnEntityLoginSessionConflict.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		for (int k = 1; k < 3; k++) {
			executarLogin(variaveisParaTeste, VariaveisParaTeste.WRONG_PASSWORD, JnProcessStatusExecuteLogin.wrongPassword);
		}
		executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.expectedStatus);
	}

	@Test
	public void senhaRecemBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		SavePassword savePassword = new SavePassword();
		savePassword.fluxoEsperado(variaveisParaTeste);
		JnEntityLoginSessionConflict.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		for (int k = 1; k <= 2; k++) {
			executarLogin(variaveisParaTeste, VariaveisParaTeste.WRONG_PASSWORD, JnProcessStatusExecuteLogin.wrongPassword);
		}
		executarLogin(variaveisParaTeste, VariaveisParaTeste.WRONG_PASSWORD, JnProcessStatusExecuteLogin.passwordLockedRecently);
		executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.lockedPassword);
	}

	public CcpJsonRepresentation fluxoEsperado(VariaveisParaTeste variaveisParaTeste) {
		SavePassword savePassword = new SavePassword();
		savePassword.fluxoEsperado(variaveisParaTeste);
		JnEntityLoginSessionConflict.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpJsonRepresentation executarLogin = this.executarLogin(variaveisParaTeste, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.expectedStatus);
		return executarLogin;
	}

	private CcpJsonRepresentation executarLogin(VariaveisParaTeste variaveisParaTeste, String senha, com.ccp.process.CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation json = variaveisParaTeste.REQUEST_TO_LOGIN.put(JnEntityLoginPassword.Fields.password, senha);
		CcpJsonRepresentation executarServico = this.execute(json, expectedStatus);
		return executarServico;
	}
}
