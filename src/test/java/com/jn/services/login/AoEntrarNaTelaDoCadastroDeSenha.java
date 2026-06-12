package com.jn.services.login;

import org.junit.Test;

import com.ccp.decorators.CcpFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;
import com.jn.status.login.JnProcessStatusCreateLoginToken;
import com.jn.utils.JnLanguage;

public class AoEntrarNaTelaDoCadastroDeSenha extends JnServiceLoginTemplateDeTestes {

	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		executarServico(JnServiceLogin.CreateLoginToken, comJsonDeToken(variaveisParaTeste), CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void tokenBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.CreateLoginToken, comJsonDeToken(variaveisParaTeste), JnProcessStatusCreateLoginToken.statusLockedToken);
	}

	@Test
	public void tokenFaltando() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		executarServico(JnServiceLogin.CreateLoginToken, comJsonDeToken(variaveisParaTeste), JnProcessStatusCreateLoginToken.statusMissingEmail);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginEmail.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(variaveisParaTeste.ANSWERS_JSON);
		executarServico(JnServiceLogin.CreateLoginToken, comJsonDeToken(variaveisParaTeste), JnProcessStatusCreateLoginToken.expectedStatus);
	}

	private com.ccp.decorators.CcpJsonRepresentation comJsonDeToken(VariaveisParaTeste variaveisParaTeste) {
		return variaveisParaTeste.REQUEST_TO_LOGIN.put(new CcpFieldName("language"), JnLanguage.portuguese.name());
	}
}
