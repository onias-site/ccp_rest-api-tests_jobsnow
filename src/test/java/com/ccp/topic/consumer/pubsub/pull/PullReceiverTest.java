package com.ccp.topic.consumer.pubsub.pull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
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
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnMensageriaReceiver;

/**
 * Proves that {@link CcpMessageReceiver} runs the task of the subscription over each pulled message before acknowledging
 * it: until 2026-10-06 the task was commented out and every valid message got {@code ack} without being processed. A
 * failure gets {@code nack} and reaches the error handler once (it used to run twice, the second time over its own result).
 */
public class PullReceiverTest {

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

	private static final String TOPIC = "pullTestTopic";

	/** Records what the receiver answered to Pub/Sub. */
	static class Reply implements AckReplyConsumer {
		final AtomicInteger acks = new AtomicInteger();
		final AtomicInteger nacks = new AtomicInteger();

		public void ack() {
			this.acks.incrementAndGet();
		}

		public void nack() {
			this.nacks.incrementAndGet();
		}
	}

	/** Records the messages it gets, optionally failing. */
	static class RecordingTask implements CcpBusiness {
		final List<CcpJsonRepresentation> received = new ArrayList<>();
		final boolean fails;

		RecordingTask(boolean fails) {
			this.fails = fails;
		}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			this.received.add(json);
			if (this.fails) {
				throw new IllegalStateException("the task failed");
			}
			return json;
		}
	}

	/** A task of the topic that answers its input, run through the real jn receiver. */
	public static class EchoTask implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.message, "done");
		}
	}

	private final RecordingTask notifyError = new RecordingTask(false);

	private static PubsubMessage pubsubMessage(String data) {
		return PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(data)).build();
	}

	@Test
	public void aValidMessageIsProcessedAndThenAcknowledged() {
		RecordingTask task = new RecordingTask(false);
		Reply reply = new Reply();

		new CcpMessageReceiver(this.notifyError, TOPIC, task).receiveMessage(pubsubMessage("{\"name\":\"jobsnow\"}"), reply);

		assertEquals(1, task.received.size());
		assertEquals("jobsnow", task.received.get(0).getAsString(new CcpFieldName("name")));
		assertEquals(1, reply.acks.get());
		assertEquals(0, reply.nacks.get());
		assertTrue(this.notifyError.received.isEmpty());
	}

	@Test
	public void aFailedTaskRejectsTheMessageAndNotifiesOnce() {
		RecordingTask task = new RecordingTask(true);
		Reply reply = new Reply();

		new CcpMessageReceiver(this.notifyError, TOPIC, task).receiveMessage(pubsubMessage("{\"name\":\"jobsnow\"}"), reply);

		assertEquals(0, reply.acks.get());
		assertEquals(1, reply.nacks.get());
		assertEquals(1, this.notifyError.received.size());
		assertTrue(this.notifyError.received.get(0).toString(), this.notifyError.received.get(0).toString().contains(TOPIC));
	}

	@Test
	public void anInvalidJsonIsRejectedWithoutRunningTheTask() {
		RecordingTask task = new RecordingTask(false);
		Reply reply = new Reply();

		new CcpMessageReceiver(this.notifyError, TOPIC, task).receiveMessage(pubsubMessage("{not json"), reply);

		assertTrue(task.received.isEmpty());
		assertEquals(0, reply.acks.get());
		assertEquals(1, reply.nacks.get());
		assertEquals(1, this.notifyError.received.size());
	}

	@Test
	public void aPulledMessageRunsThroughTheJnReceiverAndItsOutcomeIsRecorded() {
		String topic = EchoTask.class.getName();
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityAsyncTask.Fields.messageId, UUID.randomUUID().toString())
				.put(JnEntityAsyncTask.Fields.started, System.currentTimeMillis())
				.put(JnEntityAsyncTask.Fields.data, "06102026 00:00:00")
				.put(JnEntityAsyncTask.Fields.topic, topic)
				.put(JnEntityAsyncTask.Fields.request, "{}");
		CcpBusiness task = json -> {
			JnMensageriaReceiver.INSTANCE.executeProcess(JnEntityAsyncTask.ENTITY, topic, json);
			return json;
		};
		Reply reply = new Reply();

		new CcpMessageReceiver(this.notifyError, topic, task).receiveMessage(pubsubMessage(message.asUgglyJson()), reply);

		assertEquals(this.notifyError.received.toString(), 1, reply.acks.get());
		CcpJsonRepresentation recorded = JnEntityAsyncTask.ENTITY.getOneById(message);
		assertTrue(recorded.getAsBoolean(JnEntityAsyncTask.Fields.success));
		assertTrue(recorded.getAsString(JnJsonCommonsFields.response).contains("done"));
	}
}
