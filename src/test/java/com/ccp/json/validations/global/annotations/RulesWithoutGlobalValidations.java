package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Rules class without the {@code @CcpJsonGlobalValidations} annotation: a counterproof that the global
 * validations exist only because of the annotation.
 */
public enum RulesWithoutGlobalValidations implements CcpJsonFieldName {

	minClt,
	maxClt,
	titulo,
	;
}
