package com.ccp.rest.api.spring.servlet.filters;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

class NoopBusiness implements CcpBusiness {
	public final static NoopBusiness INSTANCE = new NoopBusiness();

	private NoopBusiness() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
