package com.ccp.especifications.db.utils.entity.decorators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Messaging double that runs each message as the synchronous messaging of the tests does, but keeps the topic and the
 * operation of each one. It proves that a write went through the queue.
 */
public class CountingMensageriaSender implements CcpMensageriaSender {
	/** The single instance: the class keeps no state. */
	public final static CountingMensageriaSender INSTANCE = new CountingMensageriaSender();

	private CountingMensageriaSender() {}

	/** The topic and the operation of each message sent, as {@code topic operation}. */
	private static final List<String> sentMessages = Collections.synchronizedList(new ArrayList<>());

	public CcpMensageriaSender sendToMensageria(String topic, String... msgs) {
		for (String msg : msgs) {
			CcpJsonRepresentation json = new CcpJsonRepresentation(msg);
			String operation = json.getAsString(JnJsonCommonsFields.operation);
			sentMessages.add(topic + " " + operation);
		}
		CcpMensageriaSender syncMensageria = (CcpMensageriaSender) CcpLocalInstances.syncMensageriaListener.getInstance();
		syncMensageria.sendToMensageria(topic, msgs);
		return this;
	}

	/**
	 * How many messages of the topic with the operation have already been sent.
	 * @param topic the topic
	 * @param operation the operation
	 * @return how many
	 */
	public static long howMany(String topic, String operation) {
		String sentMessage = topic + " " + operation;
		synchronized (sentMessages) {
			long howMany = sentMessages.stream().filter(x -> sentMessage.equals(x)).count();
			return howMany;
		}
	}
}
