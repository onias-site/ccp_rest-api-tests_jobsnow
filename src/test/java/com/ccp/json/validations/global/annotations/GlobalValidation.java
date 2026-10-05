package com.ccp.json.validations.global.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidationRulesEngine;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;
import com.ccp.json.validations.global.engine.CcpJsonValidationError.CcpValidationErrorFields;

/**
 * Support for the tests of {@code @CcpJsonGlobalValidations}. Unlike the field validations, the global
 * validations record the errors under the key of the name of the class holding the rules, and the message is
 * free text returned by {@code getErrorMessage}, which is why the extraction is done here.
 */
public class GlobalValidation {

	private GlobalValidation() {}

	private static final String NOME_DA_FUNCIONALIDADE = "teste de validacoes globais";

	/** Checks that the JSON passes the global validations of the given rules class. */
	public static void accepts(Class<?> rules, CcpJsonRepresentation json) {
		try {
			CcpJsonRepresentation returned = CcpJsonValidatorEngine.INSTANCE.validateJson(rules, json, NOME_DA_FUNCIONALIDADE);
			assertEquals(json, returned);
		} catch (CcpJsonValidationError e) {
			List<String> messages = extractMessages(e, rules);
			fail("The JSON should have passed the global validation, but it was refused with: " + messages);
		}
	}

	/**
	 * Checks that the JSON is refused by the global validations of the rules class and returns the error messages
	 * recorded under the key of that class.
	 */
	public static List<String> refuses(Class<?> rules, CcpJsonRepresentation json) {
		try {
			CcpJsonValidatorEngine.INSTANCE.validateJson(rules, json, NOME_DA_FUNCIONALIDADE);
		} catch (CcpJsonValidationError e) {
			List<String> messages = extractMessages(e, rules);
			boolean withoutMessages = messages.isEmpty();
			assertTrue("The JSON was refused, but no error was recorded for the rules class", false == withoutMessages);
			return messages;
		}

		String ruleNames = rules.getName();
		fail("The JSON should have been refused by the global validations of '" + ruleNames + "', but it passed the validation");
		return new ArrayList<>();
	}

	/** Checks that at least one of the error messages contains the given excerpt. */
	public static void containsMessage(List<String> messages, String trecho) {
		for (String message : messages) {
			boolean contem = message.contains(trecho);
			if(contem) {
				return;
			}
		}
		fail("Era esperada uma mensagem contendo '" + trecho + "', mas as mensagens foram: " + messages);
	}

	/** Checks that none of the error messages contains the given excerpt. */
	public static void doesNotContainMessage(List<String> messages, String trecho) {
		for (String message : messages) {
			boolean contem = message.contains(trecho);
			if(contem) {
				fail("Nao era esperada uma mensagem contendo '" + trecho + "', mas as mensagens foram: " + messages);
			}
		}
	}

	/**
	 * Returns the explanations of the global rules of the given class, the text that goes with the error to tell the
	 * caller what the annotation demands.
	 */
	public static List<String> rulesExplanations(Class<?> rules) {
		CcpJsonRepresentation rulesExplanation = CcpJsonValidationRulesEngine.INSTANCE.getRulesExplanation(rules);
		String ruleNames = rules.getName();
		CcpFieldName key = new CcpFieldName(ruleNames);
		List<Object> explicacoes = rulesExplanation.getAsObjectList(key);
		List<String> messages = new ArrayList<>();
		flatten(explicacoes, messages);
		return messages;
	}

	private static List<String> extractMessages(CcpJsonValidationError e, Class<?> rules) {
		CcpJsonRepresentation errors = e.json.getInnerJson(CcpValidationErrorFields.errors);
		String ruleNames = rules.getName();
		CcpFieldName key = new CcpFieldName(ruleNames);
		List<Object> errorList = errors.getAsObjectList(key);
		List<String> messages = new ArrayList<>();
		flatten(errorList, messages);
		return messages;
	}

	private static void flatten(Collection<?> values, List<String> messages) {
		for (Object value : values) {
			boolean isCollection = value instanceof Collection;
			if(isCollection) {
				Collection<?> collection = (Collection<?>) value;
				flatten(collection, messages);
				continue;
			}
			String message = "" + value;
			messages.add(message);
		}
	}
}
