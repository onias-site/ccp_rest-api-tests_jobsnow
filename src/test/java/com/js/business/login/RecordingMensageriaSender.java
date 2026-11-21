package com.js.business.login;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Sender that only records the topic and the operation of each message, without running it: proves what an API sends
 * to the queues when the consumer is somewhere else.
 */
public class RecordingMensageriaSender implements CcpMensageriaSender {

	/** Where each message is recorded as {@code "<topic> <operation>"}. */
	private final List<String> sentMessages;

	/**
	 * Keeps the list that receives the messages.
	 * @param sentMessages where each message is recorded
	 */
	public RecordingMensageriaSender(List<String> sentMessages) {
		this.sentMessages = sentMessages;
	}

	/**
	 * Records each message.
	 * @param topic the topic
	 * @param msgs the messages
	 * @return this sender
	 */
	public CcpMensageriaSender sendToMensageria(String topic, String... msgs) {
		for (String msg : msgs) {
			CcpJsonRepresentation json = new CcpJsonRepresentation(msg);
			String operation = json.getAsString(JnJsonCommonsFields.operation);
			String sentMessage = topic + " " + operation;
			this.sentMessages.add(sentMessage);
		}
		return this;
	}
}
