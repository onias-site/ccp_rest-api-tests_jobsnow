package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;

/**
 * Verifica se {@code @CcpJsonFieldValidatorRequired} realmente exige a presença do campo e se a
 * ausência de um campo não anotado continua sendo aceita.
 */
public class CcpJsonFieldValidatorRequiredTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioValidatorRequired negocio = new NegocioValidatorRequired();

	private CcpJsonRepresentation jsonCompleto() {
		CcpJsonRepresentation comObrigatorio = CcpOtherConstants.EMPTY_JSON.put(RegrasValidatorRequired.obrigatorio, "valor");
		CcpJsonRepresentation completo = comObrigatorio.put(RegrasValidatorRequired.chavePrimaria, "chave");
		return completo;
	}

	@Test
	public void campoObrigatorioPresenteTest() {
		CcpJsonRepresentation json = this.jsonCompleto();
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void campoObrigatorioAusenteTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RegrasValidatorRequired.chavePrimaria, "chave");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasValidatorRequired.obrigatorio, CcpJsonFieldError.requiredFieldIsMissing);
	}

	@Test
	public void chavePrimariaAusenteTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RegrasValidatorRequired.obrigatorio, "valor");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasValidatorRequired.chavePrimaria, CcpJsonFieldError.requiredFieldIsMissing);
	}

	@Test
	public void campoOpcionalAusenteTest() {
		CcpJsonRepresentation json = this.jsonCompleto();
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void campoOpcionalPresenteTest() {
		CcpJsonRepresentation jsonCompleto = this.jsonCompleto();
		CcpJsonRepresentation json = jsonCompleto.put(RegrasValidatorRequired.opcional, "outro");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}
}
