package com.vis.json.fields.validation;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@link RulesYearMonth}: it returns the JSON it receives. */
public class BusinessYearMonth implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public static final BusinessYearMonth INSTANCE = new BusinessYearMonth();

	private BusinessYearMonth() {}

	public Class<?> getJsonValidationClass() {
		return RulesYearMonth.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
