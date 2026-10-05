package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesValidatorArray}. */
public class BusinessValidatorArray implements CcpBusiness {

	public Class<?> getJsonValidationClass() {
		return RulesValidatorArray.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
