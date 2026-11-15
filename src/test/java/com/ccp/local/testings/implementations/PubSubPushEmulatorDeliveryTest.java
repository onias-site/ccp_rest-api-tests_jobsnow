package com.ccp.local.testings.implementations;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * The body {@code PubSubPushEmulatorDelivery} sends to the local push consumer: the envelope of a Pub/Sub push, with
 * the message in Base64 under {@code message.data}, decoded as UTF-8 by the consumer without losing the accents.
 */
public class PubSubPushEmulatorDeliveryTest {

	@Before
	public void loadTheJsonHandler() {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void theMessageGoesInBase64InsideTheEnvelopeAndKeepsItsAccents() {
		String message = "{\"text\":\"Atenção: currículo visualizado às 10h\"}";
		PubSubPushEmulatorDelivery delivery = new PubSubPushEmulatorDelivery("com.vis.entities.VisEntityViewedResume", message, "mark");

		CcpJsonRepresentation pushRequest = delivery.getPushRequest();

		CcpJsonRepresentation envelope = pushRequest.getInnerJson(PubSubPushEmulatorDelivery.Fields.message);
		String data = envelope.getAsString(PubSubPushEmulatorDelivery.Fields.data);
		CcpStringDecorator dataDecorator = new CcpStringDecorator(data);
		CcpTextDecorator dataText = dataDecorator.text();
		CcpTextDecorator decoded = dataText.fromBase64();
		assertEquals(message, decoded.content);
	}
}
