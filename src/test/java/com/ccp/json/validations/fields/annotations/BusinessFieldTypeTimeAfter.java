package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeTimeAfter}. */
public class BusinessFieldTypeTimeAfter implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessFieldTypeTimeAfter INSTANCE = new BusinessFieldTypeTimeAfter();

	private BusinessFieldTypeTimeAfter() {}

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeTimeAfter.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
