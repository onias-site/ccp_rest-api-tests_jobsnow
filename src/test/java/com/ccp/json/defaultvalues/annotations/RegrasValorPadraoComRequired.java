package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Campos em que o valor padrão convive com as duas formas de obrigatoriedade: a anotação
 * {@code @CcpJsonFieldValidatorRequired} e a chave primária de entidade. Em ambos os casos a
 * obrigatoriedade deve ficar desligada, porque quem preenche o campo ausente é o valor padrão.
 */
public enum RegrasValorPadraoComRequired implements CcpJsonFieldName {

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
