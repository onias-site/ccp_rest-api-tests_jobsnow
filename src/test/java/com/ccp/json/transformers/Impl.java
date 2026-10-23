package com.ccp.json.transformers;

import com.ccp.decorators.CcpJsonRepresentation;

class Impl implements CcpTransformers {
	public final static Impl INSTANCE = new Impl();

	private Impl() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
