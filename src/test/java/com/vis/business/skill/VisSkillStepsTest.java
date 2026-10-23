package com.vis.business.skill;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.entity.CcpEntity;
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
import com.vis.entities.VisEntitySkillFixHierarchyItemPending;
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * The steps of the skill suggestion and of the fix of the skill hierarchy run directly, each one over the json the
 * steps before it would hand over: {@link VisBusinessCollectPendingFixHierarchyRequest},
 * {@link VisBusinessAddMissingSynonymsToSkill}, {@link VisBusinessCreateApprovedSkill},
 * {@link VisBusinessChangeTheSkillAndTheLookup}, {@link VisBusinessChangeOnlyTheSkillLookup}, the rejection branch of
 * {@link VisBusinessMoveFixHierarchyItem} and the orphan branch of {@link VisBusinessDeleteFixHierarchyItemIfOrphan}.
 * The steps that send messages or write the pending entities through the messaging are proved by
 * {@code SkillSuggestionThroughSupportBotTest} and {@code SkillFixHierarchyThroughSupportBotTest}.
 */
public class VisSkillStepsTest {

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

	private static final String SKILL = "TESTE PASSO DE SKILL";

	private static final String PARENT = "TESTE PAI DO PASSO";

	private final CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, SKILL);

	private CcpJsonRepresentation withTheStoredSkill(CcpJsonRepresentation storedSkill) {
		return this.skillKey.addToItem(CcpEntity.JsonFieldNames._entities, VisEntitySkill.ENTITY, storedSkill);
	}

	private List<CcpJsonRepresentation> lookupItemsOfTheSkill() {
		CcpJsonRepresentation groupKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityGroupPositionsBySkills.Fields.firstTwoInitials, SKILL.substring(0, 2));
		String groupId = VisEntityGroupPositionsBySkills.ENTITY.calculateId(groupKey);
		new CcpCacheDecorator(groupId).delete();
		CcpJsonRepresentation group = VisEntityGroupPositionsBySkills.ENTITY.getEntityMetaData().getOneByIdOrHandleItIfThisIdWasNotFound(groupKey, notFound -> CcpOtherConstants.EMPTY_JSON);
		List<CcpJsonRepresentation> items = group.getAsJsonList(VisEntityGroupPositionsBySkills.Fields.skill);
		return items.stream().filter(item -> SKILL.equals(item.getAsString(VisJsonCommonsFields.skill))).collect(Collectors.toList());
	}

	/** Takes the test skill out of the lookup, so that it does not stay in the real one. */
	private void removeTheSkillFromTheLookup() {
		CcpJsonRepresentation groupKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityGroupPositionsBySkills.Fields.firstTwoInitials, SKILL.substring(0, 2));
		CcpJsonRepresentation group = VisEntityGroupPositionsBySkills.ENTITY.getEntityMetaData().getOneByIdOrHandleItIfThisIdWasNotFound(groupKey, notFound -> CcpOtherConstants.EMPTY_JSON);
		List<CcpJsonRepresentation> items = group.getAsJsonList(VisEntityGroupPositionsBySkills.Fields.skill);
		List<CcpJsonRepresentation> otherItems = items.stream().filter(item -> false == SKILL.equals(item.getAsString(VisJsonCommonsFields.skill))).collect(Collectors.toList());
		CcpJsonRepresentation groupWithoutTheSkill = groupKey.put(VisEntityGroupPositionsBySkills.Fields.skill, otherItems);
		VisEntityGroupPositionsBySkills.ENTITY.save(groupWithoutTheSkill);
		String groupId = VisEntityGroupPositionsBySkills.ENTITY.calculateId(groupKey);
		new CcpCacheDecorator(groupId).delete();
	}

	@Test
	public void thePendingRequestIsCollectedWithItsStoredRecordAndTheReadableKey() {
		List<CcpJsonRepresentation> pendingRequests = new ArrayList<>();
		CcpJsonRepresentation storedRequest = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyPending.Fields.email, "hash")
				.put(VisEntitySkillFixHierarchyPending.Fields.description, "because");
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyPending.Fields.email, "candidato@jobsnow.com.br")
				.put(VisEntitySkillFixHierarchyPending.Fields.parent, PARENT)
				.put(VisEntitySkillFixHierarchyPending.Fields.type, "add")
				.addToItem(CcpEntity.JsonFieldNames._entities, VisEntitySkillFixHierarchyPending.ENTITY, storedRequest);

		CcpJsonRepresentation result = new VisBusinessCollectPendingFixHierarchyRequest(pendingRequests).execute(request);

		assertEquals(request, result);
		assertEquals(1, pendingRequests.size());
		CcpJsonRepresentation collected = pendingRequests.get(0);
		assertEquals("candidato@jobsnow.com.br", collected.getAsString(VisEntitySkillFixHierarchyPending.Fields.email));
		assertEquals("because", collected.getAsString(VisEntitySkillFixHierarchyPending.Fields.description));
		assertEquals(PARENT, collected.getAsString(VisEntitySkillFixHierarchyPending.Fields.parent));
		assertEquals("add", collected.getAsString(VisEntitySkillFixHierarchyPending.Fields.type));
	}

	@Test
	public void theStoredSkillGetsOnlyTheSynonymsItDidNotHave() {
		CcpJsonRepresentation storedSkill = this.skillKey
				.put(VisEntitySkill.Fields.synonym, Arrays.asList("TESTE ANTIGO", "TESTE COMUM"))
				.put(VisEntitySkill.Fields.ranking, 7);
		try {
			new VisBusinessAddMissingSynonymsToSkill(Arrays.asList("TESTE COMUM", "TESTE NOVO")).execute(this.withTheStoredSkill(storedSkill));

			CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(this.skillKey);
			assertEquals(Arrays.asList("TESTE ANTIGO", "TESTE COMUM", "TESTE NOVO"), skillRecord.getAsStringList(VisEntitySkill.Fields.synonym));
			assertEquals(Long.valueOf(7), skillRecord.getAsLongNumber(VisEntitySkill.Fields.ranking));
		} finally {
			VisEntitySkill.ENTITY.delete(this.skillKey);
		}
	}

	@Test
	public void theApprovedSkillIsCreatedAtTheEndOfTheRanking() {
		long skillsCount = CcpQueryOptions.INSTANCE.matchAll().selectFrom(VisEntitySkill.ENTITY).total();
		try {
			CcpJsonRepresentation skillNotFound = CcpOtherConstants.EMPTY_JSON;
			CcpJsonRepresentation result = new VisBusinessCreateApprovedSkill(this.skillKey, Arrays.asList("TESTE SINONIMO")).execute(skillNotFound);

			assertEquals(skillNotFound, result);
			CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(this.skillKey);
			assertEquals(Arrays.asList("TESTE SINONIMO"), skillRecord.getAsStringList(VisEntitySkill.Fields.synonym));
			// the count of Elasticsearch is near real time: a skill deleted just before (by another test) may leave it between
			// the count taken here and the one taken by the step
			long ranking = skillRecord.getAsLongNumber(VisEntitySkill.Fields.ranking);
			assertTrue("ranking " + ranking + " for " + skillsCount + " skills", ranking == skillsCount + 1 || ranking == skillsCount);
		} finally {
			VisEntitySkill.ENTITY.delete(this.skillKey);
		}
	}

	@Test
	public void theStoredSkillAndItsLookupGetTheNewParent() {
		CcpJsonRepresentation storedSkill = this.skillKey
				.put(VisEntitySkill.Fields.synonym, Arrays.asList("TESTE SINONIMO DO PASSO"))
				.put(VisEntitySkill.Fields.parent, Arrays.asList("TESTE PAI ANTIGO"))
				.put(VisEntitySkill.Fields.ranking, 7);
		VisSkillWordsGroups.addWord(SKILL, SKILL);
		List<String> words = new ArrayList<>(Arrays.asList(SKILL));
		try {
			new VisBusinessChangeTheSkillAndTheLookup(SKILL, words, parents -> parents.add(PARENT)).execute(this.withTheStoredSkill(storedSkill));

			CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(this.skillKey);
			assertEquals(Arrays.asList("TESTE PAI ANTIGO", PARENT), skillRecord.getAsStringList(VisEntitySkill.Fields.parent));
			assertTrue("the synonyms join the words whose lookup changes", words.contains("TESTE SINONIMO DO PASSO"));
			List<CcpJsonRepresentation> lookupItems = this.lookupItemsOfTheSkill();
			assertEquals(1, lookupItems.size());
			assertEquals(Arrays.asList(PARENT), lookupItems.get(0).getAsStringList(VisJsonCommonsFields.parent));
		} finally {
			VisEntitySkill.ENTITY.delete(this.skillKey);
			this.removeTheSkillFromTheLookup();
		}
	}

	@Test
	public void withNoStoredSkillOnlyTheLookupGetsTheNewParent() {
		VisSkillWordsGroups.addWord(SKILL, SKILL);
		try {
			CcpJsonRepresentation skillNotFound = CcpOtherConstants.EMPTY_JSON;
			CcpJsonRepresentation result = new VisBusinessChangeOnlyTheSkillLookup(SKILL, Arrays.asList(SKILL), parents -> parents.add(PARENT)).execute(skillNotFound);

			assertEquals(skillNotFound, result);
			List<CcpJsonRepresentation> lookupItems = this.lookupItemsOfTheSkill();
			assertEquals(1, lookupItems.size());
			assertEquals(Arrays.asList(PARENT), lookupItems.get(0).getAsStringList(VisJsonCommonsFields.parent));
			assertFalse(VisEntitySkill.ENTITY.exists(this.skillKey));
		} finally {
			this.removeTheSkillFromTheLookup();
		}
	}

	private CcpJsonRepresentation itemKey() {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyItemPending.Fields.parent, PARENT)
				.put(VisEntitySkillFixHierarchyItemPending.Fields.type, "add")
				.put(VisEntitySkillFixHierarchyItemPending.Fields.skill, SKILL);
	}

	@Test
	public void aRejectedItemGoesToTheTwinOfTheRejectedItems() {
		CcpJsonRepresentation itemKey = this.itemKey();
		CcpEntity rejectedItems = VisEntitySkillFixHierarchyItemPending.ENTITY.getTwinEntity();
		VisEntitySkillFixHierarchyItemPending.ENTITY.save(itemKey);
		try {
			CcpJsonRepresentation rejectedItem = itemKey
					.put(VisEntitySkillFixHierarchyPending.Fields.email, "candidato@jobsnow.com.br")
					.put(VisSkillFixHierarchyReviewFields.decision, VisSkillFixHierarchyDecisions.rejected.name());

			CcpJsonRepresentation result = VisBusinessMoveFixHierarchyItem.INSTANCE.execute(rejectedItem);

			assertEquals(rejectedItem, result);
			assertFalse(VisEntitySkillFixHierarchyItemPending.ENTITY.exists(itemKey));
			assertTrue(rejectedItems.exists(itemKey));
		} finally {
			VisEntitySkillFixHierarchyItemPending.ENTITY.deleteAnyWhere(itemKey);
		}
	}

	@Test
	public void anItemNoOtherRequestAsksForIsDeletedEverywhere() {
		CcpJsonRepresentation itemKey = this.itemKey();
		CcpEntity rejectedItems = VisEntitySkillFixHierarchyItemPending.ENTITY.getTwinEntity();
		VisEntitySkillFixHierarchyItemPending.ENTITY.save(itemKey);
		CcpJsonRepresentation deletedRequest = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyPending.Fields.email, "ninguem" + System.currentTimeMillis() + "@jobsnow.com.br")
				.put(VisEntitySkillFixHierarchyPending.Fields.parent, PARENT)
				.put(VisEntitySkillFixHierarchyPending.Fields.type, "add");

		CcpJsonRepresentation result = new VisBusinessDeleteFixHierarchyItemIfOrphan(deletedRequest).execute(itemKey);

		assertEquals(itemKey, result);
		assertFalse(VisEntitySkillFixHierarchyItemPending.ENTITY.exists(itemKey));
		assertFalse("deleted everywhere, the item does not count as rejected", rejectedItems.exists(itemKey));
	}
}
