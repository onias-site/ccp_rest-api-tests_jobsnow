package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Business whose validation rules live in {@code RulesFieldTypeString}. The {@code apply} is a pass-through on
 * purpose: what is under test is the validation triggered by {@code execute}.
 */
public class BusinessFieldTypeString implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeString.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
