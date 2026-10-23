package com.ccp.implementations.mensageria.sender.gcp.pubsub;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.ProjectTopicName;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * {@link GcpPubSubSdkPublishing} publishes through the cached {@code Publisher} of the topic. The cache receives a
 * publisher with no credentials pointed at a port where nothing listens, so the test needs no GCP: the message is
 * accepted into the batch and a future of its id comes back.
 */
public class GcpPubSubSdkPublishingTest {

	@SuppressWarnings("unchecked")
	@Test
	public void theMessageGoesToThePublisherOfTheTopic() throws Exception {
		String topicId = "sdk-publishing-test";
		ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:1").usePlaintext().build();
		Publisher publisher = Publisher.newBuilder(ProjectTopicName.of("any-project", topicId))
				.setChannelProvider(FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
				.setCredentialsProvider(NoCredentialsProvider.create())
				.build();
		Field publishersField = GcpPubSubMensageriaSender.class.getDeclaredField("publishers");
		publishersField.setAccessible(true);
		Map<String, Publisher> publishers = (Map<String, Publisher>) publishersField.get(null);
		publishers.put(topicId, publisher);
		try {
			GcpPubSubSdkPublishing publishing = new GcpPubSubSdkPublishing(new GcpPubSubMensageriaSender());
			PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8("{}")).build();

			ApiFuture<String> messageId = publishing.publish(topicId, message);

			assertNotNull(messageId);
		} finally {
			publishers.remove(topicId);
			publisher.shutdown();
			channel.shutdownNow();
		}
	}
}
