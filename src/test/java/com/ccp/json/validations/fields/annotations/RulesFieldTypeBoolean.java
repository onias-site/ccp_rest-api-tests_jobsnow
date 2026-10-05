package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeBoolean;

/**
 * Field annotated with {@code @CcpJsonFieldTypeBoolean}, which has no parameters: the only rule is the type
 * compatibility.
 */
public enum RulesFieldTypeBoolean implements CcpJsonFieldName {

	@CcpJsonFieldTypeBoolean
	ativo,
	;
}
