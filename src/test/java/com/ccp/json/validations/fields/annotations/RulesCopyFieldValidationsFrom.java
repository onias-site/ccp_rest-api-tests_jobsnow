package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * A field without any rule of its own: all of them come from the field with the same name in
 * {@code RulesValidationsSource}, because of {@code @CcpJsonCopyFieldValidationsFrom}. The {@code causa*} fields
 * reproduce the shape used in {@code JnEntityJobsnowError.Fields.cause}: {@code @CcpJsonFieldValidatorArray} on
 * the target field and the type coming from the source class.
 */
public enum RulesCopyFieldValidationsFrom implements CcpJsonFieldName {

	@CcpJsonCopyFieldValidationsFrom(RulesValidationsSource.class)
	apelido,

	@CcpJsonFieldValidatorArray(nonRepeatedItems = false)
	@CcpJsonCopyFieldValidationsFrom(RulesValidationsSource.class)
	causaComRepetidos,

	@CcpJsonFieldValidatorArray
	@CcpJsonCopyFieldValidationsFrom(RulesValidationsSource.class)
	causaSemRepetidos,
	;
}
