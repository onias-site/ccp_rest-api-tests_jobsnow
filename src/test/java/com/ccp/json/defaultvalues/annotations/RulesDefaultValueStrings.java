package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Fields to exercise the {@code defaultStrings} attribute: a single item, several items, an item with a template
 * to resolve, a template that points to an absent field and a field without default value.
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
