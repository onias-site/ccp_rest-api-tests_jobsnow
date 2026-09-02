package com.ccp.business;

import com.ccp.decorators.CcpJsonRepresentation;

class NoopBusiness implements CcpBusiness {
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
