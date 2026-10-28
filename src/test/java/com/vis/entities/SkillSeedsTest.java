package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityGrouper;
import com.vis.json.fields.validation.VisJsonCommonsFields;
import com.vis.json.transformers.VisSkillInitialsCombinations;

/**
 * Proves the seeds of the skills, built from the synonym file and the resume counts of the documentation: the skills
 * come ranked by how many resumes mention them, and each skill is a child, in {@code jn_grouper}, of the group of the
 * first two letters of each of its words, the groups written whole by the seed itself (a bulk, which the decorator of
 * {@code @CcpEntityGrouperChild} does not see).
 */
public class SkillSeedsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void skillsAreRankedFromTheMostMentionedInResumes() {
		List<CcpBulkItem> seeds = this.itemsOf(VisEntitySkill.ENTITY.getEntityMetaData().entityName);

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
	public void eachSkillIsInTheGroupOfTheInitialsOfEachOfItsWords() {
		List<CcpBulkItem> skills = this.itemsOf(VisEntitySkill.ENTITY.getEntityMetaData().entityName);
		List<CcpBulkItem> groups = this.itemsOf(JnEntityGrouper.ENTITY.getEntityMetaData().entityName);
		String parentName = VisEntityVirtualSkillInitialsGrouper.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntitySkill.ENTITY.getEntityMetaData().entityName;

		assertFalse(groups.isEmpty());
		Map<String, List<String>> childrenByParentId = new HashMap<>();
		for (CcpBulkItem group : groups) {
			assertEquals(CcpBulkEntityOperationType.create, group.operation);
			assertEquals(parentName, group.json.getAsString(JnEntityGrouper.Fields.parentName));
			assertEquals(childName, group.json.getAsString(JnEntityGrouper.Fields.childName));
			List<String> children = group.json.getAsStringList(JnEntityGrouper.Fields.child);
			assertEquals(children.size(), (int) group.json.getAsIntegerNumber(JnEntityGrouper.Fields.childCount));
			String initials = group.json.getInnerJson(JnEntityGrouper.Fields.parentJson).getAsString(VisEntityVirtualSkillInitialsGrouper.Fields.firstTwoInitials);
			assertEquals(initials, initials.toUpperCase());
			String parentId = group.json.getAsString(JnEntityGrouper.Fields.parentId);
			assertEquals(VisEntityVirtualSkillInitialsGrouper.getParentId(initials), parentId);
			childrenByParentId.put(parentId, children);
		}

		for (CcpBulkItem skill : skills) {
			List<String> expectedGroups = new ArrayList<>();
			for (CcpJsonRepresentation combination : VisSkillInitialsCombinations.INSTANCE.apply(skill.json)) {
				expectedGroups.add(VisEntityVirtualSkillInitialsGrouper.ENTITY.calculateId(combination));
			}
			List<String> storedGroups = skill.json.getAsStringList(VisEntitySkill.Fields.initialsGroup);
			assertEquals(skill.json.toString(), expectedGroups, storedGroups);
			for (String parentId : storedGroups) {
				List<String> children = childrenByParentId.get(parentId);
				assertTrue(skill.json.toString(), children.contains(skill.id));
			}
		}
	}

	private List<CcpBulkItem> itemsOf(String entityName) {
		List<CcpBulkItem> seeds = VisEntitySkill.INSTANCE.getFirstRecordsToInsert();
		List<CcpBulkItem> itemsOfTheEntity = new ArrayList<>();
		for (CcpBulkItem seed : seeds) {
			String seedEntityName = seed.entity.getEntityMetaData().entityName;
			boolean otherEntity = false == entityName.equals(seedEntityName);

			if(otherEntity) {
				continue;
			}

			itemsOfTheEntity.add(seed);
		}
		return itemsOfTheEntity;
	}
}
