package com.jn.services;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Coverage of {@code CcpNullParameterAspect} over the protected method
 * {@code JnServiceLogin.createParametersToSearchInAllEntities}.
 */
public class JnServiceLoginInternalsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void createParametersToSearchInAllEntitiesNullTest() {
		JnServiceLogin.ExecuteLogin.createParametersToSearchInAllEntities(null);
	}
}
