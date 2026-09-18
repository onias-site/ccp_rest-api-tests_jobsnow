package com.ccp.json.validations.fields.annotations;

import java.util.Arrays;
import java.util.List;

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

	/**
	 * Mesma forma de {@code JnEntityJobsnowError.Fields.cause}: o {@code nonRepeatedItems = false} mora
	 * no campo de destino enquanto o tipo vem da classe de origem.
	 */
	@Test
	public void nonRepeatedItemsFalseConviveComValidacoesCopiadasTest() {
		List<String> valor = Arrays.asList("a", "a");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RegrasCopyFieldValidationsFrom.causaComRepetidos, valor);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void nonRepeatedItemsPadraoConviveComValidacoesCopiadasTest() {
		List<String> valor = Arrays.asList("a", "a");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RegrasCopyFieldValidationsFrom.causaSemRepetidos, valor);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasCopyFieldValidationsFrom.causaSemRepetidos, CcpJsonFieldTypeError.arrayNonReapeted);
	}
}
