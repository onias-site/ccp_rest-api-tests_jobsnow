package com.ccp.aspects;

import java.util.List;

import com.ccp.aop.CcpAllowNullParameter;
import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;

class InterceptedTargets {

	String retornaNull() {
		return null;
	}

	List<String> retornaListaNula() {
		return null;
	}

	@CcpAllowNullReturn
	String retornaNullPermitido() {
		return null;
	}

	String recebeParametro(String value) {
		return value;
	}

	@CcpAllowNullParameter
	String recebeParametroNulavel(String value) {
		return String.valueOf(value);
	}

	void metodoVoidComRetornoImplicito() {
	}

	CcpJsonRepresentation retornaJson() {
		return CcpOtherConstants.EMPTY_JSON;
	}
}
