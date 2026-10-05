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
 * Verifies {@code @CcpJsonFieldDefaultValue}: the absent field is filled by {@code defaultStrings} (one item
 * becomes a String, several become a list, and all go through {@code resolveTemplate}), {@code jsonProducer} is
 * triggered when {@code defaultStrings} is empty, a value already in the JSON is kept, and the field stops being
 * required.
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
	public void oneItemOnlyBecomesStringTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		String value = result.getAsString(RulesDefaultValueStrings.umItemSo);
		assertEquals("valor unico", value);
	}

	@Test
	public void oneItemOnlyIsStoredAsStringAndNotAsListTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		Object value = result.get(RulesDefaultValueStrings.umItemSo);
		boolean isString = value instanceof String;
		assertTrue("A single defaultString must be stored as a String, but it came as " + value.getClass(), isString);
	}

	@Test
	public void severalItemsBecomeListOfStringsTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		List<Object> value = result.getAsObjectList(RulesDefaultValueStrings.variosItens);
		List<String> expected = Arrays.asList("primeiro", "segundo", "terceiro");
		assertEquals(expected, value);
	}

	@Test
	public void severalItemsAreStoredAsCollectionAndNotAsStringTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		Object value = result.get(RulesDefaultValueStrings.variosItens);
		boolean isCollection = value instanceof List;
		assertTrue("Several defaultStrings must become a list, but it came as " + value.getClass(), isCollection);
	}

	@Test
	public void templateIsResolvedWithTheValuesOfTheJsonTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		String value = result.getAsString(RulesDefaultValueStrings.comTemplate);
		assertEquals("ola onias", value);
	}

	/**
	 * A placeholder that points to an absent field has nothing to replace and stays literal. This is the
	 * self-reference trap: a field whose default value points to itself never resolves, because the default value
	 * only runs when the field is absent.
	 */
	@Test
	public void templateWithoutSourceStaysLiteralTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		String value = result.getAsString(RulesDefaultValueStrings.comTemplateSemOrigem);
		assertEquals("[{inexistente}]", value);
	}

	@Test
	public void fieldAlreadyGivenIsNotOverwrittenTest() {
		CcpJsonRepresentation withName = this.withName();
		CcpJsonRepresentation json = withName.put(RulesDefaultValueStrings.umItemSo, "valor que veio de fora");
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		String value = result.getAsString(RulesDefaultValueStrings.umItemSo);
		assertEquals("valor que veio de fora", value);
	}

	@Test
	public void fieldWithoutAnnotationStaysAbsentTest() {
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		boolean present = result.containsField(RulesDefaultValueStrings.semValorPadrao);
		assertFalse("A field without the annotation cannot get a default value", present);
	}

	@Test
	public void inputJsonIsNotChangedTest() {
		CcpJsonRepresentation json = this.withName();
		FieldDefaultValue.applyDefaultValues(RulesDefaultValueStrings.class, json);
		boolean present = json.containsField(RulesDefaultValueStrings.umItemSo);
		assertFalse("The input JSON must stay untouched", present);
	}

	// ── jsonProducer ──────────────────────────────────────────────────────────

	@Test
	public void jsonProducerIsTriggeredWhenDefaultStringsIsEmptyTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesDefaultValueJsonProducer.name, "onias");
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueJsonProducer.class, json);
		String value = result.getAsString(RulesDefaultValueJsonProducer.produzido);
		assertEquals("produzido para onias", value);
	}

	@Test
	public void jsonProducerIsNotTriggeredWhenTheFieldIsAlreadyThereTest() {
		CcpJsonRepresentation withName = CcpOtherConstants.EMPTY_JSON.put(RulesDefaultValueJsonProducer.name, "onias");
		CcpJsonRepresentation json = withName.put(RulesDefaultValueJsonProducer.produzido, "valor que veio de fora");
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueJsonProducer.class, json);
		String value = result.getAsString(RulesDefaultValueJsonProducer.produzido);
		assertEquals("valor que veio de fora", value);
	}

	@Test
	public void jsonProducerDefaultSetsNoValueTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesDefaultValueJsonProducer.name, "onias");
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueJsonProducer.class, json);
		boolean present = result.containsField(RulesDefaultValueJsonProducer.inerte);
		assertFalse("The annotation without attributes falls into the default producer, which sets no value", present);
	}

	@Test
	public void defaultProducerReturnsTheSameJsonTest() {
		CcpBusiness doNothing = new CcpJsonFieldDefaultValueDoNothing();
		CcpJsonRepresentation json = this.withName();
		CcpJsonRepresentation returned = doNothing.execute(json);
		assertSame(json, returned);
	}

	// ── attributes and default values of the annotation itself ────────────────

	@Test
	public void defaultStringsDefaultIsEmptyArrayTest() throws Exception {
		CcpJsonFieldDefaultValue annotation = this.annotationOf(RulesDefaultValueJsonProducer.class, "inerte");
		String[] defaultStrings = annotation.defaultStrings();
		assertEquals(0, defaultStrings.length);
	}

	@Test
	public void jsonProducerDefaultIsDoNothingTest() throws Exception {
		CcpJsonFieldDefaultValue annotation = this.annotationOf(RulesDefaultValueJsonProducer.class, "inerte");
		Class<? extends CcpBusiness> jsonProducer = annotation.jsonProducer();
		assertEquals(CcpJsonFieldDefaultValueDoNothing.class, jsonProducer);
	}

	private CcpJsonFieldDefaultValue annotationOf(Class<?> rulesClass, String fieldName) throws Exception {
		Field field = rulesClass.getDeclaredField(fieldName);
		CcpJsonFieldDefaultValue annotation = field.getAnnotation(CcpJsonFieldDefaultValue.class);
		return annotation;
	}

	// ── desligamento da obrigatoriedade ───────────────────────────────────────

	@Test
	public void requiredFieldWithDefaultValueIsNoLongerDemandedTest() {
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueWithRequired.class, CcpOtherConstants.EMPTY_JSON);
		String value = result.getAsString(RulesDefaultValueWithRequired.obrigatorioComValorPadrao);
		assertEquals("preenchido pelo valor padrao", value);
	}

	@Test
	public void primaryKeyWithDefaultValueIsNoLongerDemandedTest() {
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesDefaultValueWithRequired.class, CcpOtherConstants.EMPTY_JSON);
		String value = result.getAsString(RulesDefaultValueWithRequired.chavePrimariaComValorPadrao);
		assertEquals("chave gerada", value);
	}

	@Test
	public void requiredFieldWithoutDefaultValueIsStillDemandedTest() {
		DefaultValueBusiness business = new DefaultValueBusiness(RulesRequiredWithoutDefaultValue.class);
		FieldValidation.refuses(business, CcpOtherConstants.EMPTY_JSON, RulesRequiredWithoutDefaultValue.obrigatorioSemValorPadrao, CcpJsonFieldError.requiredFieldIsMissing);
	}

	/**
	 * Turning the requirement off also applies to the explanation of the rules: it makes no sense to document as
	 * required a field that the framework fills by itself.
	 */
	@Test
	public void requiredRuleDisappearsFromTheExplanationTest() {
		List<String> rules = this.rulesOfTheField(RulesDefaultValueWithRequired.class, RulesDefaultValueWithRequired.obrigatorioComValorPadrao);
		String nameOfTheRule = CcpJsonFieldError.requiredFieldIsMissing.getValue();
		boolean present = rules.contains(nameOfTheRule);
		assertFalse("The required rule should not appear, but the rules were: " + rules, present);
	}

	@Test
	public void requiredRuleStaysWithoutDefaultValueTest() {
		List<String> rules = this.rulesOfTheField(RulesRequiredWithoutDefaultValue.class, RulesRequiredWithoutDefaultValue.obrigatorioSemValorPadrao);
		String nameOfTheRule = CcpJsonFieldError.requiredFieldIsMissing.getValue();
		boolean present = rules.contains(nameOfTheRule);
		assertTrue("The required rule should appear, but the rules were: " + rules, present);
	}

	private List<String> rulesOfTheField(Class<?> rulesClass, CcpJsonFieldName field) {

		CcpJsonRepresentation explanation = CcpJsonValidationRulesEngine.INSTANCE.getRulesExplanation(rulesClass);
		List<CcpJsonRepresentation> rulesOfTheField = explanation.getAsJsonList(field);
		List<String> nomes = new ArrayList<>();

		for (CcpJsonRepresentation rule : rulesOfTheField) {
			String ruleName = rule.getAsString(RuleFields.ruleName);
			nomes.add(ruleName);
		}

		return nomes;
	}

	// ── default value inherited through CcpJsonCopyFieldValidationsFrom ────────

	/**
	 * The real shape of {@code fileName}: {@code required} stays on the local field and the default value on the
	 * source class. If the annotation were looked up only on the local field, the requirement would not be turned
	 * off and no default value would be applied.
	 */
	@Test
	public void defaultValueInheritedFromTheSourceClassTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesCopiedDefaultValue.origem, "curriculo");
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesCopiedDefaultValue.class, json);
		String value = result.getAsString(RulesCopiedDefaultValue.copiado);
		assertEquals("derivado de curriculo", value);
	}

	@Test
	public void inheritedFieldAlreadyGivenIsNotOverwrittenTest() {
		CcpJsonRepresentation withSource = CcpOtherConstants.EMPTY_JSON.put(RulesCopiedDefaultValue.origem, "curriculo");
		CcpJsonRepresentation json = withSource.put(RulesCopiedDefaultValue.copiado, "valor que veio de fora");
		CcpJsonRepresentation result = FieldDefaultValue.applyDefaultValues(RulesCopiedDefaultValue.class, json);
		String value = result.getAsString(RulesCopiedDefaultValue.copiado);
		assertEquals("valor que veio de fora", value);
	}
}
