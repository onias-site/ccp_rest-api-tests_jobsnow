package com.ccp.json.validations.global.interfaces;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.enums.CcpJsonValidatorDefaults;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre o método default {@code getErrors} de
 * {@code CcpJsonValidator}.
 */
public class CcpJsonValidatorAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static CcpJsonValidator validator() {
		return CcpJsonValidatorDefaults.requiredAtLeastOne;
	}

	@Test(expected = CcpNullParameterException.class)
	public void getErrorsErrorsNullTest() {
		validator().getErrors(null, JSON, CcpJsonValidatorAopNullTest.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getErrorsJsonNullTest() {
		validator().getErrors(JSON, null, CcpJsonValidatorAopNullTest.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getErrorsClassNullTest() {
		validator().getErrors(JSON, JSON, null);
	}
}
