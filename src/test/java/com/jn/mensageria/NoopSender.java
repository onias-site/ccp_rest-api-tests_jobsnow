package com.jn.mensageria;

import com.ccp.decorators.CcpJsonRepresentation;

class NoopSender implements JnBusinessSendToMensageria {
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
