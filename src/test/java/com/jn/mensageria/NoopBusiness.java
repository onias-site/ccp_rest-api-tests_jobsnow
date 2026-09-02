package com.jn.mensageria;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

class NoopBusiness implements CcpBusiness {
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
