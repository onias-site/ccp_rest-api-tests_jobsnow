package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Campos para exercitar o atributo {@code defaultStrings}: um item só, vários itens, um item com
 * template a resolver, um template que aponta para campo ausente e um campo sem valor padrão.
 */
public enum RulesDefaultValueStrings implements CcpJsonFieldName {

	@CcpJsonFieldTypeString
	name,

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "valor unico")
	umItemSo,

	@CcpJsonFieldDefaultValue(defaultStrings = {"primeiro", "segundo", "terceiro"})
	variosItens,

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "ola {name}")
	comTemplate,

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "[{inexistente}]")
	comTemplateSemOrigem,

	@CcpJsonFieldTypeString
	semValorPadrao,
	;
}
