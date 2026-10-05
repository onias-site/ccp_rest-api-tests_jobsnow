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
 * ({@code vis_group_positions_by_skills}): known skills are found, short words only as whole words, excluded skills
 * are left out and an empty text finds nothing.
 */
public class SkillsFromTextTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
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

	@Test
	public void anEmptyTextFindsNothing() {
		CcpJsonRepresentation response = this.find("   ", Arrays.asList());

		assertTrue(response.isEmpty());
	}
}
