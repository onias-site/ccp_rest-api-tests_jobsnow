package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Validador crítico seguido de um validador comum: quando o crítico acusa erro, o que vem depois dele
 * não chega a ser executado.
 */
@CcpJsonGlobalValidations(customJsonValidators = {
		ValidadorCriticoDeContrato.class,
		ValidadorTituloObrigatorio.class
})
public enum RegrasCustomJsonValidatorCritico implements CcpJsonFieldName {

	contrato,
	titulo,
	;
}
