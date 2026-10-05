package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Counterproof of the scenarios of {@code RulesDefaultValueWithRequired}: without the default value annotation,
 * the required field is still demanded.
 */
public enum RulesRequiredWithoutDefaultValue implements CcpJsonFieldName {

	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString
	obrigatorioSemValorPadrao,
	;
}
