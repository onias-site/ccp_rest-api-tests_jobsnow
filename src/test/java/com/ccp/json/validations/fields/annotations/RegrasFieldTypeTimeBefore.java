package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeTimeBefore;

/**
 * Campos anotados com {@code @CcpJsonFieldTypeTimeBefore}: o timestamp informado tem que estar
 * dentro do intervalo configurado, contado para trás a partir do momento atual.
 */
public enum RegrasFieldTypeTimeBefore implements CcpJsonFieldName {

	@CcpJsonFieldTypeTimeBefore(intervalType = CcpEntityExpurgableOptions.daily, maxValue = 7)
	noMaximoSeteDiasAtras,

	@CcpJsonFieldTypeTimeBefore(intervalType = CcpEntityExpurgableOptions.daily, minValue = 2)
	noMinimoDoisDiasAtras,
	;
}
