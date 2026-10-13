package com.vis.json.transformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityVirtualHashGrouper;

/**
 * Proves the ddd of the hashes of a resume: every region ("any state / city") becomes the single neutral ddd, and a
 * partial choice keeps one value per ddd.
 */
public class VisResumeHashCombinationsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final List<Integer> ALL_REGIONS = Arrays.asList(
			61, 62, 64, 65, 66, 67, 82, 71, 73, 74, 75, 77, 85, 88, 98, 99, 83, 81, 87, 86, 89, 84, 79, 68, 96, 92, 97, 91,
			93, 94, 69, 95, 63, 27, 28, 31, 32, 33, 34, 35, 37, 38, 21, 22, 24, 11, 12, 13, 14, 15, 16, 17, 18, 19, 41, 42,
			43, 44, 45, 46, 51, 53, 54, 55, 47, 48, 49);

	private List<Integer> dddsOfTheHashes(List<Integer> ddds) {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.resumeType, 2)
				.put(VisEntityResume.Fields.experience, 3)
				.put(VisEntityResume.Fields.ddd, ddds);
		List<CcpJsonRepresentation> dddValues = new VisResumeHashCombinations().getValuesByField(resume).get(3);
		return dddValues.stream().map(value -> value.getAsIntegerNumber(VisEntityVirtualHashGrouper.Fields.ddd)).collect(Collectors.toList());
	}

	@Test
	public void everyRegionBecomesTheSingleNeutralDdd() {
		assertEquals(67, ALL_REGIONS.size());
		assertEquals(Arrays.asList(VisResumeHashCombinations.ANY_REGION), this.dddsOfTheHashes(ALL_REGIONS));
	}

	@Test
	public void everyRegionPlusHomeOfficeKeepsTheHomeOffice() {
		List<Integer> ddds = new ArrayList<>(ALL_REGIONS);
		ddds.add(10);
		assertEquals(Arrays.asList(10, VisResumeHashCombinations.ANY_REGION), this.dddsOfTheHashes(ddds));
	}

	@Test
	public void everyCombinationObeysTheRulesOfTheGrouper() {
		List<Integer> ddds = new ArrayList<>(ALL_REGIONS);
		ddds.add(10);
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JVM\"]}");
		CcpJsonRepresentation spanish = new CcpJsonRepresentation("{\"name\":\"Espanhol\",\"level\":2}");
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.experience, 12)
				.put(VisEntityResume.Fields.ddd, ddds)
				.put(VisEntityResume.Fields.pcd, true)
				.put(VisEntityResume.Fields.travel, true)
				.put(VisEntityResume.Fields.negotiableClaim, true)
				.put(VisEntityResume.Fields.skill, Arrays.asList(java))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(spanish));

		List<CcpJsonRepresentation> combinations = new VisResumeHashCombinations().apply(resume);

		// languages: none, Espanhol 2, Espanhol 1; skills: none, JAVA, JVM; ddd: 10 and 0; three flags with 2 values each
		assertEquals(3 * 3 * 2 * 2 * 2 * 2, combinations.size());
		for (CcpJsonRepresentation combination : combinations) {
			CcpJsonValidatorEngine.INSTANCE.validateJson(VisEntityVirtualHashGrouper.Fields.class, combination, "combination");
		}

		CcpJsonRepresentation outOfTheRules = combinations.get(0).put(VisEntityVirtualHashGrouper.Fields.pcd, 3);
		try {
			CcpJsonValidatorEngine.INSTANCE.validateJson(VisEntityVirtualHashGrouper.Fields.class, outOfTheRules, "combination");
			fail("pcd 3 was accepted");
		} catch (CcpJsonValidationError e) {
		}
	}

	@Test
	public void aPartialChoiceKeepsOneValuePerDdd() {
		List<Integer> ddds = ALL_REGIONS.subList(0, 66);
		assertEquals(66, this.dddsOfTheHashes(ddds).size());
	}
}
