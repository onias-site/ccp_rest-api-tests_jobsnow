package com.ccp.implementations.mensageria.sender.gcp.pubsub;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;

public class CcpGcpPubSubMensageriaSenderTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGcpPubSubMensageriaSender());
	}

	private static CcpMensageriaSender getSender() {
		return CcpDependencyInjection.getDependency(CcpMensageriaSender.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpGcpPubSubMensageriaSender());
	}

	@Test
	public void getInstanceTest() {
		CcpMensageriaSender instance = new CcpGcpPubSubMensageriaSender().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaTopicNullTest() {
		getSender().sendToMensageria(null, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaMsgsNullTest() {
		getSender().sendToMensageria("topic", (String[]) null);
	}
}
