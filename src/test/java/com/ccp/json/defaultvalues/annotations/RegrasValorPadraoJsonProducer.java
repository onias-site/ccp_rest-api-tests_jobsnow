package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Campos para exercitar o atributo {@code jsonProducer}: um com produtor próprio e outro com a
 * anotação sem atributo algum, que cai no produtor padrão e portanto não define valor nenhum.
 */
public enum RegrasValorPadraoJsonProducer implements CcpJsonFieldName {

	@CcpJsonFieldTypeString
	nome,

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(jsonProducer = ProdutorDeValorPadrao.class)
	produzido,

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue
	inerte,
	;
}
