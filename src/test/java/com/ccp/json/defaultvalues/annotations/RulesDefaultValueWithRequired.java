package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Fields in which the default value coexists with both forms of requirement: the
 * {@code @CcpJsonFieldValidatorRequired} annotation and the primary key of an entity. In both cases the
 * requirement must be turned off, because the default value is what fills the absent field.
 */
public enum RulesDefaultValueWithRequired implements CcpJsonFieldName {

	@CcpJsonFieldValidatorRequired
	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "preenchido pelo valor padrao")
	obrigatorioComValorPadrao,

	@CcpEntityFieldPrimaryKey
	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "chave gerada")
	chavePrimariaComValorPadrao,
	;
}
