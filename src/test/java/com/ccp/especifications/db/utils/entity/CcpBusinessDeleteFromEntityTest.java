package com.ccp.especifications.db.utils.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntitySkillSynonym;

/**
 * {@link CcpBusinessDeleteRecordFromEntity} deletes the record it was given and {@link CcpBusinessDeleteStoredRecordFromEntity}
 * the one its input carries under {@code _entities}; both answer their input.
 */
public class CcpBusinessDeleteFromEntityTest {

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

	private final String synonym = "DELETE TEST " + System.currentTimeMillis() % 100000;

	private final CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON
			.put(VisEntitySkillSynonym.Fields.skill, this.synonym)
			.put(VisEntitySkillSynonym.Fields.synonym, "ANY SKILL");

	@After
	public void deleteTheRecord() {
		VisEntitySkillSynonym.ENTITY.delete(this.record);
	}

	@Test
	public void theGivenRecordIsDeletedAndTheInputComesBack() {
		VisEntitySkillSynonym.ENTITY.save(this.record);
		assertTrue(VisEntitySkillSynonym.ENTITY.exists(this.record));
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON;

		CcpJsonRepresentation output = new CcpBusinessDeleteRecordFromEntity(VisEntitySkillSynonym.ENTITY, this.record).execute(input);

		assertEquals(input, output);
		assertFalse(VisEntitySkillSynonym.ENTITY.exists(this.record));
	}

	@Test
	public void theStoredRecordIsDeletedAndTheInputComesBack() {
		VisEntitySkillSynonym.ENTITY.save(this.record);
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.addToItem(CcpEntity.JsonFieldNames._entities, VisEntitySkillSynonym.ENTITY, this.record);

		CcpJsonRepresentation output = new CcpBusinessDeleteStoredRecordFromEntity(VisEntitySkillSynonym.ENTITY).execute(input);

		assertEquals(input, output);
		assertFalse(VisEntitySkillSynonym.ENTITY.exists(this.record));
	}
}
