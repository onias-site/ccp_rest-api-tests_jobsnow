package com.ccp.local.testings.implementations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.mensageria.JnMensageriaReceiver;
import com.jn.mensageria.ReceiverOutcomeTest.EchoTask;
import com.jn.mensageria.ReceiverOutcomeTest.FailingTask;

/**
 * {@link AsyncMensageriaProcessRun} runs the message with the receiver it names, and removes the running mark of the
 * message ({@link CcpLocalMensageriaRunningMarks}) when the task ends, with or without success.
 */
public class AsyncMensageriaProcessRunTest {

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

	private CcpJsonRepresentation message(Class<?> task) {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(CcpJsonCommonsFields.topic, task.getName())
				.put(CcpMensageriaReceiver.JsonFieldNames.mensageriaReceiver, JnMensageriaReceiver.class.getName());
		return message;
	}

	@Test
	public void theMessageRunsWithTheReceiverItNamesAndItsMarkIsRemoved() {
		long beforeTheMark = System.currentTimeMillis() - 1;
		String runningMarkName = CcpLocalMensageriaRunningMarks.mark();
		assertTrue(CcpLocalMensageriaRunningMarks.isAnyRunningPublishedAfter(beforeTheMark));
		String topic = EchoTask.class.getName();
		CcpJsonRepresentation message = this.message(EchoTask.class);

		new AsyncMensageriaProcessRun(topic, message, runningMarkName).run();

		assertFalse(CcpLocalMensageriaRunningMarks.isAnyRunningPublishedAfter(beforeTheMark));
	}

	@Test
	public void aFailedTaskAlsoRemovesItsMark() {
		long beforeTheMark = System.currentTimeMillis() - 1;
		String runningMarkName = CcpLocalMensageriaRunningMarks.mark();
		String topic = FailingTask.class.getName();
		CcpJsonRepresentation message = this.message(FailingTask.class);
		try {
			new AsyncMensageriaProcessRun(topic, message, runningMarkName).run();
			fail("the failure of the task goes on to the thread");
		} catch (IllegalStateException e) {
			assertFalse(CcpLocalMensageriaRunningMarks.isAnyRunningPublishedAfter(beforeTheMark));
		}
	}
}
