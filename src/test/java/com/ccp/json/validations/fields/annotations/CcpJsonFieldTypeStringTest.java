package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifica se {@code @CcpJsonFieldTypeString} de fato recusa os valores que as suas restrições
 * proíbem e aceita os que elas permitem.
 */
public class CcpJsonFieldTypeStringTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioFieldTypeString negocio = new NegocioFieldTypeString();

	private CcpJsonRepresentation json(RegrasFieldTypeString campo, Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(campo, valor);
		return json;
	}

	@Test
	public void comprimentoMinimoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.comprimentoMinimo, "abc");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void comprimentoMinimoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.comprimentoMinimo, "ab");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeString.comprimentoMinimo, CcpJsonFieldTypeError.stringMinLength);
	}

	@Test
	public void comprimentoMaximoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.comprimentoMaximo, "abcde");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void comprimentoMaximoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.comprimentoMaximo, "abcdef");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeString.comprimentoMaximo, CcpJsonFieldTypeError.stringMaxLength);
	}

	@Test
	public void comprimentoExatoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.comprimentoExato, "abcd");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void comprimentoExatoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.comprimentoExato, "abcde");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeString.comprimentoExato, CcpJsonFieldTypeError.stringExactLength);
	}

	@Test
	public void naoAceitaVazioRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.naoAceitaVazio, "");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeString.naoAceitaVazio, CcpJsonFieldTypeError.stringNotEmpty);
	}

	@Test
	public void aceitaVazioTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.aceitaVazio, "");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void regexAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.apenasTresDigitos, "123");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void regexRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.apenasTresDigitos, "12a");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeString.apenasTresDigitos, CcpJsonFieldTypeError.stringRegex);
	}

	@Test
	public void valorPermitidoAceitaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.valorPermitido, "sim");
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorPermitidoRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.valorPermitido, "talvez");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeString.valorPermitido, CcpJsonFieldTypeError.stringAllowedValues);
	}

	@Test
	public void nomeDeClasseJavaAceitaTest() {
		String nomeDeClasse = CcpJsonRepresentation.class.getName();
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.nomeDeClasseJava, nomeDeClasse);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void nomeDeClasseJavaAninhadaAceitaTest() {
		String nomeDeClasse = RegrasFieldTypeString.ValoresPermitidos.class.getName();
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.nomeDeClasseJava, nomeDeClasse);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void nomeDeClasseJavaInexistenteRecusaTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.nomeDeClasseJava, "com.ccp.nao.existe.ClasseInexistente");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeString.nomeDeClasseJava, CcpJsonFieldTypeError.stringJavaClass);
	}

	@Test
	public void nomeDeClasseJavaSemPacoteRecusaTest() {
		String nomeSimples = CcpJsonRepresentation.class.getSimpleName();
		CcpJsonRepresentation json = this.json(RegrasFieldTypeString.nomeDeClasseJava, nomeSimples);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeString.nomeDeClasseJava, CcpJsonFieldTypeError.stringJavaClass);
	}
}
