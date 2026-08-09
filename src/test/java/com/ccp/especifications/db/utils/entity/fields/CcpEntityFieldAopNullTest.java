package com.ccp.especifications.db.utils.entity.fields;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre o construtor de {@code CcpEntityField}.
 */
public class CcpEntityFieldAopNullTest {

	@Test(expected = CcpNullParameterException.class)
	public void construtorNameNullTest() {
		new CcpEntityField(null, false, true, CcpOtherConstants.DO_NOTHING);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorTransformerNullTest() {
		new CcpEntityField("campo", false, true, null);
	}
}
