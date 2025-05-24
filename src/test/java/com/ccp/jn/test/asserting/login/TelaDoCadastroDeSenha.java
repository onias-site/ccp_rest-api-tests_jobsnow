package com.ccp.jn.test.asserting.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
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
import com.jn.status.login.JnStatusUpdatePassword;

public class TelaDoCadastroDeSenha extends JnTemplateDeTestes{
	@Test
	public void emailInvalido() {
		this.requisicaoFake(VariaveisParaTeste.INVALID_EMAIL, "abcdefgh", JnStatusUpdatePassword.invalidEmail);
	}

	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate( variaveisParaTeste.REQUEST_TO_LOGIN);
		String token = this.getTokenToValidateLogin(variaveisParaTeste);
		this.execute(variaveisParaTeste, JnStatusUpdatePassword.lockedToken, x -> token);
	}

	@Test
	public void tokenFaltando() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		String token = this.getTokenToValidateLogin(variaveisParaTeste);
		JnEntityLoginEmail.ENTITY.delete( variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnStatusUpdatePassword.missingEmail, x -> token);
	}

	@Test
	public void efetuarDesbloqueios() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.createOrUpdate(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.fluxoEsperado(variaveisParaTeste);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.fluxoEsperado(variaveisParaTeste);
	}

	public void fluxoEsperado(VariaveisParaTeste variaveisParaTeste) {
		String token = this.getToken(variaveisParaTeste);
		this.execute(variaveisParaTeste,JnStatusUpdatePassword.expectedStatus, x -> token);
	}

	private String getToken(VariaveisParaTeste variaveisParaTeste) {
		JnEntityLoginEmail.ENTITY.createOrUpdate( variaveisParaTeste.REQUEST_TO_LOGIN);

		CcpJsonRepresentation entityValue =  variaveisParaTeste.REQUEST_TO_LOGIN;
		CcpJsonRepresentation createOrUpdate = JnEntityLoginToken.ENTITY.createOrUpdate(entityValue);
		String token = createOrUpdate.getAsString("originalToken");
		return token;
	}

	@Test
	public void errarParaDepoisAcertarToken() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		String token = this.getToken(variaveisParaTeste);
		for(int k = 1; k < 3; k++) {
			this.execute(variaveisParaTeste, JnStatusUpdatePassword.wrongToken, x -> "abcdefgh");
		}
		this.execute(variaveisParaTeste, JnStatusUpdatePassword.expectedStatus, x -> token);
	}
	
	@Test
	public void tokenRecemBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		String token = this.getToken(variaveisParaTeste);
		for(int k = 1; k <= 3; k++) {
			this.execute(variaveisParaTeste, JnStatusUpdatePassword.wrongToken, x -> "abcdefgh");
		}
		this.execute(variaveisParaTeste, JnStatusUpdatePassword.tokenLockedRecently, x -> "abcdefgh");
		new CcpTimeDecorator().sleep(10_000);
		this.execute(variaveisParaTeste, JnStatusUpdatePassword.lockedToken, x -> token);
	}

	public String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer) {
		String tokenToValidateLogin = producer.apply(variaveisParaTeste);
		String uri = "login/"
		+ variaveisParaTeste.VALID_EMAIL
		+ "/password";
		
		CcpJsonRepresentation body =  variaveisParaTeste.REQUEST_TO_LOGIN.put(JnEntityLoginPassword.Fields.password.name(), VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token.name(), tokenToValidateLogin);
		this.testarEndpoint(expectedStatus, body, uri,  CcpHttpResponseType.singleRecord);
		String apply = producer.apply(variaveisParaTeste);
		return apply;
	}

	private void requisicaoFake(String email, String tokenToValidateLogin, CcpProcessStatus expectedStatus) {
		String uri = "login/"
		+ email
		+ "/password";
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpJsonRepresentation body =  variaveisParaTeste.REQUEST_TO_LOGIN.put(JnEntityLoginPassword.Fields.password.name(), VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token.name(), tokenToValidateLogin);
		this.testarEndpoint(expectedStatus, body, uri,  CcpHttpResponseType.singleRecord);
	}

	
	private String getTokenToValidateLogin(VariaveisParaTeste variaveisParaTeste) {
		JnEntityLoginEmail.ENTITY.createOrUpdate( variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.createOrUpdate( variaveisParaTeste.ANSWERS_JSON);
		return "";

	}
	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}


}
