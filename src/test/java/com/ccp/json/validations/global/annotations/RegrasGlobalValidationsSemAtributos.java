package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Anotação presente porém com todos os atributos no valor padrão (listas vazias): nenhuma validação
 * global deve ser cobrada, qualquer json passa.
 */
@CcpJsonGlobalValidations
public enum RegrasGlobalValidationsSemAtributos implements CcpJsonFieldName {

	minClt,
	maxClt,
	titulo,
	;
}
