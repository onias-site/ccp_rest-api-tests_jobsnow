package com.ccp.json.validations.fields.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldValidatorInterface.CcpErrorFields;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidationError.CcpValidationErrorFields;

/**
 * Support for the tests of the field validation annotations. Runs the {@code CcpBusiness} through
 * {@code execute} (which triggers {@code CcpJsonValidatorEngine} over the class returned by
 * {@code getJsonValidationClass}) and checks which validators reported an error.
 */
public class FieldValidation {

	private FieldValidation() {}

	/**
	 * Checks that the JSON passes the validation: {@code execute} must not throw {@code CcpJsonValidationError} and
	 * must return the JSON produced by {@code apply}.
	 */
	public static void accepts(CcpBusiness business, CcpJsonRepresentation json) {
		try {
			CcpJsonRepresentation returned = business.execute(json);
			assertEquals(json, returned);
		} catch (CcpJsonValidationError e) {
			String message = e.getExplanedMessage();
			fail("The JSON should have passed the validation, but it was refused with: " + message);
		}
	}

	/**
	 * Checks that the JSON is refused and that the validator {@code expectedValidator} reported an error on the
	 * field {@code field}. Returns the description of the error for further assertions.
	 */
	public static String refuses(CcpBusiness business, CcpJsonRepresentation json, CcpJsonFieldName field, CcpJsonFieldName expectedValidator) {

		try {
			business.execute(json);
		} catch (CcpJsonValidationError e) {
			return extractDescription(e, field, expectedValidator);
		}

		String fieldName = field.getValue();
		String validatorName = expectedValidator.getValue();
		fail("The JSON should have been refused by the validator '" + validatorName + "' on the field '" + fieldName + "', but it passed the validation");
		return "";
	}

	private static String extractDescription(CcpJsonValidationError e, CcpJsonFieldName field, CcpJsonFieldName expectedValidator) {

		CcpJsonRepresentation errors = e.json.getInnerJson(CcpValidationErrorFields.errors);
		String fieldName = field.getValue();
		boolean containsAllFields = errors.containsAllFields(field);

		boolean fieldWithoutErrors = false == containsAllFields;

		if(fieldWithoutErrors) {
			fail("An error was expected on the field '" + fieldName + "', but the errors came on: " + errors.fieldSet());
		}

		List<CcpJsonRepresentation> errorsOfTheField = errors.getAsJsonList(field);
		String validatorName = expectedValidator.getValue();
		List<String> validatorsFound = new ArrayList<>();

		for (CcpJsonRepresentation error : errorsOfTheField) {
			String errorName = error.getAsString(CcpErrorFields.errorName);
			validatorsFound.add(errorName);
			boolean errorNameEquals = errorName.equals(validatorName);

			boolean isNotTheExpectedValidator = false == errorNameEquals;

			if(isNotTheExpectedValidator) {
				continue;
			}
			String errorDescription = error.getAsString(CcpErrorFields.errorDescription);
			boolean emptyDescription = errorDescription.trim().isEmpty();
			assertTrue("O validador '" + validatorName + "' acusou erro sem descrever o motivo", false == emptyDescription);
			return errorDescription;
		}

		fail("The validator '" + validatorName + "' on the field '" + fieldName + "' was expected, but the validators that reported an error were: " + validatorsFound);
		return "";
	}
}
