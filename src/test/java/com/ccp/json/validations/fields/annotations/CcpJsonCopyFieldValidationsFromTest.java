package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifica se {@code @CcpJsonCopyFieldValidationsFrom} realmente faz o campo herdar as regras do
 * campo homônimo da classe de origem, mesmo não tendo nenhuma anotação de validação própria.
 */
public class CcpJsonCopyFieldValidationsFromTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioCopyFieldValidationsFrom negocio = new NegocioCopyFieldValidationsFrom();

	private CcpJsonRepresentation json(Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RegrasCopyFieldValidationsFrom.apelido, valor);
		return json;
	}

	@Test
	public void valorDentroDasRegrasCopiadasTest() {
		CcpJsonRepresentation json = this.json("joao");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void comprimentoMinimoCopiadoTest() {
		CcpJsonRepresentation json = this.json("ab");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasCopyFieldValidationsFrom.apelido, CcpJsonFieldTypeError.stringMinLength);
	}

	@Test
	public void comprimentoMaximoCopiadoTest() {
		CcpJsonRepresentation json = this.json("abcdefghij");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasCopyFieldValidationsFrom.apelido, CcpJsonFieldTypeError.stringMaxLength);
	}
}
