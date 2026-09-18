package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Dois validadores customizados não críticos: os dois devem ser executados e os dois erros devem
 * aparecer juntos no resultado.
 */
@CcpJsonGlobalValidations(customJsonValidators = {
		ValidadorFaixaSalarialCoerente.class,
		ValidadorTituloObrigatorio.class
})
public enum RegrasCustomJsonValidatoresEmSerie implements CcpJsonFieldName {

	minClt,
	maxClt,
	titulo,
	;
}
