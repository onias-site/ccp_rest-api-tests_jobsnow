package com.vis.json.fields.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;
import com.vis.entities.VisEntityResume;

/**
 * Proves that the type of a skill must go with its category: each category accepts only its own types, and a resume
 * carrying a skill with a wrong pair is refused.
 */
public class VisJsonValidatorSkillTypeMatchesCategoryTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final VisJsonValidatorSkillTypeMatchesCategory validator = new VisJsonValidatorSkillTypeMatchesCategory();

	private CcpJsonRepresentation skill(String category, String type) {
		return new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"category\":\"" + category + "\",\"type\":\"" + type + "\"}");
	}

	@Test
	public void everyCategoryAcceptsItsOwnTypes() {
		assertFalse(this.validator.hasError(this.skill("skillsFromText", "skillsFromResume"), VisJsonFieldsSkills.class));
		assertFalse(this.validator.hasError(this.skill("excludedSkills", "excludedSkills"), VisJsonFieldsSkills.class));
		assertFalse(this.validator.hasError(this.skill("parent", "parent"), VisJsonFieldsSkills.class));
		assertFalse(this.validator.hasError(this.skill("discardedSkills", "isPieceOfOtherWord"), VisJsonFieldsSkills.class));
		assertFalse(this.validator.hasError(this.skill("discardedSkills", "isPieceOfOtherSkill"), VisJsonFieldsSkills.class));
		assertFalse(this.validator.hasError(this.skill("discardedSkills", "skillAlreadyAdded"), VisJsonFieldsSkills.class));
	}

	@Test
	public void aCategoryRefusesTheTypesOfTheOthers() {
		assertTrue(this.validator.hasError(this.skill("skillsFromText", "skillAlreadyAdded"), VisJsonFieldsSkills.class));
		assertTrue(this.validator.hasError(this.skill("discardedSkills", "skillsFromResume"), VisJsonFieldsSkills.class));
		assertTrue(this.validator.hasError(this.skill("parent", "excludedSkills"), VisJsonFieldsSkills.class));
	}

	@Test
	public void unknownOrMissingValuesAreLeftToTheFieldRules() {
		assertFalse(this.validator.hasError(this.skill("unknown", "parent"), VisJsonFieldsSkills.class));
		assertFalse(this.validator.hasError(new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"category\":\"parent\"}"), VisJsonFieldsSkills.class));
	}

	@Test
	public void aResumeWithAWrongPairIsRefused() {
		CcpJsonRepresentation resume = new CcpJsonRepresentation("{}")
				.put(VisEntityResume.Fields.email, "pair@jobsnow.com")
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/onias")
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000);

		CcpJsonRepresentation rightPair = resume.put(VisEntityResume.Fields.skill, Arrays.asList(this.skill("skillsFromText", "skillsFromResume")));
		CcpJsonValidatorEngine.INSTANCE.validateJson(VisEntityResume.Fields.class, rightPair, "pair");

		CcpJsonRepresentation wrongPair = resume.put(VisEntityResume.Fields.skill, Arrays.asList(this.skill("skillsFromText", "skillAlreadyAdded")));
		try {
			CcpJsonValidatorEngine.INSTANCE.validateJson(VisEntityResume.Fields.class, wrongPair, "pair");
			fail("the wrong pair was accepted");
		} catch (CcpJsonValidationError e) {
			assertTrue(e.getMessage(), e.getMessage().contains("skillAlreadyAdded"));
		}
	}
}
