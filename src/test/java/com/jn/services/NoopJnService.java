package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;

enum NoopJnService implements JnService, CcpJsonFieldName {
	INSTANCE;

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
