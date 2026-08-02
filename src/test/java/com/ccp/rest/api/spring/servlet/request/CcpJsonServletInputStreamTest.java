package com.ccp.rest.api.spring.servlet.request;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpJsonServletInputStreamTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void construtorTest() {
		CcpJsonServletInputStream is = new CcpJsonServletInputStream(CcpOtherConstants.EMPTY_JSON);
		assertNotNull(is);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorNullTest() {
		this.get(null);
	}

	@Test
	public void isReadyTest() {
		assertTrue(this.get(CcpOtherConstants.EMPTY_JSON).isReady());
	}

	@Test
	public void isFinishedTest() {
		this.get(CcpOtherConstants.EMPTY_JSON).isFinished();
	}

	@Test(expected = CcpNullParameterException.class)
	public void setReadListenerNullTest() {
		this.get(CcpOtherConstants.EMPTY_JSON).setReadListener(null);
	}

	protected CcpJsonServletInputStream get(CcpJsonRepresentation json) {
		try(CcpJsonServletInputStream is = new CcpJsonServletInputStream(json);) {
			
			return is;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
