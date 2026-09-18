package com.ccp.json.validations.global.annotations;

/**
 * Grupo com as duas pontas da faixa salarial CLT, espelhando {@code CltSalaryRange} da entidade de vaga:
 * quem informa uma ponta precisa informar a outra.
 */
public enum GrupoFaixaClt {

	minClt,
	maxClt,
	;
}
