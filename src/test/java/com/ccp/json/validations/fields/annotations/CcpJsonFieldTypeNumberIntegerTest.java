package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifica se {@code @CcpJsonFieldTypeNumberInteger} respeita valor mínimo, máximo, exato e a
 * lista de valores permitidos, e se recusa o que não é inteiro.
 */
public class CcpJsonFieldTypeNumberIntegerTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioFieldTypeNumberInteger negocio = new NegocioFieldTypeNumberInteger();

	private CcpJsonRepresentation json(RegrasFieldTypeNumberInteger campo, Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(campo, valor);
		return json;
	}

	@Test
	public void valorMinimoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.valorMinimo, 10);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorMinimoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.valorMinimo, 9);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberInteger.valorMinimo, CcpJsonFieldTypeError.longNumberMinValue);
	}

	@Test
	public void valorMaximoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.valorMaximo, 20);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorMaximoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.valorMaximo, 21);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberInteger.valorMaximo, CcpJsonFieldTypeError.longNumberMaxValue);
	}

	@Test
	public void valorExatoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.valorExato, 1500);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorExatoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.valorExato, 1501);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberInteger.valorExato, CcpJsonFieldTypeError.longNumberExactValue);
	}

	@Test
	public void valorPermitidoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.valorPermitido, 2);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorPermitidoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.valorPermitido, 9);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberInteger.valorPermitido, CcpJsonFieldTypeError.longNumberAllowed);
	}

	@Test
	public void numeroNegativoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.semRestricao, -42);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void textoNaoNumericoTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberInteger.semRestricao, "abc");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberInteger.semRestricao, CcpJsonFieldError.incompatibleType);
	}
}
