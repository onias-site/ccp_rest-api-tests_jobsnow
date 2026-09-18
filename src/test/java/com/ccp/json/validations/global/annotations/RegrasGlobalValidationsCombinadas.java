package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Os três atributos da anotação em uso ao mesmo tempo, espelhando {@code VisEntityPosition.Fields} e
 * acrescentando um validador customizado: as regras são independentes e somam seus erros.
 */
@CcpJsonGlobalValidations(
		requiresAtLeastOne = {
				@CcpJsonValidationFieldList(GrupoSalarioMaximo.class),
				@CcpJsonValidationFieldList(GrupoSalarioMinimo.class)
		},
		requiresAllOrNone = {
				@CcpJsonValidationFieldList(GrupoFaixaClt.class),
				@CcpJsonValidationFieldList(GrupoFaixaPj.class)
		},
		customJsonValidators = ValidadorFaixaSalarialCoerente.class)
public enum RegrasGlobalValidationsCombinadas implements CcpJsonFieldName {

	minClt,
	maxClt,
	minPj,
	maxPj,
	;
}
