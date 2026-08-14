package com.jn.business.http;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBusinessSendHttpRequestTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static class NoopExecutor implements CcpHttpApiExecutor {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) { return json; }
	}

	@Test
	public void construtorTest() {
		assertNotNull(new JnBusinessSendHttpRequest(new NoopExecutor()));
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorProcessNullTest() {
		new JnBusinessSendHttpRequest(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		new JnBusinessSendHttpRequest(new NoopExecutor()).execute(null);
	}
}
