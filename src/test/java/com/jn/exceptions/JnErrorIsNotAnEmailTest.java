package com.jn.exceptions;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;

public class JnErrorIsNotAnEmailTest {

	@Test
	public void ctorTest() {
		assertNotNull(new JnErrorIsNotAnEmail("x", CcpOtherConstants.EMPTY_JSON));
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorContentNullTest() {
		new JnErrorIsNotAnEmail(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorJsonNullTest() {
		new JnErrorIsNotAnEmail("x", null); 
	}
}
