package com.jn.services.login;

import org.junit.Test;

import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;

public class ResendLoginToken extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void reenvioJaFoiFeito() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestResend.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestResend.ENTITY.getTwinEntity().delete(variaveisParaTeste.REQUEST_TO_LOGIN);

		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusTokenAlredyResent);
	}

	@Test
	public void tokenNaoExiste() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginTokenRequestResend.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestResend.ENTITY.getTwinEntity().delete(variaveisParaTeste.REQUEST_TO_LOGIN);

		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusTokenNotExists);
	}

	@Test
	public void reenvioJaSolicitado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestResend.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.OK);
	}
}
