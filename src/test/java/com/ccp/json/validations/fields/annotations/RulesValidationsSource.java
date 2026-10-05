package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Source class of the validations copied by {@code @CcpJsonCopyFieldValidationsFrom}. This is where the rules
 * actually live; the target class only points here.
 */
public enum RulesValidationsSource implements CcpJsonFieldName {

	@CcpJsonFieldTypeString(minLength = 3, maxLength = 8)
	apelido,

	@CcpJsonFieldTypeString
	causaComRepetidos,

	@CcpJsonFieldTypeString
	causaSemRepetidos,
	;
}
