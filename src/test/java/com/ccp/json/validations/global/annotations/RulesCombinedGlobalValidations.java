package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Os três atributos da anotação em uso ao mesmo tempo, espelhando {@code VisEntityPosition.Fields} e
 * acrescentando um validador customizado: as regras são independentes e somam seus erros.
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
