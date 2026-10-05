package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * A single group in {@code requiresAtLeastOne}, in the same format used by {@code VisEntityResume.Fields}: the
 * JSON must bring {@code maxClt} or {@code maxPj}.
 */
@CcpJsonGlobalValidations(requiresAtLeastOne = {
		@CcpJsonValidationFieldList(MaxSalaryGroup.class)
})
public enum RulesRequiresAtLeastOneOneGroup implements CcpJsonFieldName {

	maxClt,
	maxPj,
	titulo,
	;
}
