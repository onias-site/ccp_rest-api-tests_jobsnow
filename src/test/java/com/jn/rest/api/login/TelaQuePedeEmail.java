package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.JnTemplateDeTestes;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.status.login.JnProcessStatusExistsLoginEmail;

public class TelaQuePedeEmail extends JnTemplateDeTestes{
	
	@Test
	public void emailInvalido() {
		this.verificarExistenciaDeEmail(JnProcessStatusExistsLoginEmail.invalidEmail, VariaveisParaTeste.INVALID_EMAIL);
	}
	
	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.lockedToken);
	}
	
	@Test
	public void tokenFaltando() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.missingEmail);		
	}
	
	@Test
	public void senhaBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN); 
		this.execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.lockedPassword);		
	}
	
	@Test
	public void usuarioJaLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste("onias85@gmail.com");
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.loginConflict);		
	}
	
	@Test
	public void faltandoCadastrarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginAnswers.ENTITY.createOrUpdate(variaveisParaTeste.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.missingPassword);		
	}
	
	@Test
	public void faltandoPreRegistro() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.missingAnswers);		
	}
	
	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.createOrUpdate(variaveisParaTeste.ANSWERS_JSON);
		JnEntityLoginPassword.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusExistsLoginEmail.expectedStatus);		
	}
	
	public String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer) {
		this.verificarExistenciaDeEmail(expectedStatus, variaveisParaTeste.VALID_EMAIL);
		String apply = producer.apply(variaveisParaTeste);
		return apply;
	}	
	
	private void verificarExistenciaDeEmail(CcpProcessStatus expectedStatus, String email) {
		String uri = "login/"
				+ email
				+ "/token";

		this.testarEndpoint(uri, expectedStatus);
	}

	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.HEAD;
	}
}
