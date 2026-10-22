package com.vis.business.skill;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntityGroupPositionsBySkills;
import com.vis.entities.VisEntitySkill;
import com.vis.entities.VisEntitySkillApproved;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * The approval of a suggestion whose skill is already in {@link VisEntitySkill} (approved meanwhile from another
 * candidate's suggestion): the stored skill keeps its ranking and its synonyms, and only gets the suggested synonyms it
 * did not have, with no repetition. The creation of a new skill is covered by {@code SkillSuggestionThroughSupportBotTest}.
 */
public class VisBusinessSkillSuggestionApplyTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.instantMessenger,
				CcpLocalInstances.email,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp()
		);
	}

	private static final String SKILL = "TESTE SKILL JA EXISTENTE";

	@Test
	public void anExistingSkillOnlyGetsTheSynonymsItDidNotHave() {
		CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, SKILL);
		CcpJsonRepresentation storedSkillWithSynonyms = skillKey.put(VisEntitySkill.Fields.synonym, Arrays.asList("TESTE SINONIMO ANTIGO", "TESTE SINONIMO COMUM"));
		CcpJsonRepresentation storedSkill = storedSkillWithSynonyms.put(VisEntitySkill.Fields.ranking, 7);
		VisEntitySkill.ENTITY.save(storedSkill);

		try {
			CcpJsonRepresentation approvedSkill = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillApproved.Fields.skill, SKILL);
			CcpJsonRepresentation approved = approvedSkill.put(VisEntitySkillApproved.Fields.synonym, Arrays.asList("TESTE SINONIMO COMUM", "TESTE SINONIMO NOVO"));

			new VisBusinessSkillSuggestionApply().execute(approved);

			CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(skillKey);
			List<String> synonyms = skillRecord.getAsStringList(VisEntitySkill.Fields.synonym);
			List<String> expected = Arrays.asList("TESTE SINONIMO ANTIGO", "TESTE SINONIMO COMUM", "TESTE SINONIMO NOVO");
			assertEquals(expected, synonyms);
			Long ranking = skillRecord.getAsLongNumber(VisEntitySkill.Fields.ranking);
			assertEquals("the stored skill keeps its place in the ranking", Long.valueOf(7), ranking);
		} finally {
			VisEntitySkill.ENTITY.delete(skillKey);
			removeFromTheWordsGroup(SKILL, "TESTE SINONIMO COMUM", "TESTE SINONIMO NOVO");
		}
	}

	/**
	 * Removes the test words from the groups of the lookup, so that they do not stay in the real lookup.
	 */
	private static void removeFromTheWordsGroup(String... words) {
		for (String word : words) {
			String firstTwoInitials = word.substring(0, 2);
			CcpJsonRepresentation groupKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityGroupPositionsBySkills.Fields.firstTwoInitials, firstTwoInitials);
			CcpJsonRepresentation group = VisEntityGroupPositionsBySkills.ENTITY.getEntityMetaData().getOneByIdOrHandleItIfThisIdWasNotFound(groupKey, notFound -> CcpOtherConstants.EMPTY_JSON);
			List<CcpJsonRepresentation> items = group.getAsJsonList(VisEntityGroupPositionsBySkills.Fields.skill);
			List<CcpJsonRepresentation> otherItems = items.stream().filter(item -> false == word.equals(item.getAsString(VisJsonCommonsFields.word))).collect(Collectors.toList());
			boolean nothingToRemove = otherItems.size() == items.size();
			if(nothingToRemove) {
				continue;
			}
			CcpJsonRepresentation groupWithoutTheWord = groupKey.put(VisEntityGroupPositionsBySkills.Fields.skill, otherItems);
			VisEntityGroupPositionsBySkills.ENTITY.save(groupWithoutTheWord);
			String groupId = VisEntityGroupPositionsBySkills.ENTITY.calculateId(groupKey);
			new CcpCacheDecorator(groupId).delete();
		}
	}
}
