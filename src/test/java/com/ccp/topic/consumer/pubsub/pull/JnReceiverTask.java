package com.ccp.topic.consumer.pubsub.pull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.mensageria.JnMensageriaReceiver;

/** Task of {@link PullReceiverTest}: runs the pulled message through the jn receiver and keeps each response. */
class JnReceiverTask implements CcpBusiness {

	/** The topic of the message. */
	private final String topic;

	/** The responses of the receiver, one per message run. */
	final List<CcpJsonRepresentation> responses = Collections.synchronizedList(new ArrayList<>());

	JnReceiverTask(String topic) {
		this.topic = topic;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation response = JnMensageriaReceiver.INSTANCE.executeProcess(this.topic, json);
		this.responses.add(response);
		return json;
	}
}
