package com.vis.business.skill;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
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
import com.vis.entities.VisEntitySkillFixHierarchyItemApproved;
import com.vis.json.fields.validation.VisSkillFixHierarchyTypes;

/**
 * 2026-11-20: an approval always changes {@link VisEntitySkill}. An approved association whose skill has no record
 * creates it, with the parent and a ranking, and the lookup of the resume screen finds it; an approved dissociation of a
 * skill with no record creates nothing.
 */
public class VisBusinessSkillFixHierarchyItemTest {

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

	private final long now = System.currentTimeMillis();

	private final String associatedSkill = "TESTE.SKILL.NOVA.ASSOCIADA." + this.now;

	private final String dissociatedSkill = "TESTE.SKILL.NOVA.DESASSOCIADA." + this.now;

	private CcpJsonRepresentation skillKey(String skill) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, skill);
	}

	private CcpJsonRepresentation approvedItem(VisSkillFixHierarchyTypes type, String skill) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyItemApproved.Fields.parent, "JAVA")
				.put(VisEntitySkillFixHierarchyItemApproved.Fields.type, type.name())
				.put(VisEntitySkillFixHierarchyItemApproved.Fields.skill, skill);
	}

	@After
	public void tearDown() {
		VisEntitySkill.ENTITY.delete(this.skillKey(this.associatedSkill));
		VisEntitySkill.ENTITY.delete(this.skillKey(this.dissociatedSkill));
	}

	@Test
	public void anApprovedAssociationCreatesTheSkillThatHasNoRecord() {
		assertFalse(VisEntitySkill.ENTITY.exists(this.skillKey(this.associatedSkill)));

		VisBusinessSkillFixHierarchyItem.INSTANCE.execute(this.approvedItem(VisSkillFixHierarchyTypes.add, this.associatedSkill));

		CcpJsonRepresentation created = VisEntitySkill.ENTITY.getOneById(this.skillKey(this.associatedSkill));
		assertEquals(List.of("JAVA"), created.getAsStringList(VisEntitySkill.Fields.parent));
		assertTrue(created.toString(), created.getAsLongNumber(VisEntitySkill.Fields.ranking) > 0);
		assertEquals(0, VisSkillWordsByInitials.getWordStatus(this.associatedSkill));

		// approving the same item again changes nothing
		VisBusinessSkillFixHierarchyItem.INSTANCE.execute(this.approvedItem(VisSkillFixHierarchyTypes.add, this.associatedSkill));
		CcpJsonRepresentation again = VisEntitySkill.ENTITY.getOneById(this.skillKey(this.associatedSkill));
		assertEquals(List.of("JAVA"), again.getAsStringList(VisEntitySkill.Fields.parent));
		assertEquals(created.getAsLongNumber(VisEntitySkill.Fields.ranking), again.getAsLongNumber(VisEntitySkill.Fields.ranking));
	}

	@Test
	public void anApprovedDissociationOfASkillWithNoRecordCreatesNothing() {
		VisBusinessSkillFixHierarchyItem.INSTANCE.execute(this.approvedItem(VisSkillFixHierarchyTypes.remove, this.dissociatedSkill));
		assertFalse(VisEntitySkill.ENTITY.exists(this.skillKey(this.dissociatedSkill)));
	}
}
