package com.ccp.implementations.mensageria.sender.gcp.pubsub;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os métodos {@code send1} / {@code send2} da
 * implementação Pub/Sub, que não fazem parte do contrato {@code CcpMensageriaSender}. Nenhuma
 * conexão com o GCP é necessária: o aspecto dispara antes do corpo do método.
 */
public class GcpPubSubMensageriaSenderInternalsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGcpPubSubMensageriaSender());
	}

	private enum TopicoParaTeste {
		topico
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
		sender().send1(TopicoParaTeste.topico, (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void send2TopicNameNullTest() {
		sender().send2(null, "msg");
	}

	@Test(expected = CcpNullParameterException.class)
	public void send2MsgsNullTest() {
		sender().send2(TopicoParaTeste.topico, (String[]) null);
	}
}
