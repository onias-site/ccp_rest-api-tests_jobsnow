package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Campo sem nenhuma regra própria: todas vêm do campo homônimo de
 * {@code RulesValidationsSource}, por conta de {@code @CcpJsonCopyFieldValidationsFrom}.
 * Os campos {@code causa*} reproduzem a forma usada em {@code JnEntityJobsnowError.Fields.cause}:
 * {@code @CcpJsonFieldValidatorArray} no campo de destino e o tipo vindo da classe de origem.
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
