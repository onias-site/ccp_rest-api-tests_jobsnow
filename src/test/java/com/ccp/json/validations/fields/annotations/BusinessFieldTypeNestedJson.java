package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesFieldTypeNestedJson}. */
public class BusinessFieldTypeNestedJson implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessFieldTypeNestedJson INSTANCE = new BusinessFieldTypeNestedJson();

	private BusinessFieldTypeNestedJson() {}

	public Class<?> getJsonValidationClass() {
		return RulesFieldTypeNestedJson.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
