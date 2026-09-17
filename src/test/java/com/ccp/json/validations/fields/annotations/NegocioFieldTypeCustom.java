package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Negócio cujas regras de validação moram em {@code RegrasFieldTypeCustom}.
 */
public class NegocioFieldTypeCustom implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RegrasFieldTypeCustom.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
