package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Um único validador customizado em {@code customJsonValidators}.
 */
@CcpJsonGlobalValidations(customJsonValidators = ConsistentSalaryRangeValidator.class)
public enum RulesCustomJsonValidator implements CcpJsonFieldName {

	minClt,
	maxClt,
	;
}
