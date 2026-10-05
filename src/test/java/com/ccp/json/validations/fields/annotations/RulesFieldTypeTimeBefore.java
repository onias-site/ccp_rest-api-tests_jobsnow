package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeTimeBefore;

/**
 * Fields annotated with {@code @CcpJsonFieldTypeTimeBefore}: the given timestamp must be within the configured
 * interval, counted backward from the current moment.
 */
public enum RulesFieldTypeTimeBefore implements CcpJsonFieldName {

	@CcpJsonFieldTypeTimeBefore(intervalType = CcpEntityExpurgableOptions.daily, maxValue = 7)
	noMaximoSeteDiasAtras,

	@CcpJsonFieldTypeTimeBefore(intervalType = CcpEntityExpurgableOptions.daily, minValue = 2)
	noMinimoDoisDiasAtras,

	@CcpJsonFieldTypeTimeBefore(intervalType = CcpEntityExpurgableOptions.daily, exactValue = 3)
	exactlyThreeDaysAgo,
	;
}
