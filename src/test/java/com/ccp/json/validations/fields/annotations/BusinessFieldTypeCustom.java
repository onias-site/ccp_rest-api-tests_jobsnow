package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeCustom}. */
public class BusinessFieldTypeCustom implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessFieldTypeCustom INSTANCE = new BusinessFieldTypeCustom();

	private BusinessFieldTypeCustom() {}

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeCustom.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
