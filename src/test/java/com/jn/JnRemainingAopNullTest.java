package com.jn;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.services.JnService;
import com.jn.services.JnServiceLogin;

/** Coverage of {@code CcpNullParameterAspect} over the default method {@code JnService.execute}. */
public class JnRemainingAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void jnServiceExecuteNullTest() {
		JnService service = JnServiceLogin.ExecuteLogin;
		service.execute((CcpJsonRepresentation) null);
	}
}
