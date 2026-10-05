package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonFieldTypeString} really refuses the values its restrictions forbid and accepts
 * the ones they allow.
 */
public class CcpJsonFieldTypeStringTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeString business = new BusinessFieldTypeString();

	private CcpJsonRepresentation json(RulesFieldTypeString field, Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, value);
		return json;
	}

	@Test
	public void minLengthAcceptedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.comprimentoMinimo, "abc");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void minLengthRefusedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.comprimentoMinimo, "ab");
		FieldValidation.refuses(this.business, json, RulesFieldTypeString.comprimentoMinimo, CcpJsonFieldTypeError.stringMinLength);
	}

	@Test
	public void maxLengthAcceptedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.comprimentoMaximo, "abcde");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void maxLengthRefusedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.comprimentoMaximo, "abcdef");
		FieldValidation.refuses(this.business, json, RulesFieldTypeString.comprimentoMaximo, CcpJsonFieldTypeError.stringMaxLength);
	}

	@Test
	public void exactLengthAcceptedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.comprimentoExato, "abcd");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void exactLengthRefusedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.comprimentoExato, "abcde");
		FieldValidation.refuses(this.business, json, RulesFieldTypeString.comprimentoExato, CcpJsonFieldTypeError.stringExactLength);
	}

	@Test
	public void doesNotAcceptEmptyRefusedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.naoAceitaVazio, "");
		FieldValidation.refuses(this.business, json, RulesFieldTypeString.naoAceitaVazio, CcpJsonFieldTypeError.stringNotEmpty);
	}

	@Test
	public void acceptsEmptyTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.aceitaVazio, "");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void regexAcceptedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.apenasTresDigitos, "123");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void regexRefusedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.apenasTresDigitos, "12a");
		FieldValidation.refuses(this.business, json, RulesFieldTypeString.apenasTresDigitos, CcpJsonFieldTypeError.stringRegex);
	}

	@Test
	public void allowedValueAcceptsTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.valorPermitido, "yes");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void allowedValueRefusesTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.valorPermitido, "talvez");
		FieldValidation.refuses(this.business, json, RulesFieldTypeString.valorPermitido, CcpJsonFieldTypeError.stringAllowedValues);
	}

	@Test
	public void javaClassNameAcceptedTest() {
		String className = CcpJsonRepresentation.class.getName();
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.nomeDeClasseJava, className);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void nestedJavaClassNameAcceptedTest() {
		String className = RulesFieldTypeString.AllowedValues.class.getName();
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.nomeDeClasseJava, className);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void missingJavaClassNameRefusedTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.nomeDeClasseJava, "com.ccp.nao.existe.ClasseInexistente");
		FieldValidation.refuses(this.business, json, RulesFieldTypeString.nomeDeClasseJava, CcpJsonFieldTypeError.stringJavaClass);
	}

	@Test
	public void javaClassNameWithoutPackageRefusedTest() {
		String simpleName = CcpJsonRepresentation.class.getSimpleName();
		CcpJsonRepresentation json = this.json(RulesFieldTypeString.nomeDeClasseJava, simpleName);
		FieldValidation.refuses(this.business, json, RulesFieldTypeString.nomeDeClasseJava, CcpJsonFieldTypeError.stringJavaClass);
	}
}
