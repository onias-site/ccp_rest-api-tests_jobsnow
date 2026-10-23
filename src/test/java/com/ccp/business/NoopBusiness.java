package com.ccp.business;

import com.ccp.decorators.CcpJsonRepresentation;

class NoopBusiness implements CcpBusiness {
	public final static NoopBusiness INSTANCE = new NoopBusiness();

	private NoopBusiness() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
