package com.jn.services.login;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;

public class SolicitacaoDeDesbloqueioDeToken extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		executarServico(JnServiceLogin.UnlockLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void tokenNaoBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		executarServico(JnServiceLogin.UnlockLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusTokenNotLocked);
	}

	@Test
	public void desbloqueioJaRealizado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		CcpEntity twinEntity = JnEntityLoginTokenRequestUnlock.ENTITY.getTwinEntity();
		twinEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.UnlockLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked);
	}

	@Test
	public void desbloqueioJaSolicitado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestUnlock.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.UnlockLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		executarServico(JnServiceLogin.UnlockLoginToken, variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.OK);
	}
}
