package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.interfaces.CcpJsonValidator;

/**
 * Validador global customizado: quando as duas pontas da faixa CLT vêm no json, o piso não pode ser
 * maior que o teto. É o tipo de regra que depende de mais de um campo ao mesmo tempo e por isso não
 * caberia numa anotação de campo.
 */
public class ConsistentSalaryRangeValidator implements CcpJsonValidator {

	public static final String MESSAGE = "O piso salarial nao pode ser maior que o teto salarial";

	public boolean hasError(CcpJsonRepresentation json, Class<?> clazz) {

		boolean containsAllFields = json.containsAllFields(GlobalValidatorFields.minClt, GlobalValidatorFields.maxClt);

		boolean naoDaParaComparar = false == containsAllFields;

		if(naoDaParaComparar) {
			return false;
		}

		Double piso = json.getAsDoubleNumber(GlobalValidatorFields.minClt);
		Double teto = json.getAsDoubleNumber(GlobalValidatorFields.maxClt);

		boolean pisoMaiorQueTeto = piso > teto;
		return pisoMaiorQueTeto;
	}

	public Object getErrorMessage(CcpJsonRepresentation json, Class<?> clazz) {
		return MESSAGE;
	}

	public boolean isCriticalValidation(CcpJsonRepresentation json, Class<?> clazz) {
		return false;
	}

	public Object getRuleExplanation(Class<?> clazz) {
		return MESSAGE;
	}
}
