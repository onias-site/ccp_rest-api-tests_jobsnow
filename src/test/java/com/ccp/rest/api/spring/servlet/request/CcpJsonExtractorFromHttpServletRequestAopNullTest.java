package com.ccp.rest.api.spring.servlet.request;

import com.ccp.aop.CcpNullParameterException;
import org.junit.Test;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre o método default
 * {@code extractJsonFromHttpServletRequest}.
 *
 * <p>
 * O construtor de {@code CcpPutSessionValuesRequestWrapper} não é testável aqui: ele exige um
 * {@code HttpServletRequest} real (mock de container servlet) e, no caso do próprio {@code request}
 * nulo, quem rejeita é {@code jakarta.servlet.http.HttpServletRequestWrapper} — classe fora de
 * {@code com.ccp..}, portanto fora do alcance do aspecto.
 * </p>
 */
public class CcpJsonExtractorFromHttpServletRequestAopNullTest {



	@Test(expected = CcpNullParameterException.class)
	public void extractJsonFromHttpServletRequestNullTest() throws Exception {
		new ExtractorForTest().extractJsonFromHttpServletRequest(null);
	}
}
