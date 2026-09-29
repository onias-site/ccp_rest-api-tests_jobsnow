package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Classe de regras sem a anotação {@code @CcpJsonGlobalValidations}: serve de contraprova de que as
 * validações globais só existem por causa da anotação.
 */
public enum RulesWithoutGlobalValidations implements CcpJsonFieldName {

	minClt,
	maxClt,
	titulo,
	;
}
