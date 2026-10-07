package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonFieldTypeNumber} honors the minimum, maximum and exact values and the list of
 * allowed values, and refuses what is not a number.
 */
public class CcpJsonFieldTypeNumberTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeNumber business = new BusinessFieldTypeNumber();

	private CcpJsonRepresentation json(RulesFieldTypeNumber field, Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, value);
		return json;
	}

	@Test
	public void minValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.valorMinimo, 5.5);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void minValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.valorMinimo, 5.4);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumber.valorMinimo, CcpJsonFieldTypeError.doubleNumberMinValue);
	}

	@Test
	public void maxValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.valorMaximo, 10.5);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void maxValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.valorMaximo, 10.6);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumber.valorMaximo, CcpJsonFieldTypeError.doubleNumberMaxValue);
	}

	@Test
	public void exactValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.valorExato, 7.5);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void exactValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.valorExato, 7.6);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumber.valorExato, CcpJsonFieldTypeError.doubleNumberExactValue);
	}

	@Test
	public void allowedValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.valorPermitido, 1.5);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void allowedValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.valorPermitido, 9.9);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumber.valorPermitido, CcpJsonFieldTypeError.doubleNumberAllowed);
	}

	@Test
	public void withoutRestrictionAcceptedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.semRestricao, 123.456);
		FieldValidation.accepts(this.business, json);
	}

	/**
	 * Without a configured minimum, zero and negatives are numbers like any other. Until 2026-10-06 the unconfigured
	 * minimum ran in this path and compared the value with {@code Double.MIN_VALUE}, the smallest positive double.
	 */
	@Test
	public void withoutRestrictionAcceptsZeroAndNegativesTest() {
		FieldValidation.accepts(this.business, this.json(RulesFieldTypeNumber.semRestricao, 0));
		FieldValidation.accepts(this.business, this.json(RulesFieldTypeNumber.semRestricao, -5.5));
	}

	/** A minimum of zero used to be taken as "not configured" (it is not greater than {@code Double.MIN_VALUE}). */
	@Test
	public void minValueOfZeroIsHonoredTest() {
		FieldValidation.accepts(this.business, this.json(RulesFieldTypeNumber.minimoZero, 0));
		FieldValidation.refuses(this.business, this.json(RulesFieldTypeNumber.minimoZero, -0.1), RulesFieldTypeNumber.minimoZero, CcpJsonFieldTypeError.doubleNumberMinValue);
	}

	@Test
	public void negativeMinValueIsHonoredTest() {
		FieldValidation.accepts(this.business, this.json(RulesFieldTypeNumber.minimoNegativo, -10));
		FieldValidation.refuses(this.business, this.json(RulesFieldTypeNumber.minimoNegativo, -10.5), RulesFieldTypeNumber.minimoNegativo, CcpJsonFieldTypeError.doubleNumberMinValue);
	}

	/** An exact value of zero used to be taken as "not configured" too. */
	@Test
	public void exactValueOfZeroIsHonoredTest() {
		FieldValidation.accepts(this.business, this.json(RulesFieldTypeNumber.exatoZero, 0));
		FieldValidation.refuses(this.business, this.json(RulesFieldTypeNumber.exatoZero, 1), RulesFieldTypeNumber.exatoZero, CcpJsonFieldTypeError.doubleNumberExactValue);
	}

	@Test
	public void nonNumericTextTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumber.semRestricao, "abc");
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumber.semRestricao, CcpJsonFieldError.incompatibleType);
	}
}
