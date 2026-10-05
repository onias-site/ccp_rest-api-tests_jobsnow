package com.ccp.json.validations.fields.annotations;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonCopyFieldValidationsFrom} really makes the field inherit the rules of the field
 * with the same name in the source class, even without any validation annotation of its own.
 */
public class CcpJsonCopyFieldValidationsFromTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessCopyFieldValidationsFrom business = new BusinessCopyFieldValidationsFrom();

	private CcpJsonRepresentation json(Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesCopyFieldValidationsFrom.apelido, value);
		return json;
	}

	@Test
	public void valueWithinTheCopiedRulesTest() {
		CcpJsonRepresentation json = this.json("joao");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void minLengthCopiedTest() {
		CcpJsonRepresentation json = this.json("ab");
		FieldValidation.refuses(this.business, json, RulesCopyFieldValidationsFrom.apelido, CcpJsonFieldTypeError.stringMinLength);
	}

	@Test
	public void maxLengthCopiedTest() {
		CcpJsonRepresentation json = this.json("abcdefghij");
		FieldValidation.refuses(this.business, json, RulesCopyFieldValidationsFrom.apelido, CcpJsonFieldTypeError.stringMaxLength);
	}

	/**
	 * Same shape as {@code JnEntityJobsnowError.Fields.cause}: {@code nonRepeatedItems = false} lives on the target
	 * field while the type comes from the source class.
	 */
	@Test
	public void nonRepeatedItemsFalseCoexistsWithCopiedValidationsTest() {
		List<String> value = Arrays.asList("a", "a");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesCopyFieldValidationsFrom.causaComRepetidos, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void nonRepeatedItemsDefaultCoexistsWithCopiedValidationsTest() {
		List<String> value = Arrays.asList("a", "a");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesCopyFieldValidationsFrom.causaSemRepetidos, value);
		FieldValidation.refuses(this.business, json, RulesCopyFieldValidationsFrom.causaSemRepetidos, CcpJsonFieldTypeError.arrayNonReapeted);
	}
}
