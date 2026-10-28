package com.vis.business.skill;

import static org.junit.Assert.assertEquals;

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
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntitySkill;
import com.vis.entities.VisEntitySkillApproved;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * The approval of a suggestion whose skill is already in {@link VisEntitySkill} (approved meanwhile from another
 * candidate's suggestion): the stored skill keeps its ranking and its synonyms, and only gets the suggested synonyms it
 * did not have, with no repetition, and the lookup of the resume screen finds the new synonym. The creation of a new skill is covered by {@code SkillSuggestionThroughSupportBotTest}.
 */
public class VisBusinessSkillSuggestionApplyTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.instantMessenger,
				CcpLocalInstances.email,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
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

			VisBusinessSkillSuggestionApply.INSTANCE.execute(approved);

			CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(skillKey);
			List<String> synonyms = skillRecord.getAsStringList(VisEntitySkill.Fields.synonym);
			List<String> expected = Arrays.asList("TESTE SINONIMO ANTIGO", "TESTE SINONIMO COMUM", "TESTE SINONIMO NOVO");
			assertEquals(expected, synonyms);
			Long ranking = skillRecord.getAsLongNumber(VisEntitySkill.Fields.ranking);
			assertEquals("the stored skill keeps its place in the ranking", Long.valueOf(7), ranking);
			List<CcpJsonRepresentation> lookupItems = VisSkillWordsByInitials.getItems(Arrays.asList("TESTE SINONIMO NOVO"));
			List<String> wordsOfTheSkill = lookupItems.stream()
					.filter(item -> SKILL.equals(item.getAsString(VisJsonCommonsFields.skill)))
					.map(item -> item.getAsString(VisJsonCommonsFields.word))
					.collect(Collectors.toList());
			assertEquals(Arrays.asList(SKILL, "TESTE SINONIMO ANTIGO", "TESTE SINONIMO COMUM", "TESTE SINONIMO NOVO"), wordsOfTheSkill);
		} finally {
			// the delete also takes the skill out of the groups of the lookup
			VisEntitySkill.ENTITY.delete(skillKey);
		}
	}
}
