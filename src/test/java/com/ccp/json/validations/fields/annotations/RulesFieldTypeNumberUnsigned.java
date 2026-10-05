package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;

/**
 * Fields annotated with {@code @CcpJsonFieldTypeNumberUnsigned} (non-negative long), one for each restriction:
 * minimum, maximum and exact values and list of allowed values.
 */
public enum RulesFieldTypeNumberUnsigned implements CcpJsonFieldName {

	@CcpJsonFieldTypeNumberUnsigned(minValue = 10)
	valorMinimo,

	@CcpJsonFieldTypeNumberUnsigned(maxValue = 20)
	valorMaximo,

	// 1500 is outside the Long cache (-128..127) on purpose: if the comparison of the validator were
	// made by reference instead of by value, the defect shows up here and vanishes with small numbers.
	@CcpJsonFieldTypeNumberUnsigned(exactValue = 1500)
	valorExato,

	@CcpJsonFieldTypeNumberUnsigned(allowedValues = {1, 2, 3})
	valorPermitido,

	@CcpJsonFieldTypeNumberUnsigned
	semRestricao,
	;
}
