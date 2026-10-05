package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;

/**
 * Verifies that {@code @CcpJsonFieldValidatorRequired} really demands the presence of the field and that the
 * absence of a field without the annotation is still accepted.
 */
public class CcpJsonFieldValidatorRequiredTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessValidatorRequired business = new BusinessValidatorRequired();

	private CcpJsonRepresentation completeJson() {
		CcpJsonRepresentation withRequired = CcpOtherConstants.EMPTY_JSON.put(RulesValidatorRequired.obrigatorio, "valor");
		CcpJsonRepresentation complete = withRequired.put(RulesValidatorRequired.chavePrimaria, "chave");
		return complete;
	}

	@Test
	public void requiredFieldPresentTest() {
		CcpJsonRepresentation json = this.completeJson();
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void requiredFieldAbsentTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesValidatorRequired.chavePrimaria, "chave");
		FieldValidation.refuses(this.business, json, RulesValidatorRequired.obrigatorio, CcpJsonFieldError.requiredFieldIsMissing);
	}

	@Test
	public void primaryKeyAbsentTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesValidatorRequired.obrigatorio, "valor");
		FieldValidation.refuses(this.business, json, RulesValidatorRequired.chavePrimaria, CcpJsonFieldError.requiredFieldIsMissing);
	}

	@Test
	public void optionalFieldAbsentTest() {
		CcpJsonRepresentation json = this.completeJson();
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void optionalFieldPresentTest() {
		CcpJsonRepresentation completeJson = this.completeJson();
		CcpJsonRepresentation json = completeJson.put(RulesValidatorRequired.opcional, "outro");
		FieldValidation.accepts(this.business, json);
	}
}
