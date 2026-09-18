package com.ccp.json.validations.global.annotations;

/**
 * Grupo com as duas pontas da faixa salarial PJ, espelhando {@code PjSalaryRange} da entidade de vaga:
 * quem informa uma ponta precisa informar a outra.
 */
public enum GrupoFaixaPj {

	minPj,
	maxPj,
	;
}
