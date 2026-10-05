package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonFieldTypeNestedJson} validates the inner JSON with the class given in
 * {@code jsonValidation} and honors the prohibition of an empty inner JSON.
 */
public class CcpJsonFieldTypeNestedJsonTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeNestedJson business = new BusinessFieldTypeNestedJson();

	private CcpJsonRepresentation json(RulesFieldTypeNestedJson field, Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, value);
		return json;
	}

	@Test
	public void validInnerJsonTest() {
		CcpJsonRepresentation inner = CcpOtherConstants.EMPTY_JSON.put(RulesNestedAddress.rua, "Rua das Flores");
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.endereco, inner);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void innerJsonWithRequiredFieldAbsentTest() {
		CcpJsonRepresentation inner = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.endereco, inner);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNestedJson.endereco, CcpJsonFieldTypeError.nestedJson);
	}

	@Test
	public void innerJsonWithFieldBreakingTheRuleTest() {
		CcpJsonRepresentation inner = CcpOtherConstants.EMPTY_JSON.put(RulesNestedAddress.rua, "ab");
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.endereco, inner);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNestedJson.endereco, CcpJsonFieldTypeError.nestedJson);
	}

	@Test
	public void emptyInnerJsonRefusedTest() {
		CcpJsonRepresentation inner = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.naoAceitaVazio, inner);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNestedJson.naoAceitaVazio, CcpJsonFieldTypeError.emptyJson);
	}

	@Test
	public void emptyInnerJsonAcceptedTest() {
		CcpJsonRepresentation inner = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.aceitaVazio, inner);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void valueThatIsNotAJsonTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.aceitaVazio, "abc");
		FieldValidation.refuses(this.business, json, RulesFieldTypeNestedJson.aceitaVazio, CcpJsonFieldError.incompatibleType);
	}
}
