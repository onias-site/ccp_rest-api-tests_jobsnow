package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;

/**
 * Campos anotados com {@code @CcpJsonFieldTypeNumber} (double), um para cada restrição: valor
 * mínimo, máximo, exato e lista de valores permitidos.
 */
public enum RegrasFieldTypeNumber implements CcpJsonFieldName {

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
