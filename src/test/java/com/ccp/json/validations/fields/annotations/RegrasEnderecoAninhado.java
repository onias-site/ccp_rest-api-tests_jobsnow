package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Regras do json interno usado por {@code RegrasFieldTypeNestedJson}: serve para provar que
 * {@code @CcpJsonFieldTypeNestedJson} valida o conteúdo aninhado de forma recursiva.
 */
public enum RegrasEnderecoAninhado implements CcpJsonFieldName {

	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString(minLength = 3)
	rua,
	;
}
