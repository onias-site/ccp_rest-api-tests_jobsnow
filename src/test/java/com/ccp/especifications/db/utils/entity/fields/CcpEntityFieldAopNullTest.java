package com.ccp.especifications.db.utils.entity.fields;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;

/**
 * Coverage of {@code CcpNullParameterAspect} over the constructor of {@code CcpEntityField}.
 */
public class CcpEntityFieldAopNullTest {

	@Test(expected = CcpNullParameterException.class)
	public void constructorNameNullTest() {
		new CcpEntityField(null, false, true, CcpOtherConstants.DO_NOTHING);
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorTransformerNullTest() {
		new CcpEntityField("field", false, true, null);
	}
}
