package com.vis.business.skill;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.business.telegram.VisSkillWordsInMemory;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * The rules of {@link VisBusinessPutTheBroadSkills}, the terms too broad of a text, over a fixed catalog (no index read):
 * <ul>
 * <li>a skill of the text with skills below it, none of them in the text, is broad and comes with every skill below it,
 * direct or not, in alphabetical order;</li>
 * <li>a skill below it in the text (found or excluded) specifies the term, and it is not broad;</li>
 * <li>a skill with nothing below it is never broad;</li>
 * <li>a text with no skill has no broad skill.</li>
 * </ul>
 */
public class OnPointingOutTheBroadSkills {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	enum Fields implements CcpJsonFieldName {
		excludedSkill, broadSkill, children
	}

	private static CcpJsonRepresentation skillWord(String skill, String... parents) {
		List<String> parentList = Arrays.asList(parents);
		CcpJsonRepresentation skillWord = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonCommonsFields.skill, skill)
				.put(VisJsonCommonsFields.word, skill)
				.put(VisJsonCommonsFields.parent, parentList);
		return skillWord;
	}

	// LARAVEL lists BACKEND besides PHP, as the dictionary does: the parents are not only the direct ones
	private static final VisSkillWordsInMemory WORDS = new VisSkillWordsInMemory(Arrays.asList(
			skillWord("BACKEND"),
			skillWord("PHP", "BACKEND"),
			skillWord("LARAVEL", "PHP", "BACKEND"),
			skillWord("JAVA", "BACKEND"),
			skillWord("SPRING BOOT", "JAVA"),
			skillWord("SCRUM")
			));

	private static List<CcpJsonRepresentation> skills(String... names) {
		List<CcpJsonRepresentation> skills = new ArrayList<>();
		for (String name : names) {
			CcpJsonRepresentation skill = skillWord(name);
			skills.add(skill);
		}
		return skills;
	}

	private static List<CcpJsonRepresentation> broadSkillsOf(List<CcpJsonRepresentation> found, List<CcpJsonRepresentation> excluded) {
		CcpJsonRepresentation answer = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonCommonsFields.skill, found)
				.put(Fields.excludedSkill, excluded);
		VisBusinessPutTheBroadSkills putTheBroadSkills = new VisBusinessPutTheBroadSkills(WORDS);
		CcpJsonRepresentation withTheBroadSkills = putTheBroadSkills.execute(answer);
		List<CcpJsonRepresentation> broadSkills = withTheBroadSkills.getAsJsonList(Fields.broadSkill);
		return broadSkills;
	}

	@Test
	public void aTermWithNoSkillBelowItInTheTextComesWithEverySkillBelowIt() {
		List<CcpJsonRepresentation> found = skills("BACKEND", "SCRUM");
		List<CcpJsonRepresentation> excluded = skills();
		List<CcpJsonRepresentation> broadSkills = broadSkillsOf(found, excluded);

		assertEquals(1, broadSkills.size());
		CcpJsonRepresentation backend = broadSkills.get(0);
		String skill = backend.getAsString(VisJsonCommonsFields.skill);
		List<String> children = backend.getAsStringList(Fields.children);
		assertEquals("BACKEND", skill);
		List<String> expectedChildren = Arrays.asList("JAVA", "LARAVEL", "PHP", "SPRING BOOT");
		assertEquals(expectedChildren, children);
	}

	@Test
	public void anIndirectSkillBelowItInTheTextSpecifiesTheTerm() {
		List<CcpJsonRepresentation> found = skills("BACKEND", "SPRING BOOT");
		List<CcpJsonRepresentation> excluded = skills();
		List<CcpJsonRepresentation> broadSkills = broadSkillsOf(found, excluded);

		assertTrue(broadSkills.isEmpty());
	}

	@Test
	public void theExcludedSkillsCountAsSkillsOfTheText() {
		// the required skills of the position go to skills/fromText as excluded
		List<CcpJsonRepresentation> found = skills("PHP");
		List<CcpJsonRepresentation> excluded = skills("BACKEND");
		List<CcpJsonRepresentation> broadSkills = broadSkillsOf(found, excluded);

		assertEquals(1, broadSkills.size());
		CcpJsonRepresentation php = broadSkills.get(0);
		String skill = php.getAsString(VisJsonCommonsFields.skill);
		List<String> children = php.getAsStringList(Fields.children);
		assertEquals("PHP", skill);
		List<String> expectedChildren = Arrays.asList("LARAVEL");
		assertEquals(expectedChildren, children);
	}

	@Test
	public void aBroadTermBelowAnotherOneIsBroadToo() {
		List<CcpJsonRepresentation> found = skills("JAVA");
		List<CcpJsonRepresentation> excluded = skills("BACKEND");
		List<CcpJsonRepresentation> broadSkills = broadSkillsOf(found, excluded);

		assertEquals(1, broadSkills.size());
		CcpJsonRepresentation java = broadSkills.get(0);
		String skill = java.getAsString(VisJsonCommonsFields.skill);
		assertEquals("JAVA", skill);
	}

	@Test
	public void aTextWithNoSkillHasNoBroadSkill() {
		List<CcpJsonRepresentation> broadSkills = broadSkillsOf(skills(), skills());

		assertTrue(broadSkills.isEmpty());
	}
}
