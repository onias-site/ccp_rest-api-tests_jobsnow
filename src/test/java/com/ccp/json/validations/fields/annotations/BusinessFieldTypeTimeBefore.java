package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeTimeBefore}. */
public class BusinessFieldTypeTimeBefore implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessFieldTypeTimeBefore INSTANCE = new BusinessFieldTypeTimeBefore();

	private BusinessFieldTypeTimeBefore() {}

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeTimeBefore.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
