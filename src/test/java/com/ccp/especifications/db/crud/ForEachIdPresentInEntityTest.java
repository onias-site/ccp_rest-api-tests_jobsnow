package com.ccp.especifications.db.crud;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntitySkillSynonym;

/**
 * {@code forEachIdPresentInEntity} reads every key passed to {@code CcpGetEntityId} in the same union all and runs the
 * action once for each key present in the entity, with that key's own record. Created on 2026-10-15 for loops that
 * used to call {@code exists} once per item; {@code ifThisIdIsPresentInEntity}, given the same keys, runs once, with the
 * first record found.
 */
public class ForEachIdPresentInEntityTest {

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

	private final long run = System.currentTimeMillis() % 100000;
	private final String first = "FOREACH A " + this.run;
	private final String missing = "FOREACH B " + this.run;
	private final String last = "FOREACH C " + this.run;

	private static CcpJsonRepresentation key(String synonym) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillSynonym.Fields.skill, synonym);
	}

	private static void save(String synonym, String skill) {
		CcpJsonRepresentation synonymKey = key(synonym);
		CcpJsonRepresentation synonymRecord = synonymKey.put(VisEntitySkillSynonym.Fields.synonym, skill);
		VisEntitySkillSynonym.ENTITY.save(synonymRecord);
	}

	@After
	public void deleteTheRecords() {
		VisEntitySkillSynonym.ENTITY.delete(key(this.first));
		VisEntitySkillSynonym.ENTITY.delete(key(this.last));
	}

	/** Collects, for each run of the action, the key it got and the record found for that key. */
	private static CcpBusiness collectInto(List<String> keysSeen, List<String> recordsSeen) {
		return keyWithItsRecord -> {
			String synonym = keyWithItsRecord.getAsString(VisEntitySkillSynonym.Fields.skill);
			CcpJsonRepresentation record = keyWithItsRecord.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, VisEntitySkillSynonym.ENTITY);
			String skill = record.getAsString(VisEntitySkillSynonym.Fields.synonym);
			keysSeen.add(synonym);
			recordsSeen.add(skill);
			return keyWithItsRecord;
		};
	}

	@Test
	public void theActionRunsOnceForEachKeyFoundWithItsOwnRecord() {
		save(this.first, "SKILL OF A");
		save(this.last, "SKILL OF C");
		List<String> keysSeen = new ArrayList<>();
		List<String> recordsSeen = new ArrayList<>();
		CcpBusiness collect = collectInto(keysSeen, recordsSeen);

		new CcpGetEntityId(key(this.first), key(this.missing), key(this.last))
		.toBeginProcedureAnd()
			.forEachIdPresentInEntity(VisEntitySkillSynonym.ENTITY).executeAction(collect)
			.andFinallyReturningTheseFields(new CcpFieldName("inexistentField"))
		.endThisProcedure(new CcpFieldName("forEachTest"), CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

		assertEquals(List.of(this.first, this.last), keysSeen);
		assertEquals(List.of("SKILL OF A", "SKILL OF C"), recordsSeen);
	}

	@Test
	public void noKeyFoundRunsNothing() {
		List<String> keysSeen = new ArrayList<>();
		List<String> recordsSeen = new ArrayList<>();
		CcpBusiness collect = collectInto(keysSeen, recordsSeen);

		new CcpGetEntityId(key(this.missing))
		.toBeginProcedureAnd()
			.forEachIdPresentInEntity(VisEntitySkillSynonym.ENTITY).executeAction(collect)
			.andFinallyReturningTheseFields(new CcpFieldName("inexistentField"))
		.endThisProcedure(new CcpFieldName("forEachTest"), CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

		assertEquals(0, keysSeen.size());
	}

	/** The contrast that motivated the new step: the same keys in {@code ifThisIdIsPresentInEntity} run the action once. */
	@Test
	public void ifThisIdIsPresentInEntityRunsOnceWithTheFirstRecordFound() {
		save(this.first, "SKILL OF A");
		save(this.last, "SKILL OF C");
		List<String> keysSeen = new ArrayList<>();
		List<String> recordsSeen = new ArrayList<>();
		CcpBusiness collect = collectInto(keysSeen, recordsSeen);

		new CcpGetEntityId(key(this.first), key(this.missing), key(this.last))
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(VisEntitySkillSynonym.ENTITY).executeAction(collect)
			.andFinallyReturningTheseFields(new CcpFieldName("inexistentField"))
		.endThisProcedure(new CcpFieldName("forEachTest"), CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE);

		assertEquals(List.of(this.first), keysSeen);
		assertEquals(List.of("SKILL OF A"), recordsSeen);
	}
}
