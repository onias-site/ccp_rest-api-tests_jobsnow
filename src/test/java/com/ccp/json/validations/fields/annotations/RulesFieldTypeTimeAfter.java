package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeTimeAfter;

/**
 * Fields annotated with {@code @CcpJsonFieldTypeTimeAfter}: the given timestamp must be within the configured
 * interval, counted forward from the current moment.
 */
public enum RulesFieldTypeTimeAfter implements CcpJsonFieldName {

	@CcpJsonFieldTypeTimeAfter(intervalType = CcpEntityExpurgableOptions.daily, maxValue = 7)
	noMaximoSeteDiasAFrente,

	@CcpJsonFieldTypeTimeAfter(intervalType = CcpEntityExpurgableOptions.daily, minValue = 2)
	noMinimoDoisDiasAFrente,

	@CcpJsonFieldTypeTimeAfter(intervalType = CcpEntityExpurgableOptions.daily, exactValue = 3)
	exactlyThreeDaysAhead,
	;
}
