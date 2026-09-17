package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Negócio cujas regras de validação moram em {@code RegrasFieldTypeNumber}.
 */
public class NegocioFieldTypeNumber implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RegrasFieldTypeNumber.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
