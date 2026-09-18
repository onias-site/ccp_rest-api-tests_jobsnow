package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.interfaces.CcpJsonValidator;

/**
 * Validador global customizado: quando as duas pontas da faixa CLT vêm no json, o piso não pode ser
 * maior que o teto. É o tipo de regra que depende de mais de um campo ao mesmo tempo e por isso não
 * caberia numa anotação de campo.
 */
public class ValidadorFaixaSalarialCoerente implements CcpJsonValidator {

	public static final String MENSAGEM = "O piso salarial nao pode ser maior que o teto salarial";

	public boolean hasError(CcpJsonRepresentation json, Class<?> clazz) {

		boolean containsAllFields = json.containsAllFields(CamposDoValidadorGlobal.minClt, CamposDoValidadorGlobal.maxClt);

		boolean naoDaParaComparar = false == containsAllFields;

		if(naoDaParaComparar) {
			return false;
		}

		Double piso = json.getAsDoubleNumber(CamposDoValidadorGlobal.minClt);
		Double teto = json.getAsDoubleNumber(CamposDoValidadorGlobal.maxClt);

		boolean pisoMaiorQueTeto = piso > teto;
		return pisoMaiorQueTeto;
	}

	public Object getErrorMessage(CcpJsonRepresentation json, Class<?> clazz) {
		return MENSAGEM;
	}

	public boolean isCriticalValidation(CcpJsonRepresentation json, Class<?> clazz) {
		return false;
	}

	public Object getRuleExplanation(Class<?> clazz) {
		return MENSAGEM;
	}
}
