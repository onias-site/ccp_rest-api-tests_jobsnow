package com.ccp.json.validations.fields.annotations;

import java.util.function.Predicate;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/** Compatibility of {@link VowelsOnlyFieldType}: the value of the field is made exclusively of vowels. */
class VowelsOnlyCompatibility implements Predicate<CcpJsonRepresentation> {

	/** The name of the field. */
	private final String fieldName;

	VowelsOnlyCompatibility(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean test(CcpJsonRepresentation json) {
		CcpFieldName ccpFieldName = new CcpFieldName(this.fieldName);
		String value = json.getAsString(ccpFieldName);
		boolean onlyVowels = value.matches("^[aeiouAEIOU]+$");
		return onlyVowels;
	}
}
