package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Campos para exercitar {@code @CcpJsonFieldValidatorRequired}: um obrigatório, um opcional e um
 * que é obrigatório por ser chave primária da entidade.
 */
public enum RegrasValidatorRequired implements CcpJsonFieldName {

	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString
	obrigatorio,

	@CcpJsonFieldTypeString
	opcional,

	@CcpEntityFieldPrimaryKey
	@CcpJsonFieldTypeString
	chavePrimaria,
	;
}
