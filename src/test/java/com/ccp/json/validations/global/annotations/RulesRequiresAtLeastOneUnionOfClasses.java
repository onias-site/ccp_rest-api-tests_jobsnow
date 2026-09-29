package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Um único grupo de {@code requiresAtLeastOne} montado a partir de duas classes: os campos das duas
 * são unidos num só grupo, então qualquer um dos quatro canais satisfaz a regra.
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
