package com.ccp.rest.api.spring.servlet.filters;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import org.junit.Test;

public class CcpPutSessionValuesAndExecuteTaskFilterTest {



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
