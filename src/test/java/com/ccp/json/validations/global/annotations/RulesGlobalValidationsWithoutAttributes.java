package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * The annotation is present but with every attribute at its default value (empty lists): no global validation
 * must be demanded, any JSON passes.
 */
@CcpJsonGlobalValidations
public enum RulesGlobalValidationsWithoutAttributes implements CcpJsonFieldName {

	minClt,
	maxClt,
	titulo,
	;
}
