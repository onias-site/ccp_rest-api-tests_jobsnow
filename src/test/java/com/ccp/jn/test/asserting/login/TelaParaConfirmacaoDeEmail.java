package com.ccp.jn.test.asserting.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.http.CcpHttpMethods;
import com.ccp.jn.test.asserting.JnTemplateDeTestes;
import com.ccp.jn.test.asserting.VariaveisParaTeste;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.status.login.JnStatusCreateLoginEmail;

public class TelaParaConfirmacaoDeEmail  extends JnTemplateDeTestes{

	@Test
	public void emailInvalido() {
		this.confirmarEmail(VariaveisParaTeste.INVALID_EMAIL, JnStatusCreateLoginEmail.invalidEmail);
	}
	
	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusCreateLoginEmail.lockedToken);
	}
	
	@Test
	public void senhaBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusCreateLoginEmail.lockedPassword);
	}
	
	@Test
	public void usuarioJaLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginSessionConflict.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusCreateLoginEmail.loginConflict);
	}
	
	@Test
	public void faltandoCadastrarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginAnswers.ENTITY.createOrUpdate(variaveisParaTeste.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusCreateLoginEmail.missingSavePassword);
	}
	
	@Test
	public void faltandoPreRegistro() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginPassword.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusCreateLoginEmail.missingSaveAnswers);
	}
	
	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginPassword.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.createOrUpdate(variaveisParaTeste.ANSWERS_JSON);
		this.execute(variaveisParaTeste, JnStatusCreateLoginEmail.expectedStatus);
	}
	//
	public String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer) {
		this.confirmarEmail(variaveisParaTeste.VALID_EMAIL, expectedStatus);
		String apply = producer.apply(variaveisParaTeste);
		return apply;
	}
	
	private CcpJsonRepresentation confirmarEmail(String email, CcpProcessStatus expectedStatus) {
		
		String uri = "/login/"
		+ email
		+ "/token";
		CcpJsonRepresentation asSingleJson = this.testarEndpoint(uri, expectedStatus);
		return asSingleJson;
	}


	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}

}
