package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Um único grupo em {@code requiresAtLeastOne}, no mesmo formato usado por {@code VisEntityResume.Fields}:
 * o json precisa trazer {@code maxClt} ou {@code maxPj}.
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
