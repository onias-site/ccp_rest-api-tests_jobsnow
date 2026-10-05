package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeTimeAfter}. */
public class BusinessFieldTypeTimeAfter implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeTimeAfter.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
