package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeNumberUnsigned}. */
public class BusinessFieldTypeNumberUnsigned implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeNumberUnsigned.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
