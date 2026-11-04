package com.vis.json.fields.validation;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeCustom;

/** A field validated by {@link VisYearMonthFieldType}. */
public enum RulesYearMonth implements CcpJsonFieldName {

	@CcpJsonFieldTypeCustom(VisYearMonthFieldType.class)
	yearMonth,
	;
}
