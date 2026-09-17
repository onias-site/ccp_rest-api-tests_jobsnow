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

	private final NegocioValidatorArray negocio = new NegocioValidatorArray();

	private CcpJsonRepresentation json(RegrasValidatorArray campo, Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(campo, valor);
		return json;
	}

	@Test
	public void minimoDoisAceitaTest() {
		List<String> valor = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.minimoDois, valor);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void minimoDoisRecusaTest() {
		List<String> valor = Arrays.asList("a");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.minimoDois, valor);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasValidatorArray.minimoDois, CcpJsonFieldTypeError.arrayMinSize);
	}

	@Test
	public void maximoDoisAceitaTest() {
		List<String> valor = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.maximoDois, valor);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void maximoDoisRecusaTest() {
		List<String> valor = Arrays.asList("a", "b", "c");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.maximoDois, valor);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasValidatorArray.maximoDois, CcpJsonFieldTypeError.arrayMaxSize);
	}

	@Test
	public void exatamenteDoisAceitaTest() {
		List<String> valor = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.exatamenteDois, valor);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void exatamenteDoisRecusaTest() {
		List<String> valor = Arrays.asList("a");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.exatamenteDois, valor);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasValidatorArray.exatamenteDois, CcpJsonFieldTypeError.arrayExactSize);
	}

	@Test
	public void semItensRepetidosAceitaTest() {
		List<String> valor = Arrays.asList("a", "b");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.semItensRepetidos, valor);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void semItensRepetidosRecusaTest() {
		List<String> valor = Arrays.asList("a", "a");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.semItensRepetidos, valor);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasValidatorArray.semItensRepetidos, CcpJsonFieldTypeError.arrayNonReapeted);
	}

	@Test
	public void aceitaItensRepetidosTest() {
		List<String> valor = Arrays.asList("a", "a");
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.aceitaItensRepetidos, valor);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorQueNaoEhColecaoTest() {
		CcpJsonRepresentation json = this.json(RegrasValidatorArray.semItensRepetidos, "a");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasValidatorArray.semItensRepetidos, CcpJsonFieldError.incompatibleType);
	}
}
