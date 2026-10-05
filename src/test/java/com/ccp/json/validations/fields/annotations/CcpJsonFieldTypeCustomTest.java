package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;

/**
 * Verifies that {@code @CcpJsonFieldTypeCustom} instantiates the given class and applies the compatibility rule
 * it defines.
 */
public class CcpJsonFieldTypeCustomTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeCustom business = new BusinessFieldTypeCustom();

	private CcpJsonRepresentation json(Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesFieldTypeCustom.apenasVogais, value);
		return json;
	}

	@Test
	public void onlyVowelsAcceptedTest() {
		CcpJsonRepresentation json = this.json("aeiou");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void withConsonantRefusedTest() {
		CcpJsonRepresentation json = this.json("aeixou");
		FieldValidation.refuses(this.business, json, RulesFieldTypeCustom.apenasVogais, CcpJsonFieldError.incompatibleType);
	}
}
