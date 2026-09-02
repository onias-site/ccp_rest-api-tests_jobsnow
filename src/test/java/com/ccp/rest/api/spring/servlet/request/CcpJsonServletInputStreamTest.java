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
			throw new CcpErrorServletInputStreamNotCreated(json, e);
		}
	}

	/**
	 * Exceção lançada quando o {@code CcpJsonServletInputStream} usado no teste não pôde ser criado ou fechado.
	 */
	@SuppressWarnings("serial")
	public static class CcpErrorServletInputStreamNotCreated extends RuntimeException {
		/**
		 * Monta a mensagem com o json de origem e encadeia a exceção original como causa.
		 * @param json o json que alimentaria o stream
		 * @param cause a exceção original
		 */
		private CcpErrorServletInputStreamNotCreated(CcpJsonRepresentation json, Throwable cause) {
			super("It was not possible to create the servlet input stream from the json: " + json, cause);
		}
	}

}
