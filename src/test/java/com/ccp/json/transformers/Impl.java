package com.ccp.json.transformers;

import com.ccp.decorators.CcpJsonRepresentation;

class Impl implements CcpTransformers {
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
