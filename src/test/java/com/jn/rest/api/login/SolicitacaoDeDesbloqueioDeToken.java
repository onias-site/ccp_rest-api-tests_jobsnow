package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.rest.api.commons.JnTemplateDeTestes;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;

public class SolicitacaoDeDesbloqueioDeToken extends JnTemplateDeTestes {

	@Test
	public void tokenNaoBloqueado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste, JnProcessStatusUnlockLoginToken.statusTokenNotLocked);
	}

	@Test
	public void desbloqueioJaRealizado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, CcpProcessStatusDefault.OK);
		JnEntityLoginTokenRequestUnlock.ENTITY.delete(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusUnlockLoginToken.statusTokenAlredyUnlocked);
	}

	@Test
	public void desbloqueioJaSolicitado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		JnEntityLoginTokenRequestUnlock.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginToken.ENTITY.getTwinEntity().save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, CcpProcessStatusDefault.OK);
	}

	public String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer) {
		this.solicitarDesbloqueio(variaveisParaTeste.VALID_EMAIL, expectedStatus);
		String apply = producer.apply(variaveisParaTeste);
		return apply;
	}

	private void solicitarDesbloqueio(String email, CcpProcessStatus expectedStatus) {
		String uri = "login/" + email + "/token/request/unlocking";
		this.testarEndpoint(uri, expectedStatus);
	}

	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}
}
