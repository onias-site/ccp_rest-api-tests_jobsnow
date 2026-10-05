package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;

/**
 * Fields annotated with {@code @CcpJsonFieldTypeNumber} (double), one for each restriction: minimum, maximum and
 * exact values and list of allowed values.
 */
public enum RulesFieldTypeNumber implements CcpJsonFieldName {

	@CcpJsonFieldTypeNumber(minValue = 5.5)
	valorMinimo,

	@CcpJsonFieldTypeNumber(maxValue = 10.5)
	valorMaximo,

	@CcpJsonFieldTypeNumber(exactValue = 7.5)
	valorExato,

	@CcpJsonFieldTypeNumber(allowedValues = {1.5, 2.5})
	valorPermitido,

	@CcpJsonFieldTypeNumber
	semRestricao,
	;
}
