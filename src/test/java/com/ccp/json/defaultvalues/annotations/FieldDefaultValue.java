package com.ccp.json.defaultvalues.annotations;

import static org.junit.Assert.fail;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;

/**
 * Support for the tests of {@code @CcpJsonFieldDefaultValue}. Runs the business through {@code execute} and
 * returns the JSON with the default values already applied. {@code FieldValidation.accepts} cannot be reused: it
 * requires the output to equal the input, and the goal here is precisely an output carrying fields the input did
 * not have.
 */
public class FieldDefaultValue {

	private FieldDefaultValue() {}

	/**
	 * Runs the business over the given rules class and returns the resulting JSON, failing the test when the
	 * validation refuses the input.
	 */
	public static CcpJsonRepresentation applyDefaultValues(Class<?> rulesClass, CcpJsonRepresentation json) {

		DefaultValueBusiness business = new DefaultValueBusiness(rulesClass);

		try {
			CcpJsonRepresentation returned = business.execute(json);
			return returned;
		} catch (CcpJsonValidationError e) {
			String message = e.getExplanedMessage();
			fail("The JSON should have passed the validation, but it was refused with: " + message);
			return CcpOtherConstants.EMPTY_JSON;
		}
	}
}
