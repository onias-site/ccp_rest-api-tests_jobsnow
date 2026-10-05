package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.interfaces.CcpJsonValidator;

/**
 * Second custom global validator, used to prove that {@code customJsonValidators} runs every validator of the
 * list and accumulates the errors of each one.
 */
public class RequiredTitleValidator implements CcpJsonValidator {

	public static final String MESSAGE = "O titulo da vaga e obrigatorio";

	public boolean hasError(CcpJsonRepresentation json, Class<?> clazz) {
		boolean containsAllFields = json.containsAllFields(GlobalValidatorFields.titulo);

		boolean titleAbsent = false == containsAllFields;
		return titleAbsent;
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
