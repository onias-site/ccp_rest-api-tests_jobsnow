package com.vis.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vis.business.statistics.VisBusinessMonthsOfTheSkill;
import com.vis.business.statistics.VisBusinessSkillsOfTheMonth;

/** Each service names the validation class of its input: with none, the service looks for a class named after itself. */
public class VisServiceSkillStatisticsTest {

	@Test
	public void theSkillsOfTheMonthAreValidatedByTheirBusiness() {
		assertEquals(VisBusinessSkillsOfTheMonth.Fields.class, VisServiceSkillStatistics.GetSkillsOfTheMonth.getJsonValidationClass());
	}

	@Test
	public void theMonthsOfTheSkillAreValidatedByTheirBusiness() {
		assertEquals(VisBusinessMonthsOfTheSkill.Fields.class, VisServiceSkillStatistics.GetMonthsOfTheSkill.getJsonValidationClass());
	}
}
