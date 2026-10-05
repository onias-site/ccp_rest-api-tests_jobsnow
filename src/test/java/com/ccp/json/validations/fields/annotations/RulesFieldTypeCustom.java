package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeCustom;

/** Field annotated with {@code @CcpJsonFieldTypeCustom}, pointing to the custom type {@code VowelsOnlyFieldType}. */
public enum RulesFieldTypeCustom implements CcpJsonFieldName {

	@CcpJsonFieldTypeCustom(VowelsOnlyFieldType.class)
	apenasVogais,
	;
}
