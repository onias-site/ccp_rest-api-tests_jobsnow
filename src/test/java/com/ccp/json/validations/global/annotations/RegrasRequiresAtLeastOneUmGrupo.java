package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Um único grupo em {@code requiresAtLeastOne}, no mesmo formato usado por {@code VisEntityResume.Fields}:
 * o json precisa trazer {@code maxClt} ou {@code maxPj}.
 */
@CcpJsonGlobalValidations(requiresAtLeastOne = {
		@CcpJsonValidationFieldList(GrupoSalarioMaximo.class)
})
public enum RegrasRequiresAtLeastOneUmGrupo implements CcpJsonFieldName {

	maxClt,
	maxPj,
	titulo,
	;
}
