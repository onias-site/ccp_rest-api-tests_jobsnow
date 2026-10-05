package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.interfaces.CcpJsonValidator;

/**
 * Custom global validator: when both ends of the CLT range are in the JSON, the floor cannot be greater than the
 * ceiling. It is the kind of rule that depends on more than one field at the same time, and so would not fit in
 * a field annotation.
 */
public class ConsistentSalaryRangeValidator implements CcpJsonValidator {

	public static final String MESSAGE = "O piso salarial nao pode ser maior que o teto salarial";

	public boolean hasError(CcpJsonRepresentation json, Class<?> clazz) {

		boolean containsAllFields = json.containsAllFields(GlobalValidatorFields.minClt, GlobalValidatorFields.maxClt);

		boolean cannotCompare = false == containsAllFields;

		if(cannotCompare) {
			return false;
		}

		Double floor = json.getAsDoubleNumber(GlobalValidatorFields.minClt);
		Double ceiling = json.getAsDoubleNumber(GlobalValidatorFields.maxClt);

		boolean floorAboveCeiling = floor > ceiling;
		return floorAboveCeiling;
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
