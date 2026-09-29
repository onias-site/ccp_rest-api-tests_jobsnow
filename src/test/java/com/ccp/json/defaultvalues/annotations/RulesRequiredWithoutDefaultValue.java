package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Contraprova dos cenários de {@code RulesDefaultValueWithRequired}: sem a anotação de valor padrão,
 * o campo obrigatório continua sendo exigido.
 */
public enum RulesRequiredWithoutDefaultValue implements CcpJsonFieldName {

	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString
	obrigatorioSemValorPadrao,
	;
}
