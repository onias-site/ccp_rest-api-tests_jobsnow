package com.ccp.service;

import com.ccp.decorators.CcpJsonRepresentation;

class CcpServiceTestStubService implements CcpService {
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
	public Class<?> getJsonValidationClass() {
		return CcpServiceTestStubService.class;
	}
	public String name() {
		return "stub";
	}
}
