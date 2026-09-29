package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeCustom;

/**
 * Campo anotado com {@code @CcpJsonFieldTypeCustom}, apontando para o tipo customizado
 * {@code VowelsOnlyFieldType}.
 */
public enum RulesFieldTypeCustom implements CcpJsonFieldName {

	@CcpJsonFieldTypeCustom(VowelsOnlyFieldType.class)
	apenasVogais,
	;
}
