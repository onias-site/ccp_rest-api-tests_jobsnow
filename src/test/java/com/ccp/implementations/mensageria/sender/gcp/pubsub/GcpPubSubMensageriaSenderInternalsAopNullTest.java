package com.ccp.implementations.mensageria.sender.gcp.pubsub;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import org.junit.Test;

/**
 * Coverage of {@code CcpNullParameterAspect} over the {@code send1} / {@code send2} methods of the
 * Pub/Sub implementation, which are not part of the {@code CcpMensageriaSender} contract. No
 * connection to GCP is needed: the aspect fires before the method body.
 */
public class GcpPubSubMensageriaSenderInternalsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGcpPubSubMensageriaSender());
	}



	private static GcpPubSubMensageriaSender sender() {
		CcpMensageriaSender dependency = CcpDependencyInjection.getDependency(CcpMensageriaSender.class);
		return (GcpPubSubMensageriaSender) dependency;
	}

	@Test(expected = CcpNullParameterException.class)
	public void send1TopicNameNullTest() {
		sender().send1(null, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void send1MsgsNullTest() {
		sender().send1(TestTopic.topic, (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void send2TopicNameNullTest() {
		sender().send2(null, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void send2MsgsNullTest() {
		sender().send2(TestTopic.topic, (String[]) null);
	}
}
