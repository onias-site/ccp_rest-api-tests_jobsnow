package com.jn.mensageria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import com.ccp.business.CcpBusiness;
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
import com.jn.entities.JnEntityAsyncTask;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves that {@link JnMensageriaReceiver#executeProcess} runs the task of the topic and records its outcome in
 * {@code jn_async_task}: success, end time, elapsed time and response, or the failure without rethrowing it.
 */
public class ReceiverOutcomeTest {

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

	/** A task that answers its input. */
	public static class EchoTask implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.message, "done");
		}
	}

	/** A task that always fails. */
	public static class FailingTask implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			throw new IllegalStateException("the task failed");
		}
	}

	private CcpJsonRepresentation message(Class<?> task, Object started) {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityAsyncTask.Fields.messageId, UUID.randomUUID().toString())
				.put(JnEntityAsyncTask.Fields.started, started)
				.put(JnEntityAsyncTask.Fields.data, "05102026 00:00:00")
				.put(JnEntityAsyncTask.Fields.topic, task.getName())
				.put(JnEntityAsyncTask.Fields.request, "{}");
		return message;
	}

	private CcpJsonRepresentation run(Class<?> task, Object started) {
		CcpJsonRepresentation message = this.message(task, started);
		JnMensageriaReceiver.INSTANCE.executeProcess(JnEntityAsyncTask.ENTITY, task.getName(), message);
		CcpJsonRepresentation recorded = JnEntityAsyncTask.ENTITY.getOneById(message);
		return recorded;
	}

	@Ignore("finding 55: response is saved as a JSON object in a text field of jn_async_task, so no outcome is recorded")
	@Test
	public void aSuccessfulTaskIsRecordedWithItsResponseAndTimes() {
		long started = System.currentTimeMillis() - 1_000;

		CcpJsonRepresentation recorded = this.run(EchoTask.class, started);

		assertTrue(recorded.getAsBoolean(JnEntityAsyncTask.Fields.success));
		assertTrue(recorded.getAsLongNumber(JnEntityAsyncTask.Fields.finished) >= started);
		assertTrue(recorded.getAsLongNumber(JnEntityAsyncTask.Fields.enlapsedTime) >= 1_000);
		assertEquals("done", recorded.getInnerJson(JnJsonCommonsFields.response).getAsString(JnJsonCommonsFields.message));
	}

	@Ignore("finding 55: response is saved as a JSON object in a text field of jn_async_task, so no outcome is recorded")
	@Test
	public void aFailedTaskIsRecordedAndNotRethrown() {
		CcpJsonRepresentation recorded = this.run(FailingTask.class, System.currentTimeMillis());

		assertFalse(recorded.getAsBoolean(JnEntityAsyncTask.Fields.success));
		assertTrue(recorded.toString(), recorded.toString().contains("the task failed"));
	}

	@Test
	public void recordingTheOutcomeCurrentlyFailsAndTheErrorEscapes() {
		try {
			this.run(EchoTask.class, System.currentTimeMillis());
			org.junit.Assert.fail("the outcome was recorded");
		} catch (RuntimeException e) {
			// finding 55: Elasticsearch refuses the object in the text field response, and then the error itself
			// is longer than the 500 characters allowed in response
		}
	}

	@Ignore("finding 39: started read back from a JSON message is a double, and saveResult casts it to Long")
	@Test
	public void aStartTimeThatCameThroughJsonIsAccepted() {
		CcpJsonRepresentation recorded = this.run(EchoTask.class, (double) System.currentTimeMillis());

		assertTrue(recorded.getAsBoolean(JnEntityAsyncTask.Fields.success));
	}
}
