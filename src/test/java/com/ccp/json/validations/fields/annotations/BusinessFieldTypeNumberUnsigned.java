package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Negócio cujas regras de validação moram em {@code RulesFieldTypeNumberUnsigned}.
 */
public class BusinessFieldTypeNumberUnsigned implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeNumberUnsigned.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
