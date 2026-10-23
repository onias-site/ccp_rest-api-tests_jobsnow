package com.jb.business.bots.engine;

import com.ccp.decorators.CcpJsonRepresentation;

final class BotBusinessForTest implements JbBotBusiness {
	public final static BotBusinessForTest INSTANCE = new BotBusinessForTest();

	private BotBusinessForTest() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
