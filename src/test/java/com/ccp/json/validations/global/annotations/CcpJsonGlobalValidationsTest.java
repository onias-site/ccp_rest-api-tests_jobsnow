package com.ccp.json.validations.global.annotations;

import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Verifies the three attributes of {@code @CcpJsonGlobalValidations}, each one with the behavior its own name
 * announces: <ul> <li>{@code requiresAtLeastOne}: of each group, at least one field must be in the JSON;</li>
 * <li>{@code requiresAllOrNone}: of each group, either all fields are in the JSON, or none;</li>
 * <li>{@code customJsonValidators}: additional class validators run together with the two above.</li> </ul> The
 * scenarios mirror the real uses in {@code VisEntityPosition.Fields} (the first two attributes combined) and in
 * {@code VisEntityResume.Fields} (a single group of {@code requiresAtLeastOne}).
 */
public class CcpJsonGlobalValidationsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ------------------------------------------------------------------
	// requiresAtLeastOne
	// ------------------------------------------------------------------

	@Test
	public void requiresAtLeastOneWithOneOfTheFieldsOfTheGroupTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneOneGroup.maxClt, 9_000);
		GlobalValidation.accepts(RulesRequiresAtLeastOneOneGroup.class, json);
	}

	@Test
	public void requiresAtLeastOneWithTheOtherFieldOfTheGroupTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneOneGroup.maxPj, 12_000);
		GlobalValidation.accepts(RulesRequiresAtLeastOneOneGroup.class, json);
	}

	@Test
	public void requiresAtLeastOneWithAllFieldsOfTheGroupTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneOneGroup.maxClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAtLeastOneOneGroup.maxPj, 12_000);
		GlobalValidation.accepts(RulesRequiresAtLeastOneOneGroup.class, json);
	}

	@Test
	public void requiresAtLeastOneWithoutAnyFieldOfTheGroupTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneOneGroup.titulo, "Desenvolvedor");
		List<String> messages = GlobalValidation.refuses(RulesRequiresAtLeastOneOneGroup.class, json);
		GlobalValidation.containsMessage(messages, "maxClt");
		GlobalValidation.containsMessage(messages, "maxPj");
	}

	@Test
	public void requiresAtLeastOneWithEmptyJsonTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		GlobalValidation.refuses(RulesRequiresAtLeastOneOneGroup.class, json);
	}

	@Test
	public void requiresAtLeastOneWithTwoGroupsBothSatisfiedTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneTwoGroups.maxClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAtLeastOneTwoGroups.minClt, 5_000);
		GlobalValidation.accepts(RulesRequiresAtLeastOneTwoGroups.class, json);
	}

	@Test
	public void requiresAtLeastOneWithTwoGroupsSatisfyingOnlyTheFirstTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneTwoGroups.maxClt, 9_000);
		List<String> messages = this.refusesTwoGroups(json);
		GlobalValidation.containsMessage(messages, "minClt");
	}

	private List<String> refusesTwoGroups(CcpJsonRepresentation json) {
		List<String> messages = GlobalValidation.refuses(RulesRequiresAtLeastOneTwoGroups.class, json);
		return messages;
	}

	@Test
	public void requiresAtLeastOneWithTwoGroupsSatisfyingOnlyTheSecondTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneTwoGroups.minPj, 5_000);
		List<String> messages = this.refusesTwoGroups(json);
		GlobalValidation.containsMessage(messages, "maxClt");
	}

	@Test
	public void requiresAtLeastOneWithGroupBuiltByTwoClassesTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneUnionOfClasses.sms, "11999999999");
		GlobalValidation.accepts(RulesRequiresAtLeastOneUnionOfClasses.class, json);
	}

	@Test
	public void requiresAtLeastOneWithGroupBuiltByTwoClassesWithoutAnyFieldTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneUnionOfClasses.titulo, "Desenvolvedor");
		List<String> messages = GlobalValidation.refuses(RulesRequiresAtLeastOneUnionOfClasses.class, json);
		GlobalValidation.containsMessage(messages, "telegram");
		GlobalValidation.containsMessage(messages, "sms");
	}

	// ------------------------------------------------------------------
	// requiresAllOrNone
	// ------------------------------------------------------------------

	@Test
	public void requiresAllOrNoneWithoutAnyFieldOfTheGroupTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.titulo, "Desenvolvedor");
		GlobalValidation.accepts(RulesRequiresAllOrNoneOneGroup.class, json);
	}

	@Test
	public void requiresAllOrNoneWithAllFieldsOfTheGroupTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAllOrNoneOneGroup.maxClt, 9_000);
		GlobalValidation.accepts(RulesRequiresAllOrNoneOneGroup.class, json);
	}

	@Test
	public void requiresAllOrNoneWithOnlyTheFirstFieldOfTheGroupTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.minClt, 5_000);
		List<String> messages = GlobalValidation.refuses(RulesRequiresAllOrNoneOneGroup.class, json);
		GlobalValidation.containsMessage(messages, "maxClt");
	}

	@Test
	public void requiresAllOrNoneWithOnlyTheSecondFieldOfTheGroupTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.maxClt, 9_000);
		List<String> messages = GlobalValidation.refuses(RulesRequiresAllOrNoneOneGroup.class, json);
		GlobalValidation.containsMessage(messages, "minClt");
	}

	@Test
	public void requiresAllOrNoneWithTwoGroupsOneCompleteAndTheOtherUntouchedTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneTwoGroups.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAllOrNoneTwoGroups.maxClt, 9_000);
		GlobalValidation.accepts(RulesRequiresAllOrNoneTwoGroups.class, json);
	}

	@Test
	public void requiresAllOrNoneWithTwoGroupsBothCompleteTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneTwoGroups.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RulesRequiresAllOrNoneTwoGroups.maxClt, 9_000);
		CcpJsonRepresentation put3 = put2
				.put(RulesRequiresAllOrNoneTwoGroups.minPj, 7_000);
		CcpJsonRepresentation json = put3
				.put(RulesRequiresAllOrNoneTwoGroups.maxPj, 12_000);
		GlobalValidation.accepts(RulesRequiresAllOrNoneTwoGroups.class, json);
	}

	@Test
	public void requiresAllOrNoneWithTwoGroupsOneCompleteAndTheOtherHalfFilledTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneTwoGroups.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RulesRequiresAllOrNoneTwoGroups.maxClt, 9_000);
		CcpJsonRepresentation json = put2
				.put(RulesRequiresAllOrNoneTwoGroups.minPj, 7_000);
		List<String> messages = GlobalValidation.refuses(RulesRequiresAllOrNoneTwoGroups.class, json);
		GlobalValidation.containsMessage(messages, "maxPj");
	}

	@Test
	public void requiresAllOrNoneWithTwoGroupsBothHalfFilledTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneTwoGroups.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAllOrNoneTwoGroups.minPj, 7_000);
		List<String> messages = GlobalValidation.refuses(RulesRequiresAllOrNoneTwoGroups.class, json);
		GlobalValidation.containsMessage(messages, "maxClt");
		GlobalValidation.containsMessage(messages, "maxPj");
	}

	@Test
	public void requiresAllOrNonePointsThePresentAndTheMissingFieldsTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.minClt, 5_000);
		List<String> messages = GlobalValidation.refuses(RulesRequiresAllOrNoneOneGroup.class, json);
		GlobalValidation.containsMessage(messages, "contains the following fields");
		GlobalValidation.containsMessage(messages, "not contains the following fields");
	}

	// ------------------------------------------------------------------
	// customJsonValidators
	// ------------------------------------------------------------------

	@Test
	public void customJsonValidatorsWithoutErrorTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesCustomJsonValidator.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesCustomJsonValidator.maxClt, 9_000);
		GlobalValidation.accepts(RulesCustomJsonValidator.class, json);
	}

	@Test
	public void customJsonValidatorsWithErrorTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesCustomJsonValidator.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesCustomJsonValidator.maxClt, 5_000);
		List<String> messages = GlobalValidation.refuses(RulesCustomJsonValidator.class, json);
		GlobalValidation.containsMessage(messages, ConsistentSalaryRangeValidator.MESSAGE);
	}

	@Test
	public void customJsonValidatorsIsNotCalledWhenItCannotEvaluateTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesCustomJsonValidator.minClt, 9_000);
		GlobalValidation.accepts(RulesCustomJsonValidator.class, json);
	}

	@Test
	public void customJsonValidatorsInSeriesAccumulateTheErrorsTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesChainedCustomJsonValidators.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesChainedCustomJsonValidators.maxClt, 5_000);
		List<String> messages = GlobalValidation.refuses(RulesChainedCustomJsonValidators.class, json);
		GlobalValidation.containsMessage(messages, ConsistentSalaryRangeValidator.MESSAGE);
		GlobalValidation.containsMessage(messages, RequiredTitleValidator.MESSAGE);
	}

	@Test
	public void customJsonValidatorsInSeriesWithOnlyOneReportingErrorTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesChainedCustomJsonValidators.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RulesChainedCustomJsonValidators.maxClt, 9_000);
		CcpJsonRepresentation json = put2
				.put(RulesChainedCustomJsonValidators.titulo, "Desenvolvedor");
		GlobalValidation.accepts(RulesChainedCustomJsonValidators.class, json);
	}

	@Test
	public void customJsonValidatorsCriticalInterruptsTheFollowingTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		List<String> messages = GlobalValidation.refuses(RulesCriticalCustomJsonValidator.class, json);
		GlobalValidation.containsMessage(messages, CriticalContractValidator.MESSAGE);
		GlobalValidation.doesNotContainMessage(messages, RequiredTitleValidator.MESSAGE);
	}

	@Test
	public void customJsonValidatorsCriticalWithoutErrorLetsTheFollowingRunTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesCriticalCustomJsonValidator.contrato, "clt");
		List<String> messages = GlobalValidation.refuses(RulesCriticalCustomJsonValidator.class, json);
		GlobalValidation.containsMessage(messages, RequiredTitleValidator.MESSAGE);
	}

	// ------------------------------------------------------------------
	// combined attributes and edge cases of the annotation
	// ------------------------------------------------------------------

	@Test
	public void combinedAttributesWithValidJsonTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesCombinedGlobalValidations.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesCombinedGlobalValidations.maxClt, 9_000);
		GlobalValidation.accepts(RulesCombinedGlobalValidations.class, json);
	}

	@Test
	public void combinedAttributesReportTheErrorsOfAllAttributesTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesCombinedGlobalValidations.minPj, 7_000);
		List<String> messages = GlobalValidation.refuses(RulesCombinedGlobalValidations.class, json);
		GlobalValidation.containsMessage(messages, "It is missing one of them fields");
		GlobalValidation.containsMessage(messages, "maxPj");
	}

	@Test
	public void combinedAttributesReportTheCustomValidatorErrorTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesCombinedGlobalValidations.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesCombinedGlobalValidations.maxClt, 5_000);
		List<String> messages = GlobalValidation.refuses(RulesCombinedGlobalValidations.class, json);
		GlobalValidation.containsMessage(messages, ConsistentSalaryRangeValidator.MESSAGE);
	}

	@Test
	public void annotationWithoutAttributesRequiresNothingTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesGlobalValidationsWithoutAttributes.minClt, 5_000);
		GlobalValidation.accepts(RulesGlobalValidationsWithoutAttributes.class, json);
	}

	@Test
	public void annotationWithoutAttributesAcceptsEvenEmptyJsonTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		GlobalValidation.accepts(RulesGlobalValidationsWithoutAttributes.class, json);
	}

	// ------------------------------------------------------------------
	// explanation of the rules of each attribute
	// ------------------------------------------------------------------

	@Test
	public void requiresAtLeastOneExplainsItsRuleTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesRequiresAtLeastOneOneGroup.class);
		GlobalValidation.containsMessage(explicacoes, "one of this following fields");
		GlobalValidation.containsMessage(explicacoes, "maxClt");
		GlobalValidation.containsMessage(explicacoes, "maxPj");
	}

	/**
	 * The copied global rules are explained under the name of the class that copies them. Until 2026-10-06 the
	 * explanation did not follow {@code @CcpJsonCopyGlobalValidationsFrom}, although the validation did.
	 */
	@Test
	public void copiedGlobalValidationsAreExplainedTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesCopyingGlobalValidations.class);
		GlobalValidation.containsMessage(explicacoes, "one of this following fields");
		GlobalValidation.containsMessage(explicacoes, "maxClt");
		GlobalValidation.containsMessage(explicacoes, "maxPj");
	}

	@Test
	public void requiresAllOrNoneExplainsItsRuleTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesRequiresAllOrNoneOneGroup.class);
		GlobalValidation.containsMessage(explicacoes, "all (or none)");
		GlobalValidation.containsMessage(explicacoes, "minClt");
		GlobalValidation.containsMessage(explicacoes, "maxClt");
	}

	@Test
	public void requiresAllOrNoneDoesNotExplainTheGroupsOfRequiresAtLeastOneTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesRequiresAtLeastOneOneGroup.class);
		GlobalValidation.doesNotContainMessage(explicacoes, "all (or none)");
	}

	@Test
	public void customJsonValidatorsExplainTheirRulesTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesChainedCustomJsonValidators.class);
		GlobalValidation.containsMessage(explicacoes, ConsistentSalaryRangeValidator.MESSAGE);
		GlobalValidation.containsMessage(explicacoes, RequiredTitleValidator.MESSAGE);
	}

	@Test
	public void classWithoutAnnotationRequiresNothingTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesWithoutGlobalValidations.minClt, 5_000);
		GlobalValidation.accepts(RulesWithoutGlobalValidations.class, json);
	}
}
