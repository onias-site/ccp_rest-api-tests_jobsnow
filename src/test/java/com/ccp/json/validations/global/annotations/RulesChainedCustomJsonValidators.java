package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Dois validadores customizados não críticos: os dois devem ser executados e os dois erros devem
 * aparecer juntos no resultado.
 */
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
