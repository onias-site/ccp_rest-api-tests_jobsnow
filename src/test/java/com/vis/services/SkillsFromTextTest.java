package com.vis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * Proves {@link VisServiceSkills#GetSkillsFromText} against the skill groups seeded in the local Elasticsearch
 * (the groups of initials of {@code vis_skill} in {@code jn_grouper}): known skills are found, short words only as whole words, excluded skills
 * are left out and an empty text finds nothing.
 */
public class SkillsFromTextTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE);
	}

	private CcpJsonRepresentation find(String text, List<CcpJsonRepresentation> excludedSkills) {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(Fields.text, text)
				.put(Fields.excludedSkill, excludedSkills);
		CcpJsonRepresentation response = VisServiceSkills.GetSkillsFromText.execute(request);
		return response;
	}

	private List<String> skillNames(CcpJsonRepresentation response) {
		List<CcpJsonRepresentation> skills = response.getAsJsonList(VisJsonCommonsFields.skill);
		List<String> names = skills.stream().map(skill -> skill.getAsString(VisJsonCommonsFields.skill)).collect(Collectors.toList());
		return names;
	}

	@Test
	public void knownSkillsAreFoundInAnyCase() {
		CcpJsonRepresentation response = this.find("Developer with java and sql experience", Arrays.asList());

		List<String> names = this.skillNames(response);
		assertTrue(names.toString(), names.contains("JAVA"));
		assertTrue(names.toString(), names.contains("SQL"));
	}

	@Test
	public void eachChosenSkillHasALabel() {
		CcpJsonRepresentation response = this.find("JAVA", Arrays.asList());

		List<CcpJsonRepresentation> skills = response.getAsJsonList(VisJsonCommonsFields.skill);
		assertFalse(skills.isEmpty());
		for (CcpJsonRepresentation skill : skills) {
			assertFalse(skill.getAsString(VisJsonCommonsFields.label).isEmpty());
		}
	}

	/**
	 * Each chosen skill brings its synonyms, read from {@code vis_skill}: the screen needs them to refuse the suggestion
	 * of a skill already in the resume under another name.
	 */
	@Test
	public void eachChosenSkillBringsItsSynonyms() {
		CcpJsonRepresentation response = this.find("Developer with java and sql experience", Arrays.asList());

		List<CcpJsonRepresentation> skills = response.getAsJsonList(VisJsonCommonsFields.skill);
		CcpJsonRepresentation java = skills.stream().filter(skill -> "JAVA".equals(skill.getAsString(VisJsonCommonsFields.skill))).findFirst().get();
		List<String> synonyms = java.getAsStringList(VisJsonCommonsFields.synonym);
		assertTrue(synonyms.toString(), synonyms.contains("JAVA 8"));
	}

	@Test
	public void anExcludedSkillIsLeftOut() {
		CcpJsonRepresentation excludedJava = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonCommonsFields.skill, "JAVA")
				.put(VisJsonCommonsFields.word, "JAVA");

		CcpJsonRepresentation response = this.find("JAVA and SQL", Arrays.asList(excludedJava));

		List<String> names = this.skillNames(response);
		assertFalse(names.toString(), names.contains("JAVA"));
		assertTrue(names.toString(), names.contains("SQL"));
		assertEquals(1, response.getAsJsonList(Fields.excludedSkill).size());
	}

	@Test
	public void aShortSkillInsideAnotherWordIsDiscarded() {
		CcpJsonRepresentation response = this.find("MYSQLX", Arrays.asList());

		assertFalse(this.skillNames(response).contains("SQL"));
	}

	/** "/" is not a delimiter, but each side of a slashed word is also a word: SCRUM/KANBAN finds SCRUM and KANBAM. */
	@Test
	public void eachSideOfASlashedWordIsAWord() {
		CcpJsonRepresentation response = this.find("SCRUM/KANBAN, REST/SOAP e JAVA/ANGULAR", Arrays.asList());

		List<String> names = this.skillNames(response);
		for (String skill : Arrays.asList("SCRUM", "KANBAM", "REST", "SOAP", "JAVA", "ANGULAR")) {
			assertTrue(names.toString(), names.contains(skill));
		}
	}

	/** A slashed skill word stays whole, and its sides are not skills of their own unless they also stand alone. */
	@Test
	public void aSlashedSkillWordKeepsItsSidesOut() {
		List<String> slashedOnly = this.skillNames(this.find("PL/SQL e CI/CD", Arrays.asList()));
		assertTrue(slashedOnly.toString(), slashedOnly.contains("PLSQL"));
		assertTrue(slashedOnly.toString(), slashedOnly.contains("DEVOPS"));
		assertFalse(slashedOnly.toString(), slashedOnly.contains("SQL"));

		List<String> withTheSideAlone = this.skillNames(this.find("PL/SQL e SQL", Arrays.asList()));
		assertTrue(withTheSideAlone.toString(), withTheSideAlone.contains("PLSQL"));
		assertTrue(withTheSideAlone.toString(), withTheSideAlone.contains("SQL"));
	}

	/** Words of 2 characters are found as whole words. */
	@Test
	public void twoCharacterWordsAreFound() {
		List<String> names = this.skillNames(this.find("Dev C# com QA, UX e AWS S3", Arrays.asList()));

		for (String skill : Arrays.asList("CSHARP", "QA", "UX", "S3")) {
			assertTrue(names.toString(), names.contains(skill));
		}
	}

	@Test
	public void anEmptyTextFindsNothing() {
		CcpJsonRepresentation response = this.find("   ", Arrays.asList());

		assertTrue(response.isEmpty());
	}
}
