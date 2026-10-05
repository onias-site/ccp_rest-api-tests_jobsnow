package com.ccp.json.validations.fields.annotations;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonFieldValidatorArray} honors the minimum, maximum and exact sizes and the
 * prohibition of repeated items, and refuses a value that is not a collection.
 */
public class CcpJsonFieldValidatorArrayTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessValidatorArray business = new BusinessValidatorArray();

	private CcpJsonRepresentation json(RulesValidatorArray field, Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, value);
		return json;
	}

	@Test
	public void minimumTwoAcceptedTest() {
		List<String> value = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void minimumTwoRefusedTest() {
		List<String> value = Arrays.asList("a");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, value);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.minimoDois, CcpJsonFieldTypeError.arrayMinSize);
	}

	@Test
	public void maximumTwoAcceptedTest() {
		List<String> value = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.maximoDois, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void maximumTwoRefusedTest() {
		List<String> value = Arrays.asList("a", "b", "c");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.maximoDois, value);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.maximoDois, CcpJsonFieldTypeError.arrayMaxSize);
	}

	@Test
	public void exactlyTwoAcceptedTest() {
		List<String> value = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.exatamenteDois, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void exactlyTwoRefusedTest() {
		List<String> value = Arrays.asList("a");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.exatamenteDois, value);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.exatamenteDois, CcpJsonFieldTypeError.arrayExactSize);
	}

	@Test
	public void withoutRepeatedItemsAcceptedTest() {
		List<String> value = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.semItensRepetidos, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void withoutRepeatedItemsRefusedTest() {
		List<String> value = Arrays.asList("a", "a");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.semItensRepetidos, value);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.semItensRepetidos, CcpJsonFieldTypeError.arrayNonReapeted);
	}

	@Test
	public void acceptsRepeatedItemsTest() {
		List<String> value = Arrays.asList("a", "a");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.aceitaItensRepetidos, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void valueThatIsNotACollectionTest() {
		CcpJsonRepresentation json = this.json(RulesValidatorArray.semItensRepetidos, "a");
		FieldValidation.refuses(this.business, json, RulesValidatorArray.semItensRepetidos, CcpJsonFieldError.incompatibleType);
	}

	@Test
	public void javaArrayIsTreatedAsCollectionTest() {
		String[] value = { "a", "b" };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void javaArrayObeysTheCollectionRestrictionsTest() {
		String[] value = { "a" };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, value);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.minimoDois, CcpJsonFieldTypeError.arrayMinSize);
	}

	@Test
	public void arrayOfPrimitiveTypeIsTreatedAsCollectionTest() {
		int[] value = { 1, 2 };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void arrayOfEnumsIsTreatedAsCollectionTest() {
		RulesFieldTypeString.AllowedValues[] value = { RulesFieldTypeString.AllowedValues.yes, RulesFieldTypeString.AllowedValues.no };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.colecaoDeEnums, value);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void aSingleValueForACollectionFieldIsRefusedAsAnIncompatibleType() {
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, "alone");

		String description = FieldValidation.refuses(this.business, json, RulesValidatorArray.minimoDois, CcpJsonFieldError.incompatibleType);

		org.junit.Assert.assertTrue(description, description.contains("must be a collection"));
	}

	@Test
	public void aCollectionForASingleValueFieldIsRefusedWithItsItems() {
		CcpJsonRepresentation json = this.json(RulesValidatorArray.singleValue, Arrays.asList("a", "b"));

		String description = FieldValidation.refuses(this.business, json, RulesValidatorArray.singleValue, CcpJsonFieldError.validateCollectionOrSigleValue);

		org.junit.Assert.assertTrue(description, description.contains("that can not be a collection"));
	}

	@Test
	public void arrayWithItemOutsideAllowedValuesTest() {
		String[] value = { "talvez" };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.colecaoDeEnums, value);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.colecaoDeEnums, CcpJsonFieldTypeError.stringAllowedValues);
	}
}
