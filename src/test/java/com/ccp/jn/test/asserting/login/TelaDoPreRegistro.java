package com.ccp.jn.test.asserting.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.http.CcpHttpMethods;
import com.ccp.jn.test.asserting.JnTemplateDeTestes;
import com.ccp.jn.test.asserting.VariaveisParaTeste;
import com.ccp.process.CcpProcessStatus;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.status.login.JnStatusSaveAnswers;

public class TelaDoPreRegistro  extends JnTemplateDeTestes{

	@Test
	public void emailInvalido() {
		this.cadastrarPreRegistration(VariaveisParaTeste.INVALID_EMAIL, JnStatusSaveAnswers.invalidEmail);
	}
 
	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusSaveAnswers.lockedToken);
	}

	@Test
	public void tokenFaltando() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste, JnStatusSaveAnswers.tokenFaltando);
	}

	@Test
	public void usuarioJaLogado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusSaveAnswers.loginConflict);
	}

	@Test
	public void faltandoCadastrarSenha() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginAnswers.ENTITY.createOrUpdate(variaveisParaTeste.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusSaveAnswers.missingPassword);
	}

	@Test
	public void senhaBloqueada() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusSaveAnswers.lockedPassword);
	}
	
	@Test
	public void caminhoFeliz() { 
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginPassword.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusSaveAnswers.expectedStatus); 
	}
	//
	public String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer) {
		this.cadastrarPreRegistration(variaveisParaTeste.VALID_EMAIL, expectedStatus);
		String apply = producer.apply(variaveisParaTeste);
		return apply;
	}
	
	private void cadastrarPreRegistration(String email, CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation body = CcpOtherConstants
				.EMPTY_JSON
				.put(JnEntityLoginAnswers.Fields.goal.name(), "jobs")
				.put(JnEntityLoginAnswers.Fields.channel.name(), "linkedin")
				;
		String uri = "login/"+ email 	+ "/pre-registration";
		this.testarEndpoint(expectedStatus, body, uri,  CcpHttpResponseType.singleRecord);
	}

	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}

}
