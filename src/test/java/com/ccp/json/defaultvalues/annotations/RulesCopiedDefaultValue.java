package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * A required field that declares no default value of its own: it inherits the one of
 * {@code RulesDefaultValueSource} through {@code @CcpJsonCopyFieldValidationsFrom}.
 */
public enum RulesCopiedDefaultValue implements CcpJsonFieldName {

	@CcpJsonFieldTypeString
	origem,

	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(RulesDefaultValueSource.class)
	copiado,
	;
}
