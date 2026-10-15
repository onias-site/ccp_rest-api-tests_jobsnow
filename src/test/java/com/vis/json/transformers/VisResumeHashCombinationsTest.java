package com.vis.json.transformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Proves the hashes of a resume: the regions (ddd) stay out of them, so the number of combinations does not grow with
 * the number of regions (until 2026-10-14 it was multiplied by it, up to 48,720 groups for one resume).
 */
public class VisResumeHashCombinationsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final List<Integer> ALL_REGIONS = Arrays.asList(
			61, 62, 64, 65, 66, 67, 82, 71, 73, 74, 75, 77, 85, 88, 98, 99, 83, 81, 87, 86, 89, 84, 79, 68, 96, 92, 97, 91,
			93, 94, 69, 95, 63, 27, 28, 31, 32, 33, 34, 35, 37, 38, 21, 22, 24, 11, 12, 13, 14, 15, 16, 17, 18, 19, 41, 42,
			43, 44, 45, 46, 51, 53, 54, 55, 47, 48, 49);

	private CcpJsonRepresentation resume(List<Integer> ddds) {
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
		return resume;
	}

	@Test
	public void theRegionsDoNotMultiplyTheCombinations() {
		assertEquals(67, ALL_REGIONS.size());
		VisResumeHashCombinations combinations = new VisResumeHashCombinations();
		long oneRegion = combinations.countCombinations(this.resume(Arrays.asList(11)));
		long homeOffice = combinations.countCombinations(this.resume(Arrays.asList(10)));
		long everyRegion = combinations.countCombinations(this.resume(ALL_REGIONS));
		assertEquals(oneRegion, homeOffice);
		assertEquals(oneRegion, everyRegion);
	}

	@Test
	public void noCombinationCarriesTheDdd() {
		List<CcpJsonRepresentation> combinations = new VisResumeHashCombinations().apply(this.resume(Arrays.asList(11, 21)));
		for (CcpJsonRepresentation combination : combinations) {
			assertFalse(combination.fieldSet().contains("ddd"));
		}
	}

	@Test
	public void everyCombinationObeysTheRulesOfTheGrouper() {
		List<Integer> ddds = new ArrayList<>(ALL_REGIONS);
		ddds.add(10);

		List<CcpJsonRepresentation> combinations = new VisResumeHashCombinations().apply(this.resume(ddds));

		// languages: none, Espanhol 2, Espanhol 1; skills: none, JAVA, JVM; three flags with 2 values each
		assertEquals(3 * 3 * 2 * 2 * 2, combinations.size());
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
}
