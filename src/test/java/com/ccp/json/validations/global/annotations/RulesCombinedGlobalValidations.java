package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * The three attributes of the annotation in use at the same time, mirroring {@code VisEntityPosition.Fields} and
 * adding a custom validator: the rules are independent and add up their errors.
 */
@CcpJsonGlobalValidations(
		requiresAtLeastOne = {
				@CcpJsonValidationFieldList(MaxSalaryGroup.class),
				@CcpJsonValidationFieldList(MinSalaryGroup.class)
		},
		requiresAllOrNone = {
				@CcpJsonValidationFieldList(CltRangeGroup.class),
				@CcpJsonValidationFieldList(PjRangeGroup.class)
		},
		customJsonValidators = ConsistentSalaryRangeValidator.class)
public enum RulesCombinedGlobalValidations implements CcpJsonFieldName {

	minClt,
	maxClt,
	minPj,
	maxPj,
	;
}
