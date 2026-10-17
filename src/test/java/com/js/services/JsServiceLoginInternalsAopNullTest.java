package com.js.services;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Coverage of {@code CcpNullParameterAspect} over the protected method
 * {@code JsServiceLogin.createParametersToSearchInAllEntities}.
 */
public class JsServiceLoginInternalsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void createParametersToSearchInAllEntitiesNullTest() {
		JsServiceLogin.ExecuteLogin.createParametersToSearchInAllEntities(null);
	}
}
