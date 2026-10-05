package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonFieldTypeNumberUnsigned} honors the numeric restrictions and, what is particular
 * to this type, refuses negative values.
 */
public class CcpJsonFieldTypeNumberUnsignedTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeNumberUnsigned business = new BusinessFieldTypeNumberUnsigned();

	private CcpJsonRepresentation json(RulesFieldTypeNumberUnsigned field, Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, value);
		return json;
	}

	@Test
	public void minValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.valorMinimo, 10);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void minValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.valorMinimo, 9);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberUnsigned.valorMinimo, CcpJsonFieldTypeError.unsignedNumberMinValue);
	}

	@Test
	public void maxValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.valorMaximo, 20);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void maxValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.valorMaximo, 21);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberUnsigned.valorMaximo, CcpJsonFieldTypeError.unsignedNumberMaxValue);
	}

	@Test
	public void exactValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.valorExato, 1500);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void exactValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.valorExato, 1501);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberUnsigned.valorExato, CcpJsonFieldTypeError.unsignedNumberExactValue);
	}

	@Test
	public void allowedValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.valorPermitido, 2);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void allowedValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.valorPermitido, 9);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberUnsigned.valorPermitido, CcpJsonFieldTypeError.unsignedNumberAllowed);
	}

	@Test
	public void zeroAcceptedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.semRestricao, 0);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void negativeNumberRefusedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNumberUnsigned.semRestricao, -1);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNumberUnsigned.semRestricao, CcpJsonFieldError.incompatibleType);
	}
}
