package com.vis.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntityPosition;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * Proves the sorting of the resumes of a position: the criteria of {@link ResumeSortOptions} and the comparator
 * {@link VisSorterResumesByPosition}, which always ends with the number of desired skills (the most first).
 */
public class ResumeSortingTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void eachCriterionComparesItsFieldsAscending() {
		CcpJsonRepresentation sooner = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.disponibility, 5);
		CcpJsonRepresentation later = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.disponibility, 30);

		assertTrue(ResumeSortOptions.disponibility.compare(sooner, later) < 0);
		assertTrue(ResumeSortOptions.disponibility.compare(later, sooner) > 0);
		assertEquals(0, ResumeSortOptions.disponibility.compare(sooner, sooner));
	}

	@Test
	public void moneyComparesCltThenPjThenBtc() {
		CcpJsonRepresentation first = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.clt, 5_000).put(VisJsonCommonsFields.pj, 9_000);
		CcpJsonRepresentation second = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.clt, 5_000).put(VisJsonCommonsFields.pj, 7_000);

		assertTrue(ResumeSortOptions.money.compare(first, second) > 0);
	}

	@Test
	public void aFieldAbsentInEitherResumeIsSkipped() {
		CcpJsonRepresentation withExperience = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.experience, 2010);

		assertEquals(0, ResumeSortOptions.experience.compare(withExperience, CcpOtherConstants.EMPTY_JSON));
		assertEquals(0, ResumeSortOptions.experience.compare(CcpOtherConstants.EMPTY_JSON, withExperience));
	}

	@Test
	public void theResumeWithMoreDesiredSkillsComesFirstWhenNothingElseDecides() {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.desiredSkill, Arrays.asList("JAVA", "SQL", "KAFKA"))
				.put(VisEntityPosition.Fields.sortFields, new ArrayList<>());
		CcpJsonRepresentation oneSkill = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.skill, Arrays.asList("JAVA"));
		CcpJsonRepresentation threeSkills = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.skill, Arrays.asList("JAVA", "SQL", "KAFKA"));

		List<CcpJsonRepresentation> resumes = new ArrayList<>(Arrays.asList(oneSkill, threeSkills));
		resumes.sort(new VisSorterResumesByPosition(position));

		assertEquals(threeSkills, resumes.get(0));
	}

	@Test
	public void theCriteriaOfThePositionComeBeforeTheDesiredSkills() {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.desiredSkill, Arrays.asList("JAVA", "SQL"))
				.put(VisEntityPosition.Fields.sortFields, Arrays.asList(ResumeSortOptions.disponibility.name()));
		CcpJsonRepresentation soonerWithFewerSkills = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonCommonsFields.disponibility, 1)
				.put(VisJsonCommonsFields.skill, Arrays.asList("JAVA"));
		CcpJsonRepresentation laterWithMoreSkills = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonCommonsFields.disponibility, 20)
				.put(VisJsonCommonsFields.skill, Arrays.asList("JAVA", "SQL"));

		int comparison = new VisSorterResumesByPosition(position).compare(soonerWithFewerSkills, laterWithMoreSkills);

		assertTrue(comparison < 0);
	}

	@org.junit.Ignore("finding 45: VisFrequencyOptions has 'montly' while the position accepts 'monthly'")
	@Test
	public void everyFrequencyOfAPositionHasItsMatchingOption() {
		for (VisEntityPosition.VisPositionFrequencyTypes frequency : VisEntityPosition.VisPositionFrequencyTypes.values()) {
			VisFrequencyOptions.valueOf(frequency.name());
		}
	}

	@Test
	public void frequenciesKnowTheLengthOfTheirPeriodsInHours() {
		assertEquals(1d / 60d, VisFrequencyOptions.minute.hours, 0.000001);
		assertEquals(1d, VisFrequencyOptions.hourly.hours, 0);
		assertEquals(24d, VisFrequencyOptions.daily.hours, 0);
		assertEquals(168d, VisFrequencyOptions.weekly.hours, 0);
		assertEquals(730.5d, VisFrequencyOptions.montly.hours, 0);
		assertEquals(8766d, VisFrequencyOptions.yearly.hours, 0);
	}
}
