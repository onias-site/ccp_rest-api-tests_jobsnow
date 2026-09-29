package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Negócio cujas regras de validação moram em {@code RulesFieldTypeCustom}.
 */
public class BusinessFieldTypeCustom implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeCustom.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
