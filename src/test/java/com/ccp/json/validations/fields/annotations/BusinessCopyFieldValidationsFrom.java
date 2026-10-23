package com.ccp.json.validations.fields.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business whose validation rules live in {@code RulesCopyFieldValidationsFrom}. */
public class BusinessCopyFieldValidationsFrom implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static BusinessCopyFieldValidationsFrom INSTANCE = new BusinessCopyFieldValidationsFrom();

	private BusinessCopyFieldValidationsFrom() {}

	public Class<?> getJsonValidationClass() {
		return RulesCopyFieldValidationsFrom.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
