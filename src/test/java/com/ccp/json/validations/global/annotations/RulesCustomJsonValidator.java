package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/** A single custom validator in {@code customJsonValidators}. */
@CcpJsonGlobalValidations(customJsonValidators = ConsistentSalaryRangeValidator.class)
public enum RulesCustomJsonValidator implements CcpJsonFieldName {

	minClt,
	maxClt,
	;
}
