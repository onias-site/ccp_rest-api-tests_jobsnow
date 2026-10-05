package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeNumber}. */
public class BusinessFieldTypeNumber implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeNumber.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
