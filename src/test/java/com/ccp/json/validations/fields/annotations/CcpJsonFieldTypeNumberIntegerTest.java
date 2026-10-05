package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonFieldTypeNumberInteger} honors the minimum, maximum and exact values and the list
 * of allowed values, and refuses what is not an integer.
 */
public class CcpJsonFieldTypeNumberIntegerTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeNumberInteger business = new BusinessFieldTypeNumberInteger();

	private CcpJsonRepresentation json(RulesFieldTypeNumberInteger field, Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, value);
		return json;
	}

	@Test
	public void minValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.valorMinimo, 10);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void minValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.valorMinimo, 9);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberInteger.valorMinimo, CcpJsonFieldTypeError.longNumberMinValue);
	}

	@Test
	public void maxValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.valorMaximo, 20);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void maxValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.valorMaximo, 21);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberInteger.valorMaximo, CcpJsonFieldTypeError.longNumberMaxValue);
	}

	@Test
	public void exactValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.valorExato, 1500);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void exactValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.valorExato, 1501);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberInteger.valorExato, CcpJsonFieldTypeError.longNumberExactValue);
	}

	@Test
	public void allowedValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.valorPermitido, 2);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void allowedValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.valorPermitido, 9);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberInteger.valorPermitido, CcpJsonFieldTypeError.longNumberAllowed);
	}

	@Test
	public void negativeNumberAcceptedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.semRestricao, -42);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void nonNumericTextTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberInteger.semRestricao, "abc");
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberInteger.semRestricao, CcpJsonFieldError.incompatibleType);
	}
}
