package com.vis.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class VisSorterResumesByPositionTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void ctorTest() {
		assertNotNull(new VisSorterResumesByPosition(CcpOtherConstants.EMPTY_JSON));
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorNullTest() {
		new VisSorterResumesByPosition(null); 
	}

	@Test(expected = CcpNullParameterException.class)
	public void compareO1NullTest() {
		new VisSorterResumesByPosition(CcpOtherConstants.EMPTY_JSON).compare(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void compareO2NullTest() {
		new VisSorterResumesByPosition(CcpOtherConstants.EMPTY_JSON).compare(CcpOtherConstants.EMPTY_JSON, null);
	}
}
