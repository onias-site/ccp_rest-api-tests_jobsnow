package com.ccp.especifications.db.utils.entity.decorators.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityAsyncTask;

/**
 * Proves {@link CcpEntityMetaData} on {@code jn_async_task}: the bulk items of each operation carry the id of the
 * record, the updatable fields are kept and the unknown ones dropped, and several records are read by id at once.
 */
public class EntityMetaDataBehaviorTest {

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

	private final CcpEntityMetaData metaData = JnEntityAsyncTask.ENTITY.getEntityMetaData();

	private CcpJsonRepresentation task() {
		CcpJsonRepresentation task = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityAsyncTask.Fields.messageId, UUID.randomUUID().toString())
				.put(JnEntityAsyncTask.Fields.started, System.currentTimeMillis())
				.put(JnEntityAsyncTask.Fields.data, "05102026 00:00:00")
				.put(JnEntityAsyncTask.Fields.topic, "test")
				.put(JnEntityAsyncTask.Fields.request, "{}");
		return task;
	}

	@Test
	public void theNameOfTheMetaDataIsTheEntityName() {
		assertEquals("jn_async_task", this.metaData.name());
		assertEquals("jn_async_task", this.metaData.toString());
	}

	@Test
	public void eachBulkItemCarriesItsOperationAndTheIdOfTheRecord() {
		CcpJsonRepresentation task = this.task();
		String id = JnEntityAsyncTask.ENTITY.calculateId(task);

		CcpBulkItem create = this.metaData.toCreateBulkItem(task);
		CcpBulkItem update = this.metaData.toUpdateBulkItem(task);
		CcpBulkItem delete = this.metaData.toDeleteBulkItem(task);

		assertEquals(CcpBulkEntityOperationType.create, create.operation);
		assertEquals(CcpBulkEntityOperationType.update, update.operation);
		assertEquals(CcpBulkEntityOperationType.delete, delete.operation);
		for (CcpBulkItem item : Arrays.asList(create, update, delete)) {
			assertEquals(id, item.id);
			assertEquals(JnEntityAsyncTask.ENTITY, item.entity);
		}
	}

	@Test
	public void onlyTheFieldsOfTheEntityAreKeptAsUpdatable() {
		CcpJsonRepresentation withAnUnknownField = this.task().put(new CcpFieldName("unknown"), 1);

		CcpJsonRepresentation updatable = this.metaData.getOnlyUpdatableFields(withAnUnknownField);

		assertTrue(updatable.containsField(JnEntityAsyncTask.Fields.topic));
		assertFalse(updatable.containsField(new CcpFieldName("unknown")));
	}

	@Test
	public void noIdToSearchGivesAnEmptyJson() {
		assertTrue(this.metaData.getMultipleByIds(new ArrayList<>()).isEmpty());
	}

	@Test
	public void severalRecordsAreReadByIdAtOnce() {
		CcpJsonRepresentation first = this.task();
		CcpJsonRepresentation second = this.task();
		JnEntityAsyncTask.ENTITY.save(first);
		JnEntityAsyncTask.ENTITY.save(second);

		CcpJsonRepresentation byId = this.metaData.getMultipleByIds(Arrays.asList(first, second));

		String firstId = JnEntityAsyncTask.ENTITY.calculateId(first);
		String secondId = JnEntityAsyncTask.ENTITY.calculateId(second);
		assertEquals(first.getAsString(JnEntityAsyncTask.Fields.messageId),
				byId.getInnerJson(new CcpFieldName(firstId)).getAsString(JnEntityAsyncTask.Fields.messageId));
		assertTrue(byId.toString(), byId.containsField(new CcpFieldName(secondId)));
	}
}
