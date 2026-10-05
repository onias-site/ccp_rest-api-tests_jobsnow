package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * Proves the seeds of the skills, built from the synonym file and the resume counts of the documentation: the skills
 * come ranked by how many resumes mention them, and the words are grouped by their first two letters.
 */
public class SkillSeedsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void skillsAreRankedFromTheMostMentionedInResumes() {
		List<CcpBulkItem> seeds = new VisEntitySkill().getFirstRecordsToInsert();

		assertFalse(seeds.isEmpty());
		assertEquals("SQL", seeds.get(0).json.getAsString(VisJsonCommonsFields.skill));
		for (int index = 0; index < seeds.size(); index++) {
			CcpBulkItem item = seeds.get(index);
			assertEquals(CcpBulkEntityOperationType.create, item.operation);
			assertEquals(index + 1, (int) item.json.getAsIntegerNumber(VisJsonCommonsFields.ranking));
			assertFalse(item.json.containsField(VisJsonCommonsFields.resumesCount));
			assertTrue(item.json.getAsString(VisJsonCommonsFields.skill).length() <= 50);
		}
	}

	@Test
	public void wordsAreGroupedByTheirFirstTwoLetters() {
		List<CcpBulkItem> seeds = new VisEntityGroupPositionsBySkills().getFirstRecordsToInsert();

		assertFalse(seeds.isEmpty());
		for (CcpBulkItem group : seeds) {
			String initials = group.json.getAsString(VisEntityGroupPositionsBySkills.Fields.firstTwoInitials);
			assertEquals(2, initials.length());
			List<CcpJsonRepresentation> words = group.json.getAsJsonList(VisJsonCommonsFields.skill);
			for (CcpJsonRepresentation word : words) {
				String text = word.getAsString(VisJsonCommonsFields.word);
				assertTrue(text, text.startsWith(initials));
				assertTrue(text, text.length() >= 2 && text.length() <= 50);
				assertEquals(text, text.toUpperCase());
			}
		}
	}
}
