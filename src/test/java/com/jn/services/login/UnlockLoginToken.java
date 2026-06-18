package com.jn.services.login;

import org.junit.Test;

import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;

public class UnlockLoginToken extends JnServiceLoginTemplateDeTestes {

	@Test(expected = CcpJsonValidationError.class)
	public void emailInvalido() {
		VariaveisParaTeste variaveisParaTeste = comEmailInvalido();
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void tokenNaoBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusTokenNotLocked);
	}

	@Test
	public void desbloqueioJaRealizado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();

		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestUnlock.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestUnlock.ENTITY.getTwinEntity().delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked);
	}

	@Test
	public void desbloqueioJaSolicitado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestUnlock.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste.REQUEST_TO_LOGIN, CcpProcessStatusDefault.OK);
	}
}
