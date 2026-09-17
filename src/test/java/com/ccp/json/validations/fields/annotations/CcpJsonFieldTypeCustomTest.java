package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;

/**
 * Verifica se {@code @CcpJsonFieldTypeCustom} instancia a classe indicada e aplica a regra de
 * compatibilidade que ela define.
 */
public class CcpJsonFieldTypeCustomTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioFieldTypeCustom negocio = new NegocioFieldTypeCustom();

	private CcpJsonRepresentation json(Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RegrasFieldTypeCustom.apenasVogais, valor);
		return json;
	}

	@Test
	public void apenasVogaisAceitaTest() {
		CcpJsonRepresentation json = this.json("aeiou");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void comConsoanteRecusaTest() {
		CcpJsonRepresentation json = this.json("aeixou");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeCustom.apenasVogais, CcpJsonFieldError.incompatibleType);
	}
}
