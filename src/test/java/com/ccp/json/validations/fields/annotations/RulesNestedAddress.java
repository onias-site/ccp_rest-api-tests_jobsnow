package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Rules of the inner JSON used by {@code RulesFieldTypeNestedJson}: it proves that
 * {@code @CcpJsonFieldTypeNestedJson} validates the nested content recursively.
 */
public enum RulesNestedAddress implements CcpJsonFieldName {

	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString(minLength = 3)
	rua,
	;
}
