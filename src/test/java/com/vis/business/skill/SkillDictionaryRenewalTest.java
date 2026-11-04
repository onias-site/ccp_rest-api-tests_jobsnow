package com.vis.business.skill;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.vis.business.telegram.VisSkillWordsInMemory;
import com.vis.entities.VisEntitySkill;
import com.vis.entities.VisEntitySkillDictionary;

/**
 * The dictionary of skills in memory ({@link VisSkillDictionary}) is renewed by version: while the version of
 * {@code vis_skill_dictionary} stays the same, every reading gets the same copy; a save of a skill writes a new version
 * ({@code @CcpAfterSaveFromMainEntity} of {@link VisEntitySkill}), and the next reading gets a new copy. Runs on the local
 * Elasticsearch, whose {@code vis_skill_dictionary} was written by {@code documentation/deploy/24-dicionario-de-skills.py}.
 */
public class SkillDictionaryRenewalTest {

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

	private CcpJsonRepresentation readDictionary() {
		CcpJsonRepresentation key = VisEntitySkillDictionary.getKeyOfTheSkills();
		CcpJsonRepresentation dictionary = VisEntitySkillDictionary.ENTITY.getOneById(key);
		return dictionary;
	}

	@Test
	public void sameVersionKeepsTheSameCopy() {
		VisSkillWordsInMemory first = VisSkillDictionary.getWords();
		VisSkillWordsInMemory second = VisSkillDictionary.getWords();
		assertSame(first, second);
	}

	@Test
	public void savingASkillWritesANewVersionAndRenewsTheCopy() {
		VisSkillWordsInMemory before = VisSkillDictionary.getWords();
		CcpJsonRepresentation dictionaryBefore = this.readDictionary();
		Long versionBefore = dictionaryBefore.getAsLongNumber(VisEntitySkillDictionary.Fields.version);
		List<String> skillIdsBefore = dictionaryBefore.getAsStringList(VisEntitySkillDictionary.Fields.skillIds);

		// an existing skill saved as it is: the dictionary gets a new version, with the same ids
		CcpJsonRepresentation javaKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, "JAVA");
		CcpJsonRepresentation java = VisEntitySkill.ENTITY.getOneById(javaKey);
		VisEntitySkill.ENTITY.save(java);

		CcpJsonRepresentation dictionaryAfter = this.readDictionary();
		Long versionAfter = dictionaryAfter.getAsLongNumber(VisEntitySkillDictionary.Fields.version);
		List<String> skillIdsAfter = dictionaryAfter.getAsStringList(VisEntitySkillDictionary.Fields.skillIds);
		assertTrue(versionBefore + " -> " + versionAfter, versionAfter > versionBefore);
		Set<String> distinctSkillIdsAfter = new HashSet<>(skillIdsAfter);
		assertEquals(skillIdsBefore.size(), skillIdsAfter.size());
		assertEquals(skillIdsAfter.size(), distinctSkillIdsAfter.size());
		String javaId = VisEntitySkill.ENTITY.calculateId(java);
		assertTrue(skillIdsAfter.contains(javaId));

		VisSkillWordsInMemory after = VisSkillDictionary.getWords();
		assertNotSame(before, after);
	}
}
