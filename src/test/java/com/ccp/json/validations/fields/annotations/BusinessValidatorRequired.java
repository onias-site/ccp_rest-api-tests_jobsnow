package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesValidatorRequired}. */
public class BusinessValidatorRequired implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessValidatorRequired INSTANCE = new BusinessValidatorRequired();

	private BusinessValidatorRequired() {}

	public Class<?> getJsonValidationClass() {
		return RulesValidatorRequired.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
