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

	private final BusinessValidatorRequired business = new BusinessValidatorRequired();

	private CcpJsonRepresentation jsonCompleto() {
		CcpJsonRepresentation comObrigatorio = CcpOtherConstants.EMPTY_JSON.put(RulesValidatorRequired.obrigatorio, "valor");
		CcpJsonRepresentation completo = comObrigatorio.put(RulesValidatorRequired.chavePrimaria, "chave");
		return completo;
	}

	@Test
	public void campoObrigatorioPresenteTest() {
		CcpJsonRepresentation json = this.jsonCompleto();
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void campoObrigatorioAusenteTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesValidatorRequired.chavePrimaria, "chave");
		FieldValidation.refuses(this.business, json, RulesValidatorRequired.obrigatorio, CcpJsonFieldError.requiredFieldIsMissing);
	}

	@Test
	public void chavePrimariaAusenteTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesValidatorRequired.obrigatorio, "valor");
		FieldValidation.refuses(this.business, json, RulesValidatorRequired.chavePrimaria, CcpJsonFieldError.requiredFieldIsMissing);
	}

	@Test
	public void campoOpcionalAusenteTest() {
		CcpJsonRepresentation json = this.jsonCompleto();
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void campoOpcionalPresenteTest() {
		CcpJsonRepresentation jsonCompleto = this.jsonCompleto();
		CcpJsonRepresentation json = jsonCompleto.put(RulesValidatorRequired.opcional, "outro");
		FieldValidation.accepts(this.business, json);
	}
}
