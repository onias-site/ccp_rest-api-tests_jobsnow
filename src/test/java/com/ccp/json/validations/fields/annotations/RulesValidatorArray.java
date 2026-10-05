package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Fields annotated with {@code @CcpJsonFieldValidatorArray}, one for each collection restriction: minimum,
 * maximum and exact size and repetition of items. All of them also carry a type annotation, because the engine
 * discards a field without a declared type before even validating the collection.
 */
public enum RulesValidatorArray implements CcpJsonFieldName {

	@CcpJsonFieldValidatorArray(minSize = 2)
	@CcpJsonFieldTypeString
	minimoDois,

	@CcpJsonFieldValidatorArray(maxSize = 2)
	@CcpJsonFieldTypeString
	maximoDois,

	@CcpJsonFieldValidatorArray(exactSize = 2)
	@CcpJsonFieldTypeString
	exatamenteDois,

	@CcpJsonFieldValidatorArray
	@CcpJsonFieldTypeString
	semItensRepetidos,

	@CcpJsonFieldValidatorArray(nonRepeatedItems = false)
	@CcpJsonFieldTypeString
	aceitaItensRepetidos,

	@CcpJsonFieldValidatorArray(minSize = 1)
	@CcpJsonFieldTypeString(allowedValuesEnum = RulesFieldTypeString.AllowedValues.class)
	colecaoDeEnums,

	@CcpJsonFieldTypeString
	singleValue,
	;
}
