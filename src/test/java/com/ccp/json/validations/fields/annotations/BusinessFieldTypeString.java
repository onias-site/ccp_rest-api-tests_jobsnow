package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Negócio cujas regras de validação moram em {@code RulesFieldTypeString}. O {@code apply} é um
 * pass-through de propósito: o que está sob teste é a validação disparada pelo {@code execute}.
 */
public class BusinessFieldTypeString implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeString.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
