package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Dois grupos em {@code requiresAllOrNone}, como em {@code VisEntityPosition.Fields}: cada faixa é
 * avaliada isoladamente, então completar a faixa CLT não desobriga quem começou a faixa PJ.
 */
@CcpJsonGlobalValidations(requiresAllOrNone = {
		@CcpJsonValidationFieldList(GrupoFaixaClt.class),
		@CcpJsonValidationFieldList(GrupoFaixaPj.class)
})
public enum RegrasRequiresAllOrNoneDoisGrupos implements CcpJsonFieldName {

	minClt,
	maxClt,
	minPj,
	maxPj,
	;
}
