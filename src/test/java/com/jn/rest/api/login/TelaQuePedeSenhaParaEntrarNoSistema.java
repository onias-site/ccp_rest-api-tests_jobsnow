package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.JnTemplateDeTestes;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;

public class TelaQuePedeSenhaParaEntrarNoSistema extends JnTemplateDeTestes{

	@Test
	public void emailInvalido() {
		this.executarLogin(VariaveisParaTeste.INVALID_EMAIL, VariaveisParaTeste.CORRECT_PASSWORD, JnProcessStatusExecuteLogin.invalidEmail);
	}

	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.lockedToken, x -> VariaveisParaTeste.CORRECT_PASSWORD);
	}

	@Test
	public void tokenFaltando() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.missingSavingEmail, x -> VariaveisParaTeste.CORRECT_PASSWORD);
	}

	@Test
	public void faltandoCadastrarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.createOrUpdate(variaveisParaTeste.ANSWERS_JSON);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.missingSavePassword, x-> VariaveisParaTeste.CORRECT_PASSWORD);
	}

	@Test
	public void senhaBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.lockedPassword, x -> VariaveisParaTeste.CORRECT_PASSWORD);
	}

	@Test
	public void usuarioJaLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.loginConflict, x -> VariaveisParaTeste.CORRECT_PASSWORD);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste("onias85@gmail.com");
		new TelaDoCadastroDeSenha().fluxoEsperado(variaveisParaTeste);;
		new CcpTimeDecorator().sleep(10000); 
		JnEntityLoginSessionConflict.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.expectedStatus, x -> VariaveisParaTeste.CORRECT_PASSWORD);
	}
 
	 
	@Test
	public void bloquearSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		TelaDoCadastroDeSenha telaDoCadastroDeSenha = new TelaDoCadastroDeSenha();
		telaDoCadastroDeSenha.fluxoEsperado(variaveisParaTeste);
		new CcpTimeDecorator().sleep(10000);
		JnEntityLoginSessionConflict.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.wrongPassword, x -> VariaveisParaTeste.WRONG_PASSWORD);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.wrongPassword, x -> VariaveisParaTeste.WRONG_PASSWORD);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.wrongPassword, x -> VariaveisParaTeste.WRONG_PASSWORD);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.passwordLockedRecently, x -> VariaveisParaTeste.WRONG_PASSWORD);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.lockedPassword, x -> VariaveisParaTeste.WRONG_PASSWORD);
		new CcpTimeDecorator().sleep(10_000);
		new TelaQuePedeEmail().execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.lockedPassword);
	}
	
	@Test
	public void errarParaDepoisAcertarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		new TelaDoCadastroDeSenha().fluxoEsperado(variaveisParaTeste);
		new CcpTimeDecorator().sleep(10000);
		JnEntityLoginSessionConflict.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		
		for(int k = 1; k < 3; k++) {
			this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.wrongPassword, x -> VariaveisParaTeste.WRONG_PASSWORD);
		}
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.expectedStatus, x -> VariaveisParaTeste.CORRECT_PASSWORD);
	}
	
	@Test
	public void senhaRecemBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		new TelaDoCadastroDeSenha().fluxoEsperado(variaveisParaTeste);;
		new CcpTimeDecorator().sleep(10000);
		JnEntityLoginSessionConflict.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		
		for(int k = 1; k <= 3; k++) {
			this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.wrongPassword, x -> VariaveisParaTeste.WRONG_PASSWORD);
		}
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.passwordLockedRecently, x -> VariaveisParaTeste.WRONG_PASSWORD);
		this.execute(variaveisParaTeste, JnProcessStatusExecuteLogin.lockedPassword, x -> VariaveisParaTeste.CORRECT_PASSWORD);
	}

	public String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer) {
		String senha = producer.apply(variaveisParaTeste);
		this.executarLogin(variaveisParaTeste.VALID_EMAIL, senha, expectedStatus);
		String apply = producer.apply(variaveisParaTeste);
		return apply;
	}
	
	private void executarLogin(String email, String senha, CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation body = CcpOtherConstants.EMPTY_JSON.put(JnEntityLoginPassword.Fields.password, senha);
		String uri = "login/"
		+ email;
		this.testarEndpoint(expectedStatus, body, uri, CcpHttpResponseType.singleRecord);
	}

	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}
	
}
