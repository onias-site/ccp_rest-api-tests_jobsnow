package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;

/** Verifies that {@code @CcpJsonFieldTypeBoolean} accepts booleans and refuses what is not a boolean. */
public class CcpJsonFieldTypeBooleanTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeBoolean business = new BusinessFieldTypeBoolean();

	private CcpJsonRepresentation json(Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesFieldTypeBoolean.ativo, value);
		return json;
	}

	@Test
	public void trueTest() {
		CcpJsonRepresentation json = this.json(true);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void falseTest() {
		CcpJsonRepresentation json = this.json(false);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void textIsNotBooleanTest() {
		CcpJsonRepresentation json = this.json("talvez");
		FieldValidation.refuses(this.business, json, RulesFieldTypeBoolean.ativo, CcpJsonFieldError.incompatibleType);
	}

	@Test
	public void numberIsNotBooleanTest() {
		CcpJsonRepresentation json = this.json(7);
		FieldValidation.refuses(this.business, json, RulesFieldTypeBoolean.ativo, CcpJsonFieldError.incompatibleType);
	}
}
