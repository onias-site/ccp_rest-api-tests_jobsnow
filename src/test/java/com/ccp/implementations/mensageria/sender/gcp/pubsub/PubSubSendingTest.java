package com.ccp.implementations.mensageria.sender.gcp.pubsub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.pubsub.v1.PubsubMessage;

/**
 * Proves how {@link GcpPubSubMensageriaSender#sendToMensageria(String, String...)} sends, with a publishing that does not
 * reach GCP: the sender keeps working after the first sending (until 2026-10-06 it shut its cached publisher down, so the
 * second sending to a topic failed), and a message refused by Pub/Sub reaches the caller instead of being lost in silence.
 */
public class PubSubSendingTest {

	/** Records what was published, refusing the messages that contain {@code refuse}. */
	static class FakePublishing implements GcpPubSubPublishing {
		final List<String> topics = new ArrayList<>();
		final List<String> messages = new ArrayList<>();
		final RuntimeException refusal = new IllegalStateException("refused by Pub/Sub");

		public ApiFuture<String> publish(String topicId, PubsubMessage message) {
			String text = message.getData().toStringUtf8();
			this.topics.add(topicId);
			this.messages.add(text);
			if (text.contains("refuse")) {
				return ApiFutures.immediateFailedFuture(this.refusal);
			}
			return ApiFutures.immediateFuture("id-" + this.messages.size());
		}
	}

	@Test
	public void theSecondSendingToTheSameTopicStillWorks() {
		FakePublishing publishing = new FakePublishing();
		GcpPubSubMensageriaSender sender = new GcpPubSubMensageriaSender(publishing);

		sender.sendToMensageria("topicA", "{\"n\":1}");
		sender.sendToMensageria("topicA", "{\"n\":2}");

		assertEquals(2, publishing.messages.size());
		assertEquals("topicA", publishing.topics.get(1));
	}

	@Test
	public void everyMessageOfOneSendingIsPublishedAsUtf8() {
		FakePublishing publishing = new FakePublishing();
		GcpPubSubMensageriaSender sender = new GcpPubSubMensageriaSender(publishing);

		sender.sendToMensageria("topicB", "{\"text\":\"configuração\"}", "{\"n\":2}");

		assertEquals(2, publishing.messages.size());
		assertEquals("{\"text\":\"configuração\"}", publishing.messages.get(0));
	}

	@Test
	public void aMessageRefusedByPubSubReachesTheCaller() {
		FakePublishing publishing = new FakePublishing();
		GcpPubSubMensageriaSender sender = new GcpPubSubMensageriaSender(publishing);

		try {
			sender.sendToMensageria("topicC", "{\"n\":1}", "{\"refuse\":true}");
			fail("the refused message was lost in silence");
		} catch (GcpPubSubMensageriaSender.CcpErrorGcpPubSubPublish e) {
			assertSame(publishing.refusal, e.getCause());
			assertTrue(e.getMessage(), e.getMessage().contains("topicC"));
		}
		assertEquals("every message was published before the refusal was reported", 2, publishing.messages.size());
	}
}
