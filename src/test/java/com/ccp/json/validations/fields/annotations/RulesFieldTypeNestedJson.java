package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;

/**
 * Fields annotated with {@code @CcpJsonFieldTypeNestedJson}: one that validates the inner JSON against
 * {@code RulesNestedAddress} and another that only forbids an empty inner JSON.
 */
public enum RulesFieldTypeNestedJson implements CcpJsonFieldName {

	@CcpJsonFieldTypeNestedJson(jsonValidation = RulesNestedAddress.class)
	endereco,

	@CcpJsonFieldTypeNestedJson(allowsEmptyJson = false)
	naoAceitaVazio,

	@CcpJsonFieldTypeNestedJson
	aceitaVazio,
	;
}
