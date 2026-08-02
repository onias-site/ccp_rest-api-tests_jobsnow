package com.jn.mensageria;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnFunctionMensageriaSenderTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(),
				com.ccp.local.testings.implementations.CcpLocalInstances.mensageriaSender);
	}

	static class NoopBusiness implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	@Test
	public void construtorTest() {
		assertNotNull(new JnFunctionMensageriaSender(new NoopBusiness()));
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorNullTest() {
		new JnFunctionMensageriaSender(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyJsonNullTest() {
		new JnFunctionMensageriaSender(new NoopBusiness()).apply((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyMapNullTest() {
		new JnFunctionMensageriaSender(new NoopBusiness()).apply((java.util.Map<String, Object>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaListNullTest() {
		new JnFunctionMensageriaSender(new NoopBusiness()).sendToMensageria((java.util.List<CcpJsonRepresentation>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaArrayNullTest() {
		new JnFunctionMensageriaSender(new NoopBusiness()).sendToMensageria((CcpJsonRepresentation[]) null);
	}
}
