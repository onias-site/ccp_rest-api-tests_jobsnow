package com.ccp.business;

import com.ccp.decorators.CcpJsonRepresentation;

class ReturnsNullBusiness implements CcpBusiness {
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return null;
	}
}
