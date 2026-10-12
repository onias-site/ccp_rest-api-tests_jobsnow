package com.ccp.especifications.db.utils.entity.decorators.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.vis.entities.VisEntitySkillReviewed;

/**
 * Proves that {@code getRecordFromUnionAll} and {@code isPresentInThisUnionAll} of a cacheable entity
 * ({@code @CcpEntityCache}) read the cache first, without caching what they read from the union all (since 2026-10-11;
 * until then the cache decorator skipped the cache on these two methods). The cache is a real map, the database the local
 * Elasticsearch; the entity is {@link VisEntitySkillReviewed} (1-hour cache). The union all runs without cleaning the
 * cache keys, otherwise it would erase the cached record before the reading.
 */
public class CacheFirstOnUnionAllTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.map,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
	}

	private static final CcpEntity ENTITY = VisEntitySkillReviewed.ENTITY;

	private static final Consumer<String[]> KEEP_THE_CACHE = keys -> {};

	/** The other test classes read through the mock cache, which never keeps anything. */
	@AfterClass
	public static void restoreTheMockCache() {
		CcpDependencyInjection.loadAllDependencies(CcpLocalCacheInstances.mock);
	}

	private CcpJsonRepresentation record(String skill) {
		CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillReviewed.Fields.skill, skill)
				.put(VisEntitySkillReviewed.Fields.status, "approved")
				.put(JnJsonCommonsFields.explanation, "Registro do teste de cache primeiro");
		return record;
	}

	/**
	 * A record the cache has is found, and read from there, even when the database no longer has it: proof that the
	 * cache is read before the result of the union all.
	 */
	@Test
	public void aCachedRecordIsReadFromTheCacheFirst() {
		CcpJsonRepresentation record = this.record("TESTE CACHE PRIMEIRO");
		ENTITY.save(record);
		try {
			String id = ENTITY.calculateId(record);
			CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
			String entityName = ENTITY.getEntityMetaData().entityName;
			// only the database loses the record; the cache, filled by the save, keeps it
			crud.delete(entityName, id);

			CcpSelectUnionAll unionAll = crud.unionAll(record, KEEP_THE_CACHE, ENTITY);

			assertTrue(ENTITY.isPresentInThisUnionAll(unionAll, record));
			CcpJsonRepresentation found = ENTITY.getRecordFromUnionAll(unionAll, record.getJsonSupplier());
			assertEquals("Registro do teste de cache primeiro", found.getAsString(JnJsonCommonsFields.explanation));
		} finally {
			ENTITY.delete(record);
		}
	}

	/**
	 * A record that is not cached is read from the union all, which does not cache it: these reads also run in the
	 * middle of writes (twin transfers), and caching there would put back a record being deleted. A record that is in
	 * neither place is not found.
	 */
	@Test
	public void aRecordNotCachedIsReadFromTheUnionAllWithoutCachingIt() {
		CcpJsonRepresentation record = this.record("TESTE CACHE PREENCHIDO");
		ENTITY.save(record);
		try {
			String id = ENTITY.calculateId(record);
			CcpCacheDecorator cache = new CcpCacheDecorator(ENTITY, id);
			cache.delete();
			assertFalse(cache.isPresentInTheCache());

			CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
			CcpSelectUnionAll unionAll = crud.unionAll(record, KEEP_THE_CACHE, ENTITY);
			CcpJsonRepresentation found = ENTITY.getRecordFromUnionAll(unionAll, record.getJsonSupplier());

			assertEquals("Registro do teste de cache primeiro", found.getAsString(JnJsonCommonsFields.explanation));
			assertFalse(cache.isPresentInTheCache());
		} finally {
			ENTITY.delete(record);
		}

		CcpJsonRepresentation missing = this.record("TESTE CACHE INEXISTENTE");
		String missingId = ENTITY.calculateId(missing);
		CcpCacheDecorator missingCache = new CcpCacheDecorator(ENTITY, missingId);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll unionAll = crud.unionAll(missing, KEEP_THE_CACHE, ENTITY);
		assertFalse(ENTITY.isPresentInThisUnionAll(unionAll, missing));
		assertTrue(ENTITY.getRecordFromUnionAll(unionAll, missing.getJsonSupplier()).isEmpty());
		assertFalse(missingCache.isPresentInTheCache());
	}
}
