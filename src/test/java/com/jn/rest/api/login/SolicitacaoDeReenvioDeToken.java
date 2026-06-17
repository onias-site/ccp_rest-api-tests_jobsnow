package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.rest.api.commons.JnTemplateDeTestes;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.status.login.JnProcessStatusUnlockLoginToken;
import com.jn.utils.JnLanguage;

public class SolicitacaoDeReenvioDeToken extends JnTemplateDeTestes {

	@Test
	public void reenvioJaFoiFeito() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		CcpEntity twinEntity = JnEntityLoginTokenRequestResend.ENTITY.getTwinEntity();
		twinEntity.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusUnlockLoginToken.statusTokenAlredyResent);
	}

	@Test
	public void reenvioJaSolicitado() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		JnEntityLoginTokenRequestResend.ENTITY.save(variaveisParaTeste.REQUEST_TO_LOGIN);
		this.execute(variaveisParaTeste, JnProcessStatusUnlockLoginToken.statusAlreadyRequested);
	}

	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste, CcpProcessStatusDefault.OK);
	}

	public String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer) {
		this.solicitarReenvio(variaveisParaTeste.VALID_EMAIL, expectedStatus);
		String apply = producer.apply(variaveisParaTeste);
		return apply;
	}

	private void solicitarReenvio(String email, CcpProcessStatus expectedStatus) {
		String uri = "login/" + email + "/token/language/" + JnLanguage.portuguese.name() + "/request/again";
		this.testarEndpoint(uri, expectedStatus);
	}

	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}
}
