package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Campo obrigatório que não declara valor padrão próprio: herda o de
 * {@code RegrasOrigemDoValorPadrao} através de {@code @CcpJsonCopyFieldValidationsFrom}.
 */
public enum RegrasValorPadraoCopiado implements CcpJsonFieldName {

	@CcpJsonFieldTypeString
	origem,

	@CcpJsonFieldValidatorRequired
	@CcpJsonCopyFieldValidationsFrom(RegrasOrigemDoValorPadrao.class)
	copiado,
	;
}
