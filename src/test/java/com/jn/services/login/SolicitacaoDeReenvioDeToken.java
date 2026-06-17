package com.jn.services.login;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;

public class SolicitacaoDeReenvioDeToken extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		executarServico(JnServiceLogin.ResendLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void reenvioJaFoiFeito() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity twinEntity = JnEntityLoginTokenRequestResend.ENTITY.getTwinEntity();
		twinEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.ResendLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusTokenAlredyResent);
	}

	@Test
	public void reenvioJaSolicitado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginTokenRequestResend.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.ResendLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		executarServico(JnServiceLogin.ResendLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.OK);
	}
}
