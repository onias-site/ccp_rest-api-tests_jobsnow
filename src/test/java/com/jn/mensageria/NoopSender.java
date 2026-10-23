package com.jn.mensageria;

import com.ccp.decorators.CcpJsonRepresentation;

class NoopSender implements JnBusinessSendToMensageria {
	public final static NoopSender INSTANCE = new NoopSender();

	private NoopSender() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
