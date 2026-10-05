package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * A single group in {@code requiresAllOrNone}: either the JSON brings both ends of the CLT range, or none of
 * them. Giving only one end is an error.
 */
@CcpJsonGlobalValidations(requiresAllOrNone = {
		@CcpJsonValidationFieldList(CltRangeGroup.class)
})
public enum RulesRequiresAllOrNoneOneGroup implements CcpJsonFieldName {

	minClt,
	maxClt,
	titulo,
	;
}
