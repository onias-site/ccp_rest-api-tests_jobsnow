package com.jb.instant.messenger.reader;

import java.util.ArrayList;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.messages.JnMessageType;

/**
 * Stand-in bot for the message reading tests. {@code readNewMessages} finds out which bot is being read through
 * the {@code name()} of the received {@code CcpBusiness}, which is why this double returns the name of the given
 * {@link JnBotType} instead of the class name. Each message delivered by the reader is kept in
 * {@link #received}, so what was read in each call can be checked.
 */
class FakeBot implements CcpBusiness {

	final List<CcpJsonRepresentation> received = new ArrayList<>();

	private final String botName;

	FakeBot(JnMessageType.JnBotType botType) {
		this.botName = botType.name();
	}

	public String name() {
		return this.botName;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.received.add(json);
		return json;
	}
}
