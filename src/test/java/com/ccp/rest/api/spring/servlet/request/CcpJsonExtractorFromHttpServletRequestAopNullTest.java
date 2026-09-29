package com.ccp.rest.api.spring.servlet.request;

import com.ccp.aop.CcpNullParameterException;
import org.junit.Test;

/**
 * Coverage of {@code CcpNullParameterAspect} over the default method
 * {@code extractJsonFromHttpServletRequest}.
 *
 * <p>
 * The constructor of {@code CcpPutSessionValuesRequestWrapper} is not testable here: it requires a
 * real {@code HttpServletRequest} (a servlet container mock) and, in the case of the {@code request}
 * itself being null, the one that rejects it is {@code jakarta.servlet.http.HttpServletRequestWrapper} — a class outside
 * {@code com.ccp..}, therefore out of the aspect's reach.
 * </p>
 */
public class CcpJsonExtractorFromHttpServletRequestAopNullTest {



	@Test(expected = CcpNullParameterException.class)
	public void extractJsonFromHttpServletRequestNullTest() throws Exception {
		new ExtractorForTest().extractJsonFromHttpServletRequest(null);
	}
}
