package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/** Two non-critical custom validators: both must run and both errors must appear together in the result. */
@CcpJsonGlobalValidations(customJsonValidators = {
		ConsistentSalaryRangeValidator.class,
		RequiredTitleValidator.class
})
public enum RulesChainedCustomJsonValidators implements CcpJsonFieldName {

	minClt,
	maxClt,
	titulo,
	;
}
