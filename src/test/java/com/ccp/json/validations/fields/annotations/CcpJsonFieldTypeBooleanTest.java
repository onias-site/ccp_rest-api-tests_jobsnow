package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;

/**
 * Verifica se {@code @CcpJsonFieldTypeBoolean} aceita booleanos e recusa o que não é booleano.
 */
public class CcpJsonFieldTypeBooleanTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeBoolean business = new BusinessFieldTypeBoolean();

	private CcpJsonRepresentation json(Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesFieldTypeBoolean.ativo, valor);
		return json;
	}

	@Test
	public void verdadeiroTest() {
		CcpJsonRepresentation json = this.json(true);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void falsoTest() {
		CcpJsonRepresentation json = this.json(false);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void textoNaoBooleanoTest() {
		CcpJsonRepresentation json = this.json("talvez");
		FieldValidation.refuses(this.business, json, RulesFieldTypeBoolean.ativo, CcpJsonFieldError.incompatibleType);
	}

	@Test
	public void numeroNaoBooleanoTest() {
		CcpJsonRepresentation json = this.json(7);
		FieldValidation.refuses(this.business, json, RulesFieldTypeBoolean.ativo, CcpJsonFieldError.incompatibleType);
	}
}
