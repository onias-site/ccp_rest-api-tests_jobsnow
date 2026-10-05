package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Fields to exercise the {@code jsonProducer} attribute: one with a producer of its own and another with the
 * annotation without any attribute, which falls into the default producer and therefore sets no value.
 */
public enum RulesDefaultValueJsonProducer implements CcpJsonFieldName {

	@CcpJsonFieldTypeString
	name,

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(jsonProducer = DefaultValueProducer.class)
	produzido,

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue
	inerte,
	;
}
