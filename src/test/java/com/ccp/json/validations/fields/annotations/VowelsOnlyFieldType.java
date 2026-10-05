package com.ccp.json.validations.fields.annotations;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldType;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldValidatorInterface;

/**
 * Custom field type, pointed to by {@code @CcpJsonFieldTypeCustom}: only a value made exclusively of vowels is
 * compatible. It proves that the engine instantiates and consults the class given in the annotation.
 */
public class VowelsOnlyFieldType implements CcpJsonFieldType {

	public Predicate<CcpJsonRepresentation> evaluateCompatibleType(String fieldName) {
		return json -> {
			CcpFieldName ccpFieldName = new CcpFieldName(fieldName);
			String value = json.getAsString(ccpFieldName);
			boolean onlyVowels = value.matches("^[aeiouAEIOU]+$");
			return onlyVowels;
		};
	}

	public boolean hasRuleExplanation(Field field) {
		return true;
	}

	public CcpJsonFieldValidatorInterface[] getErrorTypes() {
		return new CcpJsonFieldValidatorInterface[0];
	}

	public String name() {
		String name = VowelsOnlyFieldType.class.getSimpleName();
		return name;
	}
}
