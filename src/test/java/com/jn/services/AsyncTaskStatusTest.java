package com.jn.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityAsyncTask;

/**
 * Proves {@link JnServiceAsyncTask#GetAsyncTaskStatusById} against the local Elasticsearch (finding 41): the status of
 * the task is read by id, without the data of the task, and an unknown id is a 404. Until 2026-10-07 the service answered
 * the request itself.
 */
public class AsyncTaskStatusTest {

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

	private final String messageId = UUID.randomUUID().toString();

	private final CcpJsonRepresentation task = CcpOtherConstants.EMPTY_JSON
			.put(JnEntityAsyncTask.Fields.messageId, this.messageId)
			.put(JnEntityAsyncTask.Fields.topic, "com.jn.SomeTask")
			.put(JnEntityAsyncTask.Fields.started, 1000L)
			.put(JnEntityAsyncTask.Fields.finished, 1500L)
			.put(JnEntityAsyncTask.Fields.enlapsedTime, 500L)
			.put(JnEntityAsyncTask.Fields.success, true)
			.put(JnEntityAsyncTask.Fields.data, "07/10/2026 10:00:00")
			.put(JnEntityAsyncTask.Fields.request, "{\"password\":\"secret\"}")
			.put(JnEntityAsyncTask.Fields.response, "{\"token\":\"secret\"}");

	@After
	public void deleteTheTask() {
		JnEntityAsyncTask.ENTITY.delete(this.task);
	}

	private CcpJsonRepresentation statusOf(String asyncTaskId) {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(JnServiceAsyncTask.JsonFieldNames.asyncTaskId, asyncTaskId);
		CcpJsonRepresentation status = JnServiceAsyncTask.GetAsyncTaskStatusById.execute(request);
		return status;
	}

	@Test
	public void theStatusOfAnExistingTaskComesWithoutItsData() {
		JnEntityAsyncTask.ENTITY.save(this.task);

		CcpJsonRepresentation status = this.statusOf(this.messageId);

		assertEquals(this.messageId, status.getAsString(JnEntityAsyncTask.Fields.messageId));
		assertEquals("com.jn.SomeTask", status.getAsString(JnEntityAsyncTask.Fields.topic));
		assertTrue(status.getAsBoolean(JnEntityAsyncTask.Fields.success));
		assertEquals(500L, (long) status.getAsLongNumber(JnEntityAsyncTask.Fields.enlapsedTime));
		assertFalse("the request of the task is never exposed: " + status, status.containsField(JnEntityAsyncTask.Fields.request));
		assertFalse("the response of the task is never exposed: " + status, status.containsField(JnEntityAsyncTask.Fields.response));
	}

	@Test
	public void anUnknownTaskIsNotFound() {
		try {
			this.statusOf(UUID.randomUUID().toString());
			fail("there is no such task");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
	}
}
