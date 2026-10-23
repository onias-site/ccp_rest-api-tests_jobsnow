package com.vis.json.fields.validation;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;

/**
 * Proves the rules of the vis field enums that no other test reached: the fields of a hash combination of a resume, a
 * skill grouped by its two first initials and the statistics of a position.
 */
public class VisJsonFieldRulesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private void accepts(Class<?> rules, CcpJsonRepresentation json) {
		try {
			CcpJsonValidatorEngine.INSTANCE.validateJson(rules, json, "vis field rules");
		} catch (CcpJsonValidationError e) {
			fail("refused: " + e.getMessage());
		}
	}

	private void refuses(Class<?> rules, CcpJsonRepresentation json) {
		try {
			CcpJsonValidatorEngine.INSTANCE.validateJson(rules, json, "vis field rules");
		} catch (CcpJsonValidationError e) {
			return;
		}
		fail("accepted: " + json);
	}

	private CcpJsonRepresentation hashCombination() {
		CcpJsonRepresentation hashCombination = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonHashCombinationFields.pcd, 1)
				.put(VisJsonHashCombinationFields.travel, 2)
				.put(VisJsonHashCombinationFields.negotiableClaim, 1)
				.put(VisJsonHashCombinationFields.seniority, "pleno")
				.put(VisJsonHashCombinationFields.languageLevel, 0);
		return hashCombination;
	}

	@Test
	public void aHashCombinationTakesTheFlagsAsOneOrTwoAndASeniorityOfTheList() {
		this.accepts(VisJsonHashCombinationFields.class, this.hashCombination());
	}

	@Test
	public void aHashCombinationRefusesAFlagOutOfOneAndTwo() {
		this.refuses(VisJsonHashCombinationFields.class, this.hashCombination().put(VisJsonHashCombinationFields.pcd, 3));
		this.refuses(VisJsonHashCombinationFields.class, this.hashCombination().put(VisJsonHashCombinationFields.travel, 0));
	}

	@Test
	public void aHashCombinationRefusesASeniorityOutOfTheListAndALanguageLevelAboveTwo() {
		this.refuses(VisJsonHashCombinationFields.class, this.hashCombination().put(VisJsonHashCombinationFields.seniority, "trainee"));
		this.refuses(VisJsonHashCombinationFields.class, this.hashCombination().put(VisJsonHashCombinationFields.languageLevel, 3));
	}

	@Test
	public void aGroupedSkillNeedsTheSkillAndTheWord() {
		CcpJsonRepresentation groupedSkill = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.skill, "JAVA")
				.put(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.word, "JAVA");
		this.accepts(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.class, groupedSkill);
		this.refuses(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.class, groupedSkill.removeFields(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.skill));
		this.refuses(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.class, groupedSkill.removeFields(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.word));
	}

	@Test
	public void aGroupedSkillRefusesASkillOfOneCharacter() {
		CcpJsonRepresentation groupedSkill = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.skill, "J")
				.put(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.word, "J");
		this.refuses(VisJsonFieldsSkillsGroupedByTheirTwoFirstInitials.class, groupedSkill);
	}

	@Test
	public void thePositionStatisticsNeedTheRequiredFieldsAndAtLeastOneSalary() {
		this.refuses(VisJsonFieldsPositionStatis.class, CcpOtherConstants.EMPTY_JSON);
		CcpJsonRepresentation withoutSalary = CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("disponibility"), 5)
				.put(new CcpFieldName("ddd"), Arrays.asList(11))
				.put(new CcpFieldName("experience"), 2015);
		this.refuses(VisJsonFieldsPositionStatis.class, withoutSalary);
	}
}
