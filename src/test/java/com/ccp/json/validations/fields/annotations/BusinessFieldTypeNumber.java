package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeNumber}. */
public class BusinessFieldTypeNumber implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessFieldTypeNumber INSTANCE = new BusinessFieldTypeNumber();

	private BusinessFieldTypeNumber() {}

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeNumber.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
