package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Dois grupos independentes em {@code requiresAtLeastOne}, como em {@code VisEntityPosition.Fields}:
 * cada grupo precisa ser satisfeito por conta própria, então satisfazer só um deles não basta.
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
