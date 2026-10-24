package com.vis.business.bots;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * The Portuguese names of the commands of the support bot ({@link VisCommandNamesInPortuguese}): what the support team
 * types after the {@code /}. Each name is a contract with the operators, so the test pins every one of them.
 */
public class VisCommandNamesInPortugueseTest {

	@Test
	public void eachCommandHasItsPortugueseName() {
		Map<VisCommandNamesInPortuguese, String> expected = new LinkedHashMap<>();
		expected.put(VisCommandNamesInPortuguese.fixSkillHierarchy, "ajustarHierarquiaDeHabilidades");
		expected.put(VisCommandNamesInPortuguese.reviewSkillSuggestion, "avaliarSugestaoDeHabilidade");

		Map<VisCommandNamesInPortuguese, String> actual = new LinkedHashMap<>();
		for (VisCommandNamesInPortuguese command : VisCommandNamesInPortuguese.values()) {
			actual.put(command, command.getValue());
		}

		assertEquals(expected, actual);
	}
}
