package com.ccp.aspects;

import java.util.List;

import com.ccp.aop.CcpAllowNullParameter;
import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;

class InterceptedTargets {

	String returnsNull() {
		return null;
	}

	List<String> returnsNullList() {
		return null;
	}

	@CcpAllowNullReturn
	String returnsAllowedNull() {
		return null;
	}

	String receivesParameter(String value) {
		return value;
	}

	@CcpAllowNullParameter
	String receivesNullableParameter(String value) {
		return String.valueOf(value);
	}

	void voidMethodWithImplicitReturn() {
	}

	CcpJsonRepresentation returnsJson() {
		return CcpOtherConstants.EMPTY_JSON;
	}
}
