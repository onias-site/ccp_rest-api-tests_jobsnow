package com.jn.business.http;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpApiExecutor;

class NoopExecutor implements CcpHttpApiExecutor {
	public final static NoopExecutor INSTANCE = new NoopExecutor();

	private NoopExecutor() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { return json; }
}
