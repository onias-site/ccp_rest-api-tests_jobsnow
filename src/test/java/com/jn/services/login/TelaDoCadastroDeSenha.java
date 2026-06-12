package com.jn.services.login;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
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
import com.jn.status.login.JnProcessStatusUpdatePassword;

public class TelaDoCadastroDeSenha extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		CcpJsonRepresentation body = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, "abcdefgh");
		executarServico(JnServiceLogin.SavePassword, body, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		String token = getTokenToValidateLogin(variaveisParaTeste);
		CcpJsonRepresentation body = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, token);
		executarServico(JnServiceLogin.SavePassword, body, JnProcessStatusUpdatePassword.lockedToken);
	}

	@Test
	public void tokenFaltando() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(variaveisParaTeste.ANSWERS_JSON);
		JnEntityLoginEmail.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpJsonRepresentation body = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, "12345678");
		executarServico(JnServiceLogin.SavePassword, body, JnProcessStatusUpdatePassword.missingEmail);
	}

	@Test
	public void efetuarDesbloqueios() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginPassword.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginSessionConflict.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.fluxoEsperado(variaveisParaTeste);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.fluxoEsperado(variaveisParaTeste);
	}

	@Test(expected = CcpJsonValidationError.class)
	public void jsonInvalido() {
		executarServico(JnServiceLogin.SavePassword, CcpOtherConstants.EMPTY_JSON, JnProcessStatusUpdatePassword.invalidJson);
	}

	@Test
	public void errarParaDepoisAcertarToken() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		String token = getToken(variaveisParaTeste);
		for (int k = 1; k < 3; k++) {
			CcpJsonRepresentation body = variaveisParaTeste.REQUEST_TO_LOGIN
					.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
					.put(JnEntityLoginToken.Fields.token, "abcdefgh");
			executarServico(JnServiceLogin.SavePassword, body, JnProcessStatusUpdatePassword.wrongToken);
		}
		CcpJsonRepresentation body = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, token);
		executarServico(JnServiceLogin.SavePassword, body, JnProcessStatusUpdatePassword.expectedStatus);
	}

	@Test
	public void tokenRecemBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		String token = getToken(variaveisParaTeste);
		for (int k = 1; k <= 2; k++) {
			CcpJsonRepresentation body = variaveisParaTeste.REQUEST_TO_LOGIN
					.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
					.put(JnEntityLoginToken.Fields.token, "abcdefgh");
			executarServico(JnServiceLogin.SavePassword, body, JnProcessStatusUpdatePassword.wrongToken);
		}
		CcpJsonRepresentation bodyWrong = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, "abcdefgh");
		executarServico(JnServiceLogin.SavePassword, bodyWrong, JnProcessStatusUpdatePassword.tokenLockedRecently);
		CcpJsonRepresentation bodyRight = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, token);
		executarServico(JnServiceLogin.SavePassword, bodyRight, JnProcessStatusUpdatePassword.lockedToken);
	}

	public CcpJsonRepresentation fluxoEsperado(VariaveisParaTeste variaveisParaTeste) {
		String token = getToken(variaveisParaTeste);
		CcpJsonRepresentation body = variaveisParaTeste.REQUEST_TO_LOGIN
				.put(JnEntityLoginPassword.Fields.password, VariaveisParaTeste.CORRECT_PASSWORD)
				.put(JnEntityLoginToken.Fields.token, token);
		return executarServico(JnServiceLogin.SavePassword, body, JnProcessStatusUpdatePassword.expectedStatus);
	}

	private String getToken(VariaveisParaTeste variaveisParaTeste) {
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpJsonRepresentation createOrUpdate = JnEntityLoginToken.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		String token = createOrUpdate.getAsString(new CcpFieldName("originalToken"));
		return token;
	}

	private String getTokenToValidateLogin(VariaveisParaTeste variaveisParaTeste) {
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(variaveisParaTeste.ANSWERS_JSON);
		return "12345678";
	}
}
