package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberInteger;

/**
 * Fields annotated with {@code @CcpJsonFieldTypeNumberInteger} (long), one for each restriction: minimum,
 * maximum and exact values and list of allowed values.
 */
public enum RulesFieldTypeNumberInteger implements CcpJsonFieldName {

	@CcpJsonFieldTypeNumberInteger(minValue = 10)
	valorMinimo,

	@CcpJsonFieldTypeNumberInteger(maxValue = 20)
	valorMaximo,

	// 1500 is outside the Long cache (-128..127) on purpose: if the comparison of the validator were
	// made by reference instead of by value, the defect shows up here and vanishes with small numbers.
	@CcpJsonFieldTypeNumberInteger(exactValue = 1500)
	valorExato,

	@CcpJsonFieldTypeNumberInteger(allowedValues = {1, 2, 3})
	valorPermitido,

	@CcpJsonFieldTypeNumberInteger
	semRestricao,
	;
}
