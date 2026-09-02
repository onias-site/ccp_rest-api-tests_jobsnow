package com.jn.business.http;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpApiExecutor;

class NoopExecutor implements CcpHttpApiExecutor {
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { return json; }
}
