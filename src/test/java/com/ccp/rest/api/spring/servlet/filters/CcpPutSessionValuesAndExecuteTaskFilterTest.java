package com.ccp.rest.api.spring.servlet.filters;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

public class CcpPutSessionValuesAndExecuteTaskFilterTest {

	static class NoopBusiness implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	@Test
	public void construtorTest() {
		assertNotNull(new CcpPutSessionValuesAndExecuteTaskFilter(new NoopBusiness()));
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorTaskNullTest() {
		new CcpPutSessionValuesAndExecuteTaskFilter(null);
	}

	@Test
	public void taskLessInstanceTest() {
		assertNotNull(CcpPutSessionValuesAndExecuteTaskFilter.TASKLESS);
	}

	@Test(expected = CcpNullParameterException.class)
	public void doFilterReqNullTest() {
		new CcpPutSessionValuesAndExecuteTaskFilter(new NoopBusiness()).doFilter(null, null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void initNullTest() throws Exception {
		new CcpPutSessionValuesAndExecuteTaskFilter(new NoopBusiness()).init(null);
	}

	@Test
	public void destroyTest() {
		new CcpPutSessionValuesAndExecuteTaskFilter(new NoopBusiness()).destroy();
	}
}
