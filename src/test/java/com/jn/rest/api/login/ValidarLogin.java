package com.jn.rest.api.login;

import java.util.function.Function;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.rest.api.commons.JnTemplateDeTestes;
import com.jn.rest.api.commons.VariaveisParaTeste;

public class ValidarLogin  extends JnTemplateDeTestes{

	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.GET;
	}

	public String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer) {
		
		CcpJsonRepresentation body = this.cadastrarSenhaParaEntrarNoSistema(variaveisParaTeste);
				;
		String sessionToken = producer.apply(variaveisParaTeste);
		String uri = "login/"
				+ variaveisParaTeste.VALID_EMAIL
				+ "/" + sessionToken
				;
		CcpJsonRepresentation testarEndpoint = this.testarEndpoint(expectedStatus, body, uri,  CcpHttpResponseType.singleRecord);
		return testarEndpoint.toString();
	}

	private CcpJsonRepresentation cadastrarSenhaParaEntrarNoSistema(VariaveisParaTeste variaveisParaTeste) {
		TelaDoCadastroDeSenha telaDoCadastroDeSenha = new TelaDoCadastroDeSenha();
		String apply = telaDoCadastroDeSenha.fluxoEsperado(variaveisParaTeste);
		CcpJsonRepresentation body =  new CcpJsonRepresentation(apply);
		return body;
	}
	private CcpJsonRepresentation executarLogin(VariaveisParaTeste variaveisParaTeste) {
		TelaQuePedeSenhaParaEntrarNoSistema tela = new TelaQuePedeSenhaParaEntrarNoSistema();
		String apply = tela.fluxoEsperado(variaveisParaTeste);
		CcpJsonRepresentation body =  new CcpJsonRepresentation(apply);
		return body;
	}
	
	@Test
	public void caminhoFeliz() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste, CcpProcessStatusDefault.SUCCESS, x -> this.cadastrarSenhaParaEntrarNoSistema(x).getDynamicVersion().getAsString("sessionToken"));
		this.execute(variaveisParaTeste, CcpProcessStatusDefault.SUCCESS, x -> this.executarLogin(x).getDynamicVersion().getAsString("sessionToken"));
	}

	@Test
	public void tokenEmFormatoInvalido() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY, x -> "123456789");
		this.execute(variaveisParaTeste, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY, x -> "1234567");
	}
	
	
	@Test
	public void tokenInvalido() {
		VariaveisParaTeste variaveisParaTeste = new VariaveisParaTeste();
		this.execute(variaveisParaTeste, CcpProcessStatusDefault.UNHAUTHORIZED, x -> "A8B7C6D5");

	}
}
