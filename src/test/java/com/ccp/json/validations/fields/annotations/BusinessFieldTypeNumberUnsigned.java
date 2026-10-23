package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeNumberUnsigned}. */
public class BusinessFieldTypeNumberUnsigned implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessFieldTypeNumberUnsigned INSTANCE = new BusinessFieldTypeNumberUnsigned();

	private BusinessFieldTypeNumberUnsigned() {}

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeNumberUnsigned.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
