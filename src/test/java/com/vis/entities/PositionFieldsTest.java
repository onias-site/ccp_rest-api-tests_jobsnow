package com.vis.entities;

import com.vis.json.fields.validation.VisJsonFieldsNewExpirationRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;
import com.vis.business.position.VisBusinessPutPositionDefaultValues;
import com.vis.json.transformers.VisPositionHashCombinations;
import com.vis.json.transformers.VisResumeHashCombinations;

/**
 * The fields of the position (2026-11-21): the validations of the salary ranges, the expiration, the contacts, the
 * skills and the sorting, and the hashes of the position ({@code VisPositionHashCombinations}), which share the logic
 * of the resume on the side of the record that searches.
 */
public class PositionFieldsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final long ONE_DAY = 24L * 60L * 60L * 1000L;

	private String daysAhead(int days) {
		long instant = System.currentTimeMillis() + days * ONE_DAY;
		String instantAsText = String.valueOf(instant);
		return instantAsText;
	}

	private CcpJsonRepresentation range(Object min, Object max) {
		CcpJsonRepresentation range = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("min"), min).put(new CcpFieldName("max"), max);
		return range;
	}

	private CcpJsonRepresentation contact(String type, String value) {
		CcpJsonRepresentation contact = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("type"), type).put(new CcpFieldName("value"), value);
		return contact;
	}

	private CcpJsonRepresentation position() {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JVM\"]}");
		CcpJsonRepresentation docker = new CcpJsonRepresentation("{\"skill\":\"DOCKER\",\"word\":\"DOCKER\",\"type\":\"shouldSkills\",\"category\":\"shouldSkills\"}");
		CcpJsonRepresentation english = new CcpJsonRepresentation("{\"name\":\"Ingles\",\"level\":2}");
		CcpJsonRepresentation sort = new CcpJsonRepresentation("{\"type\":\"desc\",\"field\":[\"clt\",\"shouldSkillsTotal\"]}");
		StringBuilder description = new StringBuilder();
		for (int k = 0; k < 10; k++) {
			description.append("description ");
		}
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.recruiter, "onias@jobsnow.com")
				.put(VisEntityPosition.Fields.clt, this.range(2_000, 9_000))
				.put(VisEntityPosition.Fields.disponibility, 5)
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11))
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.seniority, "senior")
				.put(VisEntityPosition.Fields.skill, Arrays.asList(java, docker))
				.put(VisEntityPosition.Fields.spokenLanguage, Arrays.asList(english))
				.put(VisEntityPosition.Fields.pcd, true)
				.put(VisEntityPosition.Fields.temporallyJobTime, 0)
				.put(VisEntityPosition.Fields.title, "Desenvolvedor Java")
				.put(VisEntityPosition.Fields.expiration, this.range(this.daysAhead(1), this.daysAhead(30)))
				.put(VisEntityPosition.Fields.description, description.toString())
				.put(VisEntityPosition.Fields.sort, Arrays.asList(sort))
				.put(VisEntityPosition.Fields.contact, Arrays.asList(this.contact("email", "rh@jobsnow.com"), this.contact("whatsapp", "+5511999998888"), this.contact("link", "https://jobsnow.com/vaga")));
		return position;
	}

	private void validate(CcpJsonRepresentation position) {
		CcpJsonValidatorEngine.INSTANCE.validateJson(VisEntityPosition.Fields.class, position, "position");
	}

	private void refused(CcpJsonRepresentation position, String reason) {
		try {
			this.validate(position);
			fail(reason + " was accepted");
		} catch (CcpJsonValidationError e) {
		}
	}

	@Test
	public void aCompletePositionIsAccepted() {
		this.validate(this.position());
	}

	@Test
	public void oneOfTheSalaryRangesIsEnough() {
		CcpJsonRepresentation onlyPj = this.position().removeFields(VisEntityPosition.Fields.clt).put(VisEntityPosition.Fields.pj, this.range(3_000, 4_000));
		this.validate(onlyPj);
		this.refused(this.position().removeFields(VisEntityPosition.Fields.clt), "a position with no salary range");
	}

	@Test
	public void theSalaryRangeHasItsLimitsAndItsOrder() {
		this.refused(this.position().put(VisEntityPosition.Fields.clt, this.range(999, 9_000)), "a min below 1,000");
		this.refused(this.position().put(VisEntityPosition.Fields.clt, this.range(2_000, 100_001)), "a max above 100,000");
		this.refused(this.position().put(VisEntityPosition.Fields.clt, this.range(9_000, 2_000)), "a max below the min");
		this.refused(this.position().put(VisEntityPosition.Fields.clt, CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("min"), 2_000)), "a range with no max");
	}

	/**
	 * Since 2026-11-21 the stored position keeps only the order of the expiration (the inactivation puts the max one minute
	 * before now, an old position keeps its min); a new position starts and ends from the current date on.
	 */
	@Test
	public void theExpirationOfANewPositionStartsTodayAndTheStoredOneKeepsTheOrder() {
		this.validate(this.position().put(VisEntityPosition.Fields.expiration, this.range(this.daysAhead(-3), this.daysAhead(-1))));
		this.refused(this.position().put(VisEntityPosition.Fields.expiration, this.range(this.daysAhead(30), this.daysAhead(5))), "a max before the min");
		CcpJsonValidatorEngine.INSTANCE.validateJson(VisJsonFieldsNewExpirationRange.class, this.range(this.daysAhead(1), this.daysAhead(30)), "expiration");
		try {
			CcpJsonValidatorEngine.INSTANCE.validateJson(VisJsonFieldsNewExpirationRange.class, this.range(this.daysAhead(-3), this.daysAhead(30)), "expiration");
			fail("a new position with a min in the past was accepted");
		} catch (CcpJsonValidationError e) {
		}
	}

	@Test
	public void eachContactIsValidatedByItsType() {
		this.refused(this.position().put(VisEntityPosition.Fields.contact, Arrays.asList(this.contact("email", "not an e-mail"))), "an invalid e-mail");
		this.refused(this.position().put(VisEntityPosition.Fields.contact, Arrays.asList(this.contact("whatsapp", "12345"))), "a short whatsapp");
		this.refused(this.position().put(VisEntityPosition.Fields.contact, Arrays.asList(this.contact("link", "jobsnow.com"))), "a link with no scheme");
		this.refused(this.position().put(VisEntityPosition.Fields.contact, Arrays.asList(this.contact("telegram", "@onias"))), "an unknown type");
	}

	@Test
	public void thePositionHasNoExcludedSkills() {
		CcpJsonRepresentation excluded = new CcpJsonRepresentation("{\"skill\":\"PHP\",\"word\":\"PHP\",\"type\":\"excludedSkills\",\"category\":\"excludedSkills\"}");
		this.refused(this.position().put(VisEntityPosition.Fields.skill, Arrays.asList(excluded)), "an excluded skill");
		CcpJsonRepresentation mismatch = new CcpJsonRepresentation("{\"skill\":\"PHP\",\"word\":\"PHP\",\"type\":\"shouldSkills\",\"category\":\"skillsFromText\"}");
		this.refused(this.position().put(VisEntityPosition.Fields.skill, Arrays.asList(mismatch)), "a type out of its category");
	}

	@Test
	public void theTextsAndTheSortingHaveTheirRules() {
		this.refused(this.position().put(VisEntityPosition.Fields.title, "Dev"), "a short title");
		this.refused(this.position().put(VisEntityPosition.Fields.description, "short"), "a short description");
		CcpJsonRepresentation badSort = new CcpJsonRepresentation("{\"type\":\"up\",\"field\":[\"clt\"]}");
		this.refused(this.position().put(VisEntityPosition.Fields.sort, Arrays.asList(badSort)), "a sort type out of the enum");
		this.refused(this.position().put(VisEntityPosition.Fields.frequencyType, "monthly"), "a frequency out of the enum");
		this.refused(this.position().put(VisEntityPosition.Fields.seniority, "SR"), "a seniority out of VisSeniorityType");
	}

	@Test
	public void theResumeTypeIsRequiredAndTheTitleIsPartOfTheKey() {
		this.refused(this.position().removeFields(VisEntityPosition.Fields.resumeType), "a position with no resumeType");
		this.refused(this.position().put(VisEntityPosition.Fields.resumeType, 5), "a resumeType out of 1 to 4");
		this.refused(this.position().removeFields(VisEntityPosition.Fields.title), "a position with no title");
		CcpJsonRepresentation otherTitle = this.position().put(VisEntityPosition.Fields.title, "Desenvolvedor Python");
		String id = VisEntityPosition.ENTITY.calculateId(this.position());
		String otherId = VisEntityPosition.ENTITY.calculateId(otherTitle);
		assertNotEquals(id, otherId);
	}

	@Test
	public void theBeforeSaveFillsOnlyTheAbsentDefaultValues() {
		CcpJsonRepresentation withDefaults = VisBusinessPutPositionDefaultValues.INSTANCE.execute(this.position());
		assertEquals(true, withDefaults.getAsBoolean(VisEntityPosition.Fields.mustSendInGroups));
		assertEquals(true, withDefaults.getAsBoolean(VisEntityPosition.Fields.mustSendToResumes));
		assertEquals("daily", withDefaults.getAsString(VisEntityPosition.Fields.frequencyType));

		CcpJsonRepresentation informed = this.position()
				.put(VisEntityPosition.Fields.mustSendInGroups, false)
				.put(VisEntityPosition.Fields.frequencyType, "weekly");
		CcpJsonRepresentation keptValues = VisBusinessPutPositionDefaultValues.INSTANCE.execute(informed);
		assertEquals(false, keptValues.getAsBoolean(VisEntityPosition.Fields.mustSendInGroups));
		assertEquals(true, keptValues.getAsBoolean(VisEntityPosition.Fields.mustSendToResumes));
		assertEquals("weekly", keptValues.getAsString(VisEntityPosition.Fields.frequencyType));
	}

	@Test
	public void theHashesOfThePositionSearchOnItsOwnSide() {
		List<CcpJsonRepresentation> combinations = VisPositionHashCombinations.INSTANCE.apply(this.position());

		// area 1; languages: none, Ingles 2, Ingles 1; skills: none, JAVA, JVM (DOCKER is a shouldSkill, out of the hash);
		// pcd true searches {1}, the absent flags (false) search {1, 2}; one seniority
		assertEquals(1 * 3 * 3 * 1 * 2 * 2 * 1, combinations.size());
		Set<String> seniorities = new TreeSet<>();
		Set<String> skills = new TreeSet<>();
		for (CcpJsonRepresentation combination : combinations) {
			CcpJsonValidatorEngine.INSTANCE.validateJson(VisEntityVirtualHashGrouper.Fields.class, combination, "combination");
			seniorities.add(combination.getAsString(VisEntityVirtualHashGrouper.Fields.seniority));
			skills.add(combination.getAsString(VisEntityVirtualHashGrouper.Fields.skillName));
			assertEquals(1, combination.getAsIntegerNumber(VisEntityVirtualHashGrouper.Fields.pcd).intValue());
			assertEquals(1, combination.getAsIntegerNumber(VisEntityVirtualHashGrouper.Fields.resumeType).intValue());
		}
		assertEquals(new TreeSet<>(Arrays.asList("senior")), seniorities);
		assertEquals(new TreeSet<>(Arrays.asList("JAVA", "JVM", "none")), skills);
	}

	@Test
	public void theResumeKeepsItsHashesAfterTheCommonSuperClass() {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JVM\"]}");
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.experience, 12)
				.put(VisEntityResume.Fields.pcd, true)
				.put(VisEntityResume.Fields.skill, Arrays.asList(java));

		List<CcpJsonRepresentation> combinations = VisResumeHashCombinations.INSTANCE.apply(resume);

		// area 1; language none; skills none, JAVA, JVM; pcd true is in {1, 2}, the absent flags in {1}; experience 12 = especialista
		assertEquals(1 * 1 * 3 * 2 * 1 * 1 * 1, combinations.size());
		for (CcpJsonRepresentation combination : combinations) {
			assertEquals("especialista", combination.getAsString(VisEntityVirtualHashGrouper.Fields.seniority));
			assertEquals(1, combination.getAsIntegerNumber(VisEntityVirtualHashGrouper.Fields.resumeType).intValue());
		}
	}
}
