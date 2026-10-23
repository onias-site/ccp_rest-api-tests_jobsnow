package com.jn.mensageria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves that {@link JnMensageriaReceiver#executeProcess} runs the task of the topic and answers its response, and that
 * a failure of the task is rethrown to the consumer (so the messaging can redeliver the message).
 */
public class ReceiverOutcomeTest {

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

	/** A task that answers its input. */
	public static class EchoTask implements CcpBusiness {
		/** The single instance: the class keeps no state. */
		public final static EchoTask INSTANCE = new EchoTask();

		private EchoTask() {}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.message, "done");
		}
	}

	/** A task that always fails. */
	public static class FailingTask implements CcpBusiness {
		/** The single instance: the class keeps no state. */
		public final static FailingTask INSTANCE = new FailingTask();

		private FailingTask() {}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			throw new IllegalStateException("the task failed");
		}
	}

	private CcpJsonRepresentation message(Class<?> task) {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON.put(CcpJsonCommonsFields.topic, task.getName());
		return message;
	}

	@Test
	public void aSuccessfulTaskAnswersItsResponse() {
		CcpJsonRepresentation message = this.message(EchoTask.class);

		CcpJsonRepresentation response = JnMensageriaReceiver.INSTANCE.executeProcess(EchoTask.class.getName(), message);

		assertEquals("done", response.getAsString(JnJsonCommonsFields.message));
	}

	@Test
	public void aFailedTaskIsRethrown() {
		CcpJsonRepresentation message = this.message(FailingTask.class);
		try {
			JnMensageriaReceiver.INSTANCE.executeProcess(FailingTask.class.getName(), message);
			fail("the failure of the task must reach the consumer");
		} catch (IllegalStateException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("the task failed"));
		}
	}
}
