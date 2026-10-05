package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeNumberInteger}. */
public class BusinessFieldTypeNumberInteger implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeNumberInteger.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
