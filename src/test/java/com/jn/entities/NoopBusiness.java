package com.jn.entities;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

public class NoopBusiness implements CcpBusiness {
	public final static NoopBusiness INSTANCE = new NoopBusiness();

	private NoopBusiness() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
