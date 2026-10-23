package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesValidatorArray}. */
public class BusinessValidatorArray implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessValidatorArray INSTANCE = new BusinessValidatorArray();

	private BusinessValidatorArray() {}

	public Class<?> getJsonValidationClass() {
		return RulesValidatorArray.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
