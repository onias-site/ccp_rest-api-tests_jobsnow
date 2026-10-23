package com.vis.skills;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.skills.SkillManager.JsonFields;

/**
 * The pieces of the skill report of {@link SkillManager}: {@link SkillWithItsParents} gives a skill the parents of
 * the line that starts with it, and {@link SkillReportSorter} orders field by field, numbers and booleans from the
 * greatest, texts alphabetically.
 */
public class SkillManagerHelpersTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static CcpJsonRepresentation skill(String name) {
		return CcpOtherConstants.EMPTY_JSON.put(JsonFields.skill, name);
	}

	@Test
	public void theSkillGetsTheParentsOfTheLineThatStartsWithIt() {
		List<List<String>> skillLines = List.of(List.of("SPRING", "JAVA", "FRAMEWORK"), List.of("JAVA", "LINGUAGEM"));

		CcpJsonRepresentation spring = new SkillWithItsParents(skillLines).apply(skill("SPRING"));

		assertEquals(List.of("JAVA", "FRAMEWORK"), spring.getAsStringList(JsonFields.parent));
	}

	@Test
	public void aSkillThatStartsNoLineIsKeptAsItIs() {
		List<List<String>> skillLines = List.of(List.of("SPRING", "JAVA"));
		CcpJsonRepresentation java = skill("JAVA");

		CcpJsonRepresentation result = new SkillWithItsParents(skillLines).apply(java);

		assertSame(java, result);
		assertFalse(result.containsField(JsonFields.parent));
	}

	private static CcpJsonRepresentation row(Object count, Object active, String name) {
		return CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("count"), count)
				.put(new CcpFieldName("active"), active)
				.put(new CcpFieldName("name"), name);
	}

	@Test
	public void theRowsAreOrderedByTheGreatestNumberThenTrueFirstThenAlphabetically() {
		CcpJsonRepresentation a = row(5, false, "B");
		CcpJsonRepresentation b = row(9, false, "Z");
		CcpJsonRepresentation c = row(5, true, "Y");
		CcpJsonRepresentation d = row(5, false, "A");
		List<CcpJsonRepresentation> rows = new ArrayList<>(List.of(a, b, c, d));

		rows.sort(new SkillReportSorter("count", "active", "name"));

		assertEquals(List.of(b, c, d, a), rows);
	}

	@Test
	public void equalRowsCompareAsZero() {
		SkillReportSorter sorter = new SkillReportSorter("count", "active", "name");

		assertEquals(0, sorter.compare(row(1, true, "A"), row(1, true, "A")));
		assertTrue(sorter.compare(row(1, true, "A"), row(2, true, "A")) > 0);
	}
}
