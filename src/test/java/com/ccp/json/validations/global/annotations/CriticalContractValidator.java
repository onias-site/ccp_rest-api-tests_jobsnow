package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.interfaces.CcpJsonValidator;

/**
 * Custom global validator marked as critical: without the contract type it makes no sense to keep evaluating the
 * rest, so when it reports an error it interrupts the other class validations.
 */
public class CriticalContractValidator implements CcpJsonValidator {

	public static final String MESSAGE = "O tipo de contrato e obrigatorio e sem ele nada mais pode ser avaliado";

	public boolean hasError(CcpJsonRepresentation json, Class<?> clazz) {
		boolean containsAllFields = json.containsAllFields(GlobalValidatorFields.contrato);

		boolean contractAbsent = false == containsAllFields;
		return contractAbsent;
	}

	public Object getErrorMessage(CcpJsonRepresentation json, Class<?> clazz) {
		return MESSAGE;
	}

	public boolean isCriticalValidation(CcpJsonRepresentation json, Class<?> clazz) {
		return true;
	}

	public Object getRuleExplanation(Class<?> clazz) {
		return MESSAGE;
	}
}
