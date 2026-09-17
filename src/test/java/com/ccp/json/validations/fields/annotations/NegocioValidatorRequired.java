package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Negócio cujas regras de validação moram em {@code RegrasValidatorRequired}.
 */
public class NegocioValidatorRequired implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RegrasValidatorRequired.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
