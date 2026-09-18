package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Um único validador customizado em {@code customJsonValidators}.
 */
@CcpJsonGlobalValidations(customJsonValidators = ValidadorFaixaSalarialCoerente.class)
public enum RegrasCustomJsonValidator implements CcpJsonFieldName {

	minClt,
	maxClt,
	;
}
