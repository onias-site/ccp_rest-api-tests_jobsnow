package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Source class of the copied validations. The default value annotation lives here, while
 * {@code @CcpJsonFieldValidatorRequired} stays on the target field, exactly the shape of {@code fileName}, whose
 * default value lives in {@code JnJsonInstantMessengerFields}.
 */
public enum RulesDefaultValueSource implements CcpJsonFieldName {

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "derivado de {origem}")
	copiado,
	;
}
