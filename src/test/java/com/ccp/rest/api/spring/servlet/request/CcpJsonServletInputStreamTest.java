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
	public void constructorTest() {
		CcpJsonServletInputStream inputStream = new CcpJsonServletInputStream(CcpOtherConstants.EMPTY_JSON);
		assertNotNull(inputStream);
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorNullTest() {
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
		try(CcpJsonServletInputStream inputStream = new CcpJsonServletInputStream(json);) {
			
			return inputStream;
			
		} catch (Exception e) {
			throw new CcpErrorServletInputStreamNotCreated(json, e);
		}
	}

	/**
	 * Exception thrown when the {@code CcpJsonServletInputStream} used in the test could not be created or closed.
	 */
	@SuppressWarnings("serial")
	public static class CcpErrorServletInputStreamNotCreated extends RuntimeException {
		/**
		 * Builds the message with the source json and chains the original exception as the cause.
		 * @param json the json that would feed the stream
		 * @param cause the original exception
		 */
		private CcpErrorServletInputStreamNotCreated(CcpJsonRepresentation json, Throwable cause) {
			super("It was not possible to create the servlet input stream from the json: " + json, cause);
		}
	}

}
