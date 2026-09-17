package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifica se {@code @CcpJsonFieldTypeNumberUnsigned} respeita as restrições numéricas e, o que é
 * próprio deste tipo, recusa valores negativos.
 */
public class CcpJsonFieldTypeNumberUnsignedTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioFieldTypeNumberUnsigned negocio = new NegocioFieldTypeNumberUnsigned();

	private CcpJsonRepresentation json(RegrasFieldTypeNumberUnsigned campo, Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(campo, valor);
		return json;
	}

	@Test
	public void valorMinimoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.valorMinimo, 10);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorMinimoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.valorMinimo, 9);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberUnsigned.valorMinimo, CcpJsonFieldTypeError.unsignedNumberMinValue);
	}

	@Test
	public void valorMaximoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.valorMaximo, 20);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorMaximoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.valorMaximo, 21);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberUnsigned.valorMaximo, CcpJsonFieldTypeError.unsignedNumberMaxValue);
	}

	@Test
	public void valorExatoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.valorExato, 1500);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorExatoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.valorExato, 1501);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberUnsigned.valorExato, CcpJsonFieldTypeError.unsignedNumberExactValue);
	}

	@Test
	public void valorPermitidoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.valorPermitido, 2);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorPermitidoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.valorPermitido, 9);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberUnsigned.valorPermitido, CcpJsonFieldTypeError.unsignedNumberAllowed);
	}

	@Test
	public void zeroAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.semRestricao, 0);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void numeroNegativoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNumberUnsigned.semRestricao, -1);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNumberUnsigned.semRestricao, CcpJsonFieldError.incompatibleType);
	}
}
