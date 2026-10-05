package com.ccp.json.validations.global.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.annotations.BusinessValidatorRequired;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldValidatorInterface.CcpErrorFields;
import com.ccp.json.validations.global.engine.CcpJsonValidationError.CcpValidationErrorFields;

/**
 * Proves {@link CcpJsonValidationError#getExplanedMessage()}: every error description of every field, each one closed
 * by {@code ". "}.
 */
public class ExplainedMessageTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void theDescriptionsOfTheFieldErrorsBecomeSentences() {
		try {
			new BusinessValidatorRequired().execute(CcpOtherConstants.EMPTY_JSON);
			fail("the required field is missing");
		} catch (CcpJsonValidationError e) {
			CcpJsonRepresentation errors = e.json.getInnerJson(CcpValidationErrorFields.errors);
			StringBuilder expected = new StringBuilder();
			for (String field : errors.fieldSet()) {
				List<CcpJsonRepresentation> fieldErrors = errors.getAsJsonList(() -> field);
				for (CcpJsonRepresentation error : fieldErrors) {
					expected.append(error.getAsString(CcpErrorFields.errorDescription)).append(". ");
				}
			}

			String message = e.getExplanedMessage();

			assertTrue(message, message.length() > 2);
			assertEquals(expected.toString(), message);
			assertTrue(message.endsWith(". "));
		}
	}
}
