package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;

/**
 * Campos anotados com {@code @CcpJsonFieldTypeNestedJson}: um que valida o json interno contra
 * {@code RulesNestedAddress} e outro que apenas proíbe json interno vazio.
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
