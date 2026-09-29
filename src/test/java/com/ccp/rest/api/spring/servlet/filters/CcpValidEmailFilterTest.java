package com.ccp.rest.api.spring.servlet.filters;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;

public class CcpValidEmailFilterTest {

	@Test
	public void constructorTest() {
		CcpValidEmailFilter filter = new CcpValidEmailFilter("/login/");
		assertNotNull(filter);
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorFilteredNullTest() {
		new CcpValidEmailFilter((String[]) null);
	} 

	@Test
	public void getEmailSyntaxFilterTest() {
		CcpValidEmailFilter filter = CcpValidEmailFilter.getEmailSyntaxFilter("/login/");
		assertNotNull(filter);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getEmailSyntaxFilterNullTest() {
		CcpValidEmailFilter.getEmailSyntaxFilter((String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void doFilterReqNullTest() {
		new CcpValidEmailFilter("/login/").doFilter(null, null, null);
	}

	@Test
	public void toStringTest() {
		assertNotNull(new CcpValidEmailFilter("/login/").toString());
	}

	@Test(expected = CcpNullParameterException.class)
	public void initNullTest() throws Exception {
		new CcpValidEmailFilter("/login/").init(null);
	}

	@Test
	public void destroyTest() {
		new CcpValidEmailFilter("/login/").destroy();
	}
}
