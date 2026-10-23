package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeBoolean}. */
public class BusinessFieldTypeBoolean implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessFieldTypeBoolean INSTANCE = new BusinessFieldTypeBoolean();

	private BusinessFieldTypeBoolean() {}

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeBoolean.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
