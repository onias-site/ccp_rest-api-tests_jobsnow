package com.ccp.business;

import com.ccp.decorators.CcpJsonRepresentation;

class ReturnsNullBusiness implements CcpBusiness {
	public final static ReturnsNullBusiness INSTANCE = new ReturnsNullBusiness();

	private ReturnsNullBusiness() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return null;
	}
}
