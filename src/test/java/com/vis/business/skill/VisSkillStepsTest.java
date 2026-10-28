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
import com.jn.utils.JnGroupers;
import com.vis.entities.VisEntitySkill;
import com.vis.entities.VisEntitySkillFixHierarchyItemPending;
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.entities.VisEntityVirtualSkillInitialsGrouper;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * The steps of the skill suggestion and of the fix of the skill hierarchy run directly, each one over the json the
 * steps before it would hand over: {@link VisBusinessCollectPendingFixHierarchyRequest},
 * {@link VisBusinessAddMissingSynonymsToSkill}, {@link VisBusinessCreateApprovedSkill},
 * {@link VisBusinessChangeTheSkillParents} (the groups of initials of the lookup follow the saved skill), the rejection
 * branch of
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

	/** The items of the lookup whose skill is the test skill, among the words with the initials of the given word. */
	private List<CcpJsonRepresentation> lookupItemsOfTheSkill(String wordWithTheInitials) {
		List<CcpJsonRepresentation> items = VisSkillWordsByInitials.getItems(Arrays.asList(wordWithTheInitials));
		return items.stream().filter(item -> SKILL.equals(item.getAsString(VisJsonCommonsFields.skill))).collect(Collectors.toList());
	}

	/** The ids of the skills in the group of the initials of the word. */
	private List<String> skillsInTheGroupOf(String word) {
		String parentName = VisEntityVirtualSkillInitialsGrouper.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntitySkill.ENTITY.getEntityMetaData().entityName;
		String parentId = VisEntityVirtualSkillInitialsGrouper.getParentId(word);
		return JnGroupers.getChildren(false, parentName, childName, parentId);
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
			List<String> words = this.lookupItemsOfTheSkill(SKILL).stream().map(item -> item.getAsString(VisJsonCommonsFields.word)).collect(Collectors.toList());
			assertTrue(words.toString(), words.contains("TESTE NOVO"));
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
		try {
			new VisBusinessChangeTheSkillParents(parents -> parents.add(PARENT)).execute(this.withTheStoredSkill(storedSkill));

			CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(this.skillKey);
			List<String> parents = Arrays.asList("TESTE PAI ANTIGO", PARENT);
			assertEquals(parents, skillRecord.getAsStringList(VisEntitySkill.Fields.parent));
			// the skill and its synonym, both under TE, carry the new parent
			List<CcpJsonRepresentation> lookupItems = this.lookupItemsOfTheSkill(SKILL);
			assertEquals(2, lookupItems.size());
			for (CcpJsonRepresentation lookupItem : lookupItems) {
				assertEquals(parents, lookupItem.getAsStringList(VisJsonCommonsFields.parent));
				assertEquals(Arrays.asList("TESTE SINONIMO DO PASSO"), lookupItem.getAsStringList(VisJsonCommonsFields.synonym));
			}
		} finally {
			VisEntitySkill.ENTITY.delete(this.skillKey);
		}
	}

	@Test
	public void aParentWithOtherInitialsMovesTheSkillBetweenTheGroups() {
		String parentWithOtherInitials = "QQ TESTE PAI DO PASSO";
		CcpJsonRepresentation storedSkill = this.skillKey.put(VisEntitySkill.Fields.ranking, 7);
		VisEntitySkill.ENTITY.save(storedSkill);
		String skillId = VisEntitySkill.ENTITY.calculateId(this.skillKey);
		try {
			assertFalse(this.skillsInTheGroupOf(parentWithOtherInitials).contains(skillId));

			new VisBusinessChangeTheSkillParents(parents -> parents.add(parentWithOtherInitials)).execute(this.withTheStoredSkill(storedSkill));

			assertTrue(this.skillsInTheGroupOf(SKILL).contains(skillId));
			assertTrue(this.skillsInTheGroupOf(parentWithOtherInitials).contains(skillId));
			CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(this.skillKey);
			List<String> expectedGroups = Arrays.asList(VisEntityVirtualSkillInitialsGrouper.getParentId(SKILL), VisEntityVirtualSkillInitialsGrouper.getParentId(parentWithOtherInitials));
			assertEquals(expectedGroups, skillRecord.getAsStringList(VisEntitySkill.Fields.initialsGroup));
			// a parent that is no skill of its own is found as a skill by itself
			List<CcpJsonRepresentation> parentItems = VisSkillWordsByInitials.getItems(Arrays.asList(parentWithOtherInitials));
			assertTrue(parentItems.toString(), parentItems.stream().anyMatch(item -> parentWithOtherInitials.equals(item.getAsString(VisJsonCommonsFields.skill))));

			new VisBusinessChangeTheSkillParents(parents -> parents.remove(parentWithOtherInitials)).execute(this.withTheStoredSkill(skillRecord));

			assertTrue(this.skillsInTheGroupOf(SKILL).contains(skillId));
			assertFalse(this.skillsInTheGroupOf(parentWithOtherInitials).contains(skillId));
		} finally {
			VisEntitySkill.ENTITY.delete(this.skillKey);
		}
		assertFalse("the delete takes the skill out of its groups", this.skillsInTheGroupOf(SKILL).contains(skillId));
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
