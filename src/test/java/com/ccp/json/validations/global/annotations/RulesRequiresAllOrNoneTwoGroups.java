package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Two groups in {@code requiresAllOrNone}, as in {@code VisEntityPosition.Fields}: each range is evaluated on
 * its own, so completing the CLT range does not release whoever started the PJ range.
 */
@CcpJsonGlobalValidations(requiresAllOrNone = {
		@CcpJsonValidationFieldList(CltRangeGroup.class),
		@CcpJsonValidationFieldList(PjRangeGroup.class)
})
public enum RulesRequiresAllOrNoneTwoGroups implements CcpJsonFieldName {

	minClt,
	maxClt,
	minPj,
	maxPj,
	;
}
