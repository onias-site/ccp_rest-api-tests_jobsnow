package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifica se {@code @CcpJsonFieldTypeNumber} respeita valor mínimo, máximo, exato e a lista de
 * valores permitidos, e se recusa o que não é número.
 */
public class CcpJsonFieldTypeNumberTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioFieldTypeNumber negocio = new NegocioFieldTypeNumber();

	private CcpJsonRepresentation json(RegrasFieldTypeNumber campo, Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(campo, valor);
		return json;
	}

	@Test
	public void valorMinimoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.valorMinimo, 5.5);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorMinimoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.valorMinimo, 5.4);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumber.valorMinimo, CcpJsonFieldTypeError.doubleNumberMinValue);
	}

	@Test
	public void valorMaximoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.valorMaximo, 10.5);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorMaximoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.valorMaximo, 10.6);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumber.valorMaximo, CcpJsonFieldTypeError.doubleNumberMaxValue);
	}

	@Test
	public void valorExatoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.valorExato, 7.5);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorExatoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.valorExato, 7.6);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumber.valorExato, CcpJsonFieldTypeError.doubleNumberExactValue);
	}

	@Test
	public void valorPermitidoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.valorPermitido, 1.5);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorPermitidoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.valorPermitido, 9.9);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumber.valorPermitido, CcpJsonFieldTypeError.doubleNumberAllowed);
	}

	@Test
	public void semRestricaoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.semRestricao, 123.456);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void textoNaoNumericoTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumber.semRestricao, "abc");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumber.semRestricao, CcpJsonFieldError.incompatibleType);
	}
}
