package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Negócio cujas regras de validação moram em {@code RulesFieldTypeBoolean}.
 */
public class BusinessFieldTypeBoolean implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeBoolean.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
