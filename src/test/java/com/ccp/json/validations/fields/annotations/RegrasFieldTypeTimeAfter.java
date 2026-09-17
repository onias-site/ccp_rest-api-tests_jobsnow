package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeTimeAfter;

/**
 * Campos anotados com {@code @CcpJsonFieldTypeTimeAfter}: o timestamp informado tem que estar
 * dentro do intervalo configurado, contado para frente a partir do momento atual.
 */
public enum RegrasFieldTypeTimeAfter implements CcpJsonFieldName {

	@CcpJsonFieldTypeTimeAfter(intervalType = CcpEntityExpurgableOptions.daily, maxValue = 7)
	noMaximoSeteDiasAFrente,

	@CcpJsonFieldTypeTimeAfter(intervalType = CcpEntityExpurgableOptions.daily, minValue = 2)
	noMinimoDoisDiasAFrente,
	;
}
