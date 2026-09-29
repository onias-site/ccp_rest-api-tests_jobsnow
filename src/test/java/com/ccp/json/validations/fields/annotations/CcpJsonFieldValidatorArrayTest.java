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
 * Verifica se {@code @CcpJsonFieldValidatorArray} respeita tamanho mínimo, máximo, exato e a
 * proibição de itens repetidos, e se recusa um valor que não seja coleção.
 */
public class CcpJsonFieldValidatorArrayTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessValidatorArray business = new BusinessValidatorArray();

	private CcpJsonRepresentation json(RulesValidatorArray field, Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, valor);
		return json;
	}

	@Test
	public void minimoDoisAceitaTest() {
		List<String> valor = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, valor);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void minimoDoisRecusaTest() {
		List<String> valor = Arrays.asList("a");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, valor);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.minimoDois, CcpJsonFieldTypeError.arrayMinSize);
	}

	@Test
	public void maximoDoisAceitaTest() {
		List<String> valor = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.maximoDois, valor);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void maximoDoisRecusaTest() {
		List<String> valor = Arrays.asList("a", "b", "c");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.maximoDois, valor);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.maximoDois, CcpJsonFieldTypeError.arrayMaxSize);
	}

	@Test
	public void exatamenteDoisAceitaTest() {
		List<String> valor = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.exatamenteDois, valor);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void exatamenteDoisRecusaTest() {
		List<String> valor = Arrays.asList("a");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.exatamenteDois, valor);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.exatamenteDois, CcpJsonFieldTypeError.arrayExactSize);
	}

	@Test
	public void semItensRepetidosAceitaTest() {
		List<String> valor = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.semItensRepetidos, valor);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void semItensRepetidosRecusaTest() {
		List<String> valor = Arrays.asList("a", "a");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.semItensRepetidos, valor);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.semItensRepetidos, CcpJsonFieldTypeError.arrayNonReapeted);
	}

	@Test
	public void aceitaItensRepetidosTest() {
		List<String> valor = Arrays.asList("a", "a");
		CcpJsonRepresentation json = this.json(RulesValidatorArray.aceitaItensRepetidos, valor);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void valorQueNaoEhColecaoTest() {
		CcpJsonRepresentation json = this.json(RulesValidatorArray.semItensRepetidos, "a");
		FieldValidation.refuses(this.business, json, RulesValidatorArray.semItensRepetidos, CcpJsonFieldError.incompatibleType);
	}

	@Test
	public void arrayJavaEhTratadoComoColecaoTest() {
		String[] valor = { "a", "b" };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, valor);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void arrayJavaObedeceAsRestricoesDaColecaoTest() {
		String[] valor = { "a" };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, valor);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.minimoDois, CcpJsonFieldTypeError.arrayMinSize);
	}

	@Test
	public void arrayDeTipoPrimitivoEhTratadoComoColecaoTest() {
		int[] valor = { 1, 2 };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.minimoDois, valor);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void arrayDeEnumsEhTratadoComoColecaoTest() {
		RulesFieldTypeString.AllowedValues[] valor = { RulesFieldTypeString.AllowedValues.yes, RulesFieldTypeString.AllowedValues.no };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.colecaoDeEnums, valor);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void arrayComItemForaDosValoresPermitidosTest() {
		String[] valor = { "talvez" };
		CcpJsonRepresentation json = this.json(RulesValidatorArray.colecaoDeEnums, valor);
		FieldValidation.refuses(this.business, json, RulesValidatorArray.colecaoDeEnums, CcpJsonFieldTypeError.stringAllowedValues);
	}
}
