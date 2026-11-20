package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;
import com.vis.business.skill.VisBusinessSkillFixHierarchyCreateItems;
import com.vis.business.skill.VisSuggestionStatusCounters;
import com.vis.business.skill.VisSuggestionStatuses;
import com.vis.business.statistics.VisBusinessCuriositiesCharts;
import com.vis.json.fields.validation.VisSkillFixHierarchyTypes;

/**
 * 2026-11-20: the charts of the screen Curiosities about the suggestions by status (pending, approved, rejected): the
 * suggestions of a new skill and the items of the suggestions of association and of dissociation of the hierarchy, each
 * one a group of counters of {@code jn_grouper} corrected by the side effects of the writes and of the transfers.
 */
public class SuggestionStatusCountersTest {
	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> RecordingEmailSender.INSTANCE;
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
	}

	private final long now = System.currentTimeMillis();

	private final String candidate = "statuscounter" + this.now + "@jobsnow.com";

	private final String approvedSkill = "TESTE CONTADOR APROVADA " + this.now;

	private final String rejectedSkill = "TESTE CONTADOR REJEITADA " + this.now;

	private final String withdrawnSkill = "TESTE CONTADOR RETIRADA " + this.now;

	private final String parent = "TESTE.PAI.CONTADOR." + this.now;

	private final List<String> itemSkills = Arrays.asList("TESTE.ITEM.A." + this.now, "TESTE.ITEM.B." + this.now, "TESTE.ITEM.C." + this.now);

	private CcpJsonRepresentation suggestionKey(String skill) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillPending.Fields.email, this.candidate)
				.put(VisEntitySkillPending.Fields.skill, skill);
	}

	private CcpJsonRepresentation suggestion(String skill) {
		return this.suggestionKey(skill)
				.put(VisEntitySkillPending.Fields.description, "Teste do contador de sugestoes por situacao")
				.put(VisEntitySkillPending.Fields.synonym, Arrays.asList());
	}

	private CcpJsonRepresentation decided(String skill) {
		return this.suggestion(skill).put(JnJsonCommonsFields.explanation, "Decisao do teste do contador");
	}

	private CcpJsonRepresentation item(String skill) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyItemPending.Fields.parent, this.parent)
				.put(VisEntitySkillFixHierarchyItemPending.Fields.type, VisSkillFixHierarchyTypes.add.name())
				.put(VisEntitySkillFixHierarchyItemPending.Fields.skill, skill);
	}

	@After
	public void tearDown() {
		for (String skill : Arrays.asList(this.approvedSkill, this.rejectedSkill, this.withdrawnSkill)) {
			VisEntitySkillPending.ENTITY.delete(this.suggestion(skill));
			VisEntitySkillApproved.ENTITY.delete(this.suggestionKey(skill));
			VisEntitySkillRejected.ENTITY.delete(this.suggestionKey(skill));
			CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, skill);
			VisEntitySkillReviewed.ENTITY.delete(skillKey);
			VisEntitySkill.ENTITY.delete(skillKey);
		}
		for (String skill : this.itemSkills) {
			VisEntitySkillFixHierarchyItemPending.ENTITY.deleteAnyWhere(this.item(skill));
			VisEntitySkillFixHierarchyItemApproved.ENTITY.delete(this.item(skill));
		}
	}

	private static CcpJsonRepresentation counters(VisSuggestionStatusCounters counting, String counterName) {
		CcpJsonRepresentation combination = VisEntityVirtualSuggestionStatusCounter.getCombination(counterName);
		String parentId = VisEntityVirtualSuggestionStatusCounter.ENTITY.calculateId(combination);
		CcpEntityMetaData parentDetails = VisEntityVirtualSuggestionStatusCounter.ENTITY.getEntityMetaData();
		CcpEntity childEntity = counting.getChildEntity();
		CcpEntityMetaData childDetails = childEntity.getEntityMetaData();
		return JnGroupers.getCounters(parentDetails.entityName, childDetails.entityName, parentId);
	}

	private static long countOf(CcpJsonRepresentation counters, VisSuggestionStatuses status) {
		return counters.containsAllFields(status) ? counters.getAsLongNumber(status) : 0L;
	}

	private static void assertChange(CcpJsonRepresentation before, CcpJsonRepresentation after, long pending, long approved, long rejected) {
		String message = before + " -> " + after;
		assertEquals(message, pending, countOf(after, VisSuggestionStatuses.pending) - countOf(before, VisSuggestionStatuses.pending));
		assertEquals(message, approved, countOf(after, VisSuggestionStatuses.approved) - countOf(before, VisSuggestionStatuses.approved));
		assertEquals(message, rejected, countOf(after, VisSuggestionStatuses.rejected) - countOf(before, VisSuggestionStatuses.rejected));
	}

	private static CcpJsonRepresentation skillSuggestions() {
		return counters(VisSuggestionStatusCounters.skillSuggestion, VisSuggestionStatusCounters.skillSuggestion.name());
	}

	private static CcpJsonRepresentation hierarchyItems(VisSkillFixHierarchyTypes type) {
		return counters(VisSuggestionStatusCounters.skillFixHierarchyItem, type.name());
	}

	@Test
	public void theSuggestionsOfSkillAreCountedByStatus() {
		CcpJsonRepresentation start = skillSuggestions();

		VisEntitySkillPending.ENTITY.save(this.suggestion(this.approvedSkill));
		VisEntitySkillPending.ENTITY.save(this.suggestion(this.rejectedSkill));
		VisEntitySkillPending.ENTITY.save(this.suggestion(this.withdrawnSkill));
		assertChange(start, skillSuggestions(), 3, 0, 0);

		// saving a pending suggestion again (update) changes nothing
		VisEntitySkillPending.ENTITY.save(this.suggestion(this.withdrawnSkill));
		assertChange(start, skillSuggestions(), 3, 0, 0);

		VisEntitySkillPending.ENTITY.transferDataTo(this.decided(this.approvedSkill), VisEntitySkillApproved.ENTITY);
		assertChange(start, skillSuggestions(), 2, 1, 0);

		VisEntitySkillPending.ENTITY.transferDataTo(this.decided(this.rejectedSkill), VisEntitySkillRejected.ENTITY);
		assertChange(start, skillSuggestions(), 1, 1, 1);

		// the candidate gives the suggestion up; deleting it again finds nothing and changes nothing
		VisEntitySkillPending.ENTITY.delete(this.suggestion(this.withdrawnSkill));
		VisEntitySkillPending.ENTITY.delete(this.suggestion(this.withdrawnSkill));
		assertChange(start, skillSuggestions(), 0, 1, 1);

		VisEntitySkillApproved.ENTITY.delete(this.suggestionKey(this.approvedSkill));
		VisEntitySkillRejected.ENTITY.delete(this.suggestionKey(this.rejectedSkill));
		assertChange(start, skillSuggestions(), 0, 0, 0);
	}

	@Test
	public void theItemsOfHierarchyAreCountedByTypeAndStatus() {
		CcpJsonRepresentation startAdd = hierarchyItems(VisSkillFixHierarchyTypes.add);
		CcpJsonRepresentation startRemove = hierarchyItems(VisSkillFixHierarchyTypes.remove);
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyPending.Fields.email, this.candidate)
				.put(VisEntitySkillFixHierarchyPending.Fields.parent, this.parent)
				.put(VisEntitySkillFixHierarchyPending.Fields.type, VisSkillFixHierarchyTypes.add.name())
				.put(VisEntitySkillFixHierarchyPending.Fields.description, "Teste do contador de itens por situacao")
				.put(VisEntitySkillFixHierarchyPending.Fields.skill, this.itemSkills);

		VisBusinessSkillFixHierarchyCreateItems.INSTANCE.execute(request);
		assertChange(startAdd, hierarchyItems(VisSkillFixHierarchyTypes.add), 3, 0, 0);

		// the items already pending are not created again, nor counted again
		VisBusinessSkillFixHierarchyCreateItems.INSTANCE.execute(request);
		assertChange(startAdd, hierarchyItems(VisSkillFixHierarchyTypes.add), 3, 0, 0);

		CcpJsonRepresentation itemA = this.item(this.itemSkills.get(0));
		CcpJsonRepresentation itemB = this.item(this.itemSkills.get(1));
		CcpJsonRepresentation itemC = this.item(this.itemSkills.get(2));

		VisEntitySkillFixHierarchyItemPending.ENTITY.transferDataTo(itemA, VisEntitySkillFixHierarchyItemApproved.ENTITY);
		assertChange(startAdd, hierarchyItems(VisSkillFixHierarchyTypes.add), 2, 1, 0);

		// the delete moves the item to the twin, the rejected items
		VisEntitySkillFixHierarchyItemPending.ENTITY.delete(itemB);
		CcpEntity rejectedItems = VisEntitySkillFixHierarchyItemPending.ENTITY.getTwinEntity();
		assertTrue(rejectedItems.exists(itemB));
		assertChange(startAdd, hierarchyItems(VisSkillFixHierarchyTypes.add), 1, 1, 1);

		// an item no pending request names any more leaves from the side it is on
		VisEntitySkillFixHierarchyItemPending.ENTITY.deleteAnyWhere(itemC);
		assertChange(startAdd, hierarchyItems(VisSkillFixHierarchyTypes.add), 0, 1, 1);
		VisEntitySkillFixHierarchyItemPending.ENTITY.deleteAnyWhere(itemB);
		assertChange(startAdd, hierarchyItems(VisSkillFixHierarchyTypes.add), 0, 1, 0);

		VisEntitySkillFixHierarchyItemApproved.ENTITY.delete(itemA);
		assertChange(startAdd, hierarchyItems(VisSkillFixHierarchyTypes.add), 0, 0, 0);

		// the other type is a group of its own
		assertChange(startRemove, hierarchyItems(VisSkillFixHierarchyTypes.remove), 0, 0, 0);
	}

	@Test
	public void theScreenCuriositiesReadTheThreeCharts() {
		CcpJsonRepresentation output = VisBusinessCuriositiesCharts.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
		String charts = output.toString();
		for (VisBusinessCuriositiesCharts.Fields chart : Arrays.asList(VisBusinessCuriositiesCharts.Fields.skillSuggestionStatuses, VisBusinessCuriositiesCharts.Fields.hierarchyAddStatuses, VisBusinessCuriositiesCharts.Fields.hierarchyRemoveStatuses)) {
			assertTrue(charts, charts.contains("\"" + chart.name() + "\""));
		}
	}
}
