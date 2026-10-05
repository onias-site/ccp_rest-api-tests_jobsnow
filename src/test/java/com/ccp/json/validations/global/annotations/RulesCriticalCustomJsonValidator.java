package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * A critical validator followed by a common one: when the critical one reports an error, the one after it is not
 * even run.
 */
@CcpJsonGlobalValidations(customJsonValidators = {
		CriticalContractValidator.class,
		RequiredTitleValidator.class
})
public enum RulesCriticalCustomJsonValidator implements CcpJsonFieldName {

	contrato,
	titulo,
	;
}
