package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldWrittenBeforeTheSave;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Fields to exercise {@code @CcpJsonFieldValidatorRequired}: a required one, an optional one and one that is
 * required for being the primary key of the entity, and one of the primary key written before the save, which is not.
 */
public enum RulesValidatorRequired implements CcpJsonFieldName {

	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString
	obrigatorio,

	@CcpJsonFieldTypeString
	opcional,

	@CcpEntityFieldPrimaryKey
	@CcpJsonFieldTypeString
	chavePrimaria,

	@CcpEntityFieldPrimaryKey
	@CcpEntityFieldWrittenBeforeTheSave
	@CcpJsonFieldTypeString
	chaveEscritaAntesDoSave,
	;
}
