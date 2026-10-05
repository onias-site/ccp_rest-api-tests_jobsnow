package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * A single group of {@code requiresAtLeastOne} built from two classes: the fields of both are joined in one
 * group, so any of the four channels satisfies the rule.
 */
@CcpJsonGlobalValidations(requiresAtLeastOne = {
		@CcpJsonValidationFieldList({InformalChannelGroup.class, FormalChannelGroup.class})
})
public enum RulesRequiresAtLeastOneUnionOfClasses implements CcpJsonFieldName {

	telegram,
	whatsapp,
	email,
	sms,
	titulo,
	;
}
