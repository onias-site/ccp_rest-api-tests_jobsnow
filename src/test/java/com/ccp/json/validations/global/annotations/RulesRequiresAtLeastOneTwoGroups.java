package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Two independent groups in {@code requiresAtLeastOne}, as in {@code VisEntityPosition.Fields}: each group must
 * be satisfied on its own, so satisfying only one of them is not enough.
 */
@CcpJsonGlobalValidations(requiresAtLeastOne = {
		@CcpJsonValidationFieldList(MaxSalaryGroup.class),
		@CcpJsonValidationFieldList(MinSalaryGroup.class)
})
public enum RulesRequiresAtLeastOneTwoGroups implements CcpJsonFieldName {

	minClt,
	maxClt,
	minPj,
	maxPj,
	;
}
