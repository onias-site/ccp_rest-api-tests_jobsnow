package com.vis.entities;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.mensageria.JnMensageriaReceiver;

/**
 * A consumer thread of a queue that takes its message at the same moment as the other threads of the barrier, as two
 * consumers of the queue of an entity do when the same write is delivered twice at once.
 */
public class ConsumerThreadTogether implements Callable<CcpJsonRepresentation> {

	private final CyclicBarrier together;

	private final String topic;

	private final CcpJsonRepresentation message;

	public ConsumerThreadTogether(CyclicBarrier together, String topic, CcpJsonRepresentation message) {
		this.together = together;
		this.topic = topic;
		this.message = message;
	}

	public CcpJsonRepresentation call() throws Exception {
		this.together.await();
		CcpJsonRepresentation response = JnMensageriaReceiver.INSTANCE.executeProcess(this.topic, this.message);
		return response;
	}
}
