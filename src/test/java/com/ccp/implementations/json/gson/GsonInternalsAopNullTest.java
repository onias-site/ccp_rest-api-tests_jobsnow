package com.ccp.implementations.json.gson;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.json.CcpJsonHandler;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre as classes internas do módulo
 * {@code ccp_json_gson}.
 */
public class GsonInternalsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static GsonJsonHandler handler() {
		CcpJsonHandler dependency = CcpDependencyInjection.getDependency(CcpJsonHandler.class);
		return (GsonJsonHandler) dependency;
	}

	@Test(expected = CcpNullParameterException.class)
	public void isValidTypeSrcNullTest() {
		handler().isValidType(null, java.util.Map.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isValidTypeClassNullTest() {
		handler().isValidType("{}", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldSkipFieldNullTest() {
		JsonRepresentationExclusionStrategy.INSTANCE.shouldSkipField(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldSkipClassNullTest() {
		JsonRepresentationExclusionStrategy.INSTANCE.shouldSkipClass(null);
	}
}
