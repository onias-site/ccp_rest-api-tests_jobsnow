package com.ccp.json.validations.global.engine;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpJsonValidatorEngineTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void validateJsonSemErrosTest() {
		CcpJsonRepresentation r = CcpJsonValidatorEngine.INSTANCE.validateJson(Object.class, CcpOtherConstants.EMPTY_JSON, "f");
		assertNotNull(r);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void validateJsonClazzNullTest() {
		CcpJsonValidatorEngine.INSTANCE.validateJson(null, CcpOtherConstants.EMPTY_JSON, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void validateJsonJsonNullTest() {
		CcpJsonValidatorEngine.INSTANCE.validateJson(Object.class, null, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void validateJsonFeatureNameNullTest() {
		CcpJsonValidatorEngine.INSTANCE.validateJson(Object.class, CcpOtherConstants.EMPTY_JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getJsonFieldTypeNullTest() {
		CcpJsonValidatorEngine.INSTANCE.getJsonFieldType(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getReplacedFieldNullTest() {
		CcpJsonValidatorEngine.INSTANCE.getReplacedField(null);
	}
}
