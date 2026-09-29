package com.ccp.json.defaultvalues.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.defaultvalues.business.CcpJsonFieldDefaultValueDoNothing;
import com.ccp.json.validations.fields.annotations.FieldValidation;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldValidatorInterface.RuleFields;
import com.ccp.json.validations.global.engine.CcpJsonValidationRulesEngine;

/**
 * Verifica {@code @CcpJsonFieldDefaultValue}: o preenchimento do campo ausente pelo
 * {@code defaultStrings} (um item vira String, vários viram lista, e todos passam por
 * {@code resolveTemplate}), o acionamento do {@code jsonProducer} quando {@code defaultStrings} está
 * vazio, a preservação do valor que já veio no JSON e o desligamento da obrigatoriedade do campo.
 */
public class CcpJsonFieldDefaultValueTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private CcpJsonRepresentation withName() {
		CcpJsonRepresentation withName = CcpOtherConstants.EMPTY_JSON.put(RulesDefaultValueStrings.name, "onias");
		return withName;
	}

	// ── defaultStrings ────────────────────────────────────────────────────────

	@Test
	public void umItemSoViraStringTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		String valor = resultado.getAsString(RulesDefaultValueStrings.umItemSo);
		assertEquals("valor unico", valor);
	}

	@Test
	public void umItemSoEhGravadoComoStringENaoComoListaTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		Object valor = resultado.get(RulesDefaultValueStrings.umItemSo);
		boolean ehString = valor instanceof String;
		assertTrue("Um unico defaultString deve ser gravado como String, mas veio " + valor.getClass(), ehString);
	}

	@Test
	public void variosItensViramListaDeStringsTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		List<Object> valor = resultado.getAsObjectList(RulesDefaultValueStrings.variosItens);
		List<String> esperado = Arrays.asList("primeiro", "segundo", "terceiro");
		assertEquals(esperado, valor);
	}

	@Test
	public void variosItensSaoGravadosComoColecaoENaoComoStringTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		Object valor = resultado.get(RulesDefaultValueStrings.variosItens);
		boolean ehColecao = valor instanceof List;
		assertTrue("Varios defaultStrings devem virar lista, mas veio " + valor.getClass(), ehColecao);
	}

	@Test
	public void templateEhResolvidoComOsValoresDoJsonTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		String valor = resultado.getAsString(RulesDefaultValueStrings.comTemplate);
		assertEquals("ola onias", valor);
	}

	/**
	 * Placeholder que aponta para campo ausente não tem o que substituir e permanece literal. É a
	 * armadilha da auto-referência: um campo cujo valor padrão aponte para ele mesmo nunca resolve,
	 * porque o valor padrão só roda quando o campo está ausente.
	 */
	@Test
	public void templateSemOrigemPermaneceLiteralTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		String valor = resultado.getAsString(RulesDefaultValueStrings.comTemplateSemOrigem);
		assertEquals("[{inexistente}]", valor);
	}

	@Test
	public void campoJaInformadoNaoEhSobrescritoTest() {
		CcpJsonRepresentation withName = this.withName();
		CcpJsonRepresentation json = withName.put(RulesDefaultValueStrings.umItemSo, "valor que veio de fora");
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		String valor = resultado.getAsString(RulesDefaultValueStrings.umItemSo);
		assertEquals("valor que veio de fora", valor);
	}

	@Test
	public void campoSemAnotacaoContinuaAusenteTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		boolean contem = resultado.containsField(RulesDefaultValueStrings.semValorPadrao);
		assertFalse("Campo sem a anotacao nao pode ganhar valor padrao", contem);
	}

	@Test
	public void jsonDeEntradaNaoEhAlteradoTest() {
		CcpJsonRepresentation json = this.withName();
		FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		boolean contem = json.containsField(RulesDefaultValueStrings.umItemSo);
		assertFalse("O json de entrada deve permanecer intacto", contem);
	}

	// ── jsonProducer ──────────────────────────────────────────────────────────

	@Test
	public void jsonProducerEhAcionadoQuandoDefaultStringsEstaVazioTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesDefaultValueJsonProducer.name, "onias");
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueJsonProducer.class, json);
		String valor = resultado.getAsString(RulesDefaultValueJsonProducer.produzido);
		assertEquals("produzido para onias", valor);
	}

	@Test
	public void jsonProducerNaoEhAcionadoQuandoOCampoJaVeioTest() {
		CcpJsonRepresentation withName = CcpOtherConstants.EMPTY_JSON.put(RulesDefaultValueJsonProducer.name, "onias");
		CcpJsonRepresentation json = withName.put(RulesDefaultValueJsonProducer.produzido, "valor que veio de fora");
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueJsonProducer.class, json);
		String valor = resultado.getAsString(RulesDefaultValueJsonProducer.produzido);
		assertEquals("valor que veio de fora", valor);
	}

	@Test
	public void jsonProducerPadraoNaoDefineValorNenhumTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesDefaultValueJsonProducer.name, "onias");
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueJsonProducer.class, json);
		boolean contem = resultado.containsField(RulesDefaultValueJsonProducer.inerte);
		assertFalse("A anotacao sem atributos cai no produtor padrao, que nao define valor algum", contem);
	}

	@Test
	public void produtorPadraoDevolveOMesmoJsonTest() {
		CcpBusiness doNothing = new CcpJsonFieldDefaultValueDoNothing();
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation retorno = doNothing.execute(json);
		assertSame(json, retorno);
	}

	// ── atributos e valores padrão da própria anotação ────────────────────────

	@Test
	public void defaultStringsPadraoEhArrayVazioTest() throws Exception {
		CcpJsonFieldDefaultValue anotacao = this.anotacaoDe(RulesDefaultValueJsonProducer.class, "inerte");
		String[] defaultStrings = anotacao.defaultStrings();
		assertEquals(0, defaultStrings.length);
	}

	@Test
	public void jsonProducerPadraoEhODoNothingTest() throws Exception {
		CcpJsonFieldDefaultValue anotacao = this.anotacaoDe(RulesDefaultValueJsonProducer.class, "inerte");
		Class<? extends CcpBusiness> jsonProducer = anotacao.jsonProducer();
		assertEquals(CcpJsonFieldDefaultValueDoNothing.class, jsonProducer);
	}

	private CcpJsonFieldDefaultValue anotacaoDe(Class<?> rulesClass, String nomeDoCampo) throws Exception {
		Field field = rulesClass.getDeclaredField(nomeDoCampo);
		CcpJsonFieldDefaultValue anotacao = field.getAnnotation(CcpJsonFieldDefaultValue.class);
		return anotacao;
	}

	// ── desligamento da obrigatoriedade ───────────────────────────────────────

	@Test
	public void campoObrigatorioComValorPadraoNaoEhMaisExigidoTest() {
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueWithRequired.class, CcpOtherConstants.EMPTY_JSON);
		String valor = resultado.getAsString(RulesDefaultValueWithRequired.obrigatorioComValorPadrao);
		assertEquals("preenchido pelo valor padrao", valor);
	}

	@Test
	public void chavePrimariaComValorPadraoNaoEhMaisExigidaTest() {
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesDefaultValueWithRequired.class, CcpOtherConstants.EMPTY_JSON);
		String valor = resultado.getAsString(RulesDefaultValueWithRequired.chavePrimariaComValorPadrao);
		assertEquals("chave gerada", valor);
	}

	@Test
	public void campoObrigatorioSemValorPadraoContinuaSendoExigidoTest() {
		DefaultValueBusiness business = new DefaultValueBusiness(RulesRequiredWithoutDefaultValue.class);
		FieldValidation.refuses(business, CcpOtherConstants.EMPTY_JSON, RulesRequiredWithoutDefaultValue.obrigatorioSemValorPadrao, CcpJsonFieldError.requiredFieldIsMissing);
	}

	/**
	 * O desligamento vale também para a explicação de regras: não faz sentido documentar como
	 * obrigatório um campo que o framework preenche sozinho.
	 */
	@Test
	public void regraDeObrigatoriedadeSomeDaExplicacaoTest() {
		List<String> regras = this.regrasDoCampo(RulesDefaultValueWithRequired.class, RulesDefaultValueWithRequired.obrigatorioComValorPadrao);
		String nomeDaRegra = CcpJsonFieldError.requiredFieldIsMissing.getValue();
		boolean contem = regras.contains(nomeDaRegra);
		assertFalse("A regra de obrigatoriedade nao deveria aparecer, mas as regras foram: " + regras, contem);
	}

	@Test
	public void regraDeObrigatoriedadeContinuaSemValorPadraoTest() {
		List<String> regras = this.regrasDoCampo(RulesRequiredWithoutDefaultValue.class, RulesRequiredWithoutDefaultValue.obrigatorioSemValorPadrao);
		String nomeDaRegra = CcpJsonFieldError.requiredFieldIsMissing.getValue();
		boolean contem = regras.contains(nomeDaRegra);
		assertTrue("A regra de obrigatoriedade deveria aparecer, mas as regras foram: " + regras, contem);
	}

	private List<String> regrasDoCampo(Class<?> rulesClass, CcpJsonFieldName field) {

		CcpJsonRepresentation explicacao = CcpJsonValidationRulesEngine.INSTANCE.getRulesExplanation(rulesClass);
		List<CcpJsonRepresentation> regrasDoCampo = explicacao.getAsJsonList(field);
		List<String> nomes = new ArrayList<>();

		for (CcpJsonRepresentation regra : regrasDoCampo) {
			String ruleName = regra.getAsString(RuleFields.ruleName);
			nomes.add(ruleName);
		}

		return nomes;
	}

	// ── valor padrão herdado por CcpJsonCopyFieldValidationsFrom ───────────────

	/**
	 * Forma real do {@code fileName}: o {@code required} fica no campo local e o valor padrão na
	 * classe de origem. Se a anotação fosse procurada só no campo local, a obrigatoriedade não seria
	 * desligada e nenhum valor padrão seria aplicado.
	 */
	@Test
	public void valorPadraoHerdadoDaClasseDeOrigemTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesCopiedDefaultValue.origem, "curriculo");
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesCopiedDefaultValue.class, json);
		String valor = resultado.getAsString(RulesCopiedDefaultValue.copiado);
		assertEquals("derivado de curriculo", valor);
	}

	@Test
	public void campoHerdadoJaInformadoNaoEhSobrescritoTest() {
		CcpJsonRepresentation comOrigem = CcpOtherConstants.EMPTY_JSON.put(RulesCopiedDefaultValue.origem, "curriculo");
		CcpJsonRepresentation json = comOrigem.put(RulesCopiedDefaultValue.copiado, "valor que veio de fora");
		CcpJsonRepresentation resultado = FieldDefaultValue.applyDefaultValues(RulesCopiedDefaultValue.class, json);
		String valor = resultado.getAsString(RulesCopiedDefaultValue.copiado);
		assertEquals("valor que veio de fora", valor);
	}
}
