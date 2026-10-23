package com.ccp.rest.api.spring.servlet.filters;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import org.junit.Test;

public class CcpPutSessionValuesAndExecuteTaskFilterTest {



	@Test
	public void constructorTest() {
		assertNotNull(new CcpPutSessionValuesAndExecuteTaskFilter(NoopBusiness.INSTANCE));
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorTaskNullTest() {
		new CcpPutSessionValuesAndExecuteTaskFilter(null);
	}

	@Test
	public void taskLessInstanceTest() {
		assertNotNull(CcpPutSessionValuesAndExecuteTaskFilter.TASKLESS);
	}

	@Test(expected = CcpNullParameterException.class)
	public void doFilterReqNullTest() {
		new CcpPutSessionValuesAndExecuteTaskFilter(NoopBusiness.INSTANCE).doFilter(null, null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void initNullTest() throws Exception {
		new CcpPutSessionValuesAndExecuteTaskFilter(NoopBusiness.INSTANCE).init(null);
	}

	@Test
	public void destroyTest() {
		new CcpPutSessionValuesAndExecuteTaskFilter(NoopBusiness.INSTANCE).destroy();
	}
}
