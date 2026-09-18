package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Um único grupo em {@code requiresAllOrNone}: ou o json traz as duas pontas da faixa CLT, ou não traz
 * nenhuma delas. Informar só uma ponta é erro.
 */
@CcpJsonGlobalValidations(requiresAllOrNone = {
		@CcpJsonValidationFieldList(GrupoFaixaClt.class)
})
public enum RegrasRequiresAllOrNoneUmGrupo implements CcpJsonFieldName {

	minClt,
	maxClt,
	titulo,
	;
}
