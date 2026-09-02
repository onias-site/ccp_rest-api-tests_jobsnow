package com.ccp.service;

import com.ccp.decorators.CcpJsonRepresentation;

class CcpCachedServiceTestStubService implements CcpService {
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
	public Class<?> getJsonValidationClass() {
		return CcpCachedServiceTestStubService.class;
	}
	public String name() {
		return "stub";
	}
}
