package com.jn.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnServiceContactUsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void valuesTest() {
		assertEquals(3, JnServiceContactUs.values().length);
	}

	@Test
	public void valueOfSaveContactUsTest() {
		assertNotNull(JnServiceContactUs.valueOf("SaveContactUs"));
	}

	@Test
	public void valueOfListContactUsByUserTest() {
		assertNotNull(JnServiceContactUs.valueOf("ListContactUsByUser"));
	}

	@Test
	public void valueOfGetContactUsKpisTest() {
		assertNotNull(JnServiceContactUs.valueOf("GetContactUsKpis"));
	}

	// ── apply returns same json (implementações são pass-through) ────────────

	@Test
	public void saveContactUsApplyTest() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON;
		assertSame(input, JnServiceContactUs.SaveContactUs.apply(input));
	}

	@Test
	public void listContactUsByUserApplyTest() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON;
		assertSame(input, JnServiceContactUs.ListContactUsByUser.apply(input));
	}

	@Test
	public void getContactUsKpisApplyTest() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON;
		assertSame(input, JnServiceContactUs.GetContactUsKpis.apply(input));
	}

	// ── null-parameter tests ─────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void saveContactUsApplyNullTest() {
		JnServiceContactUs.SaveContactUs.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void listContactUsByUserApplyNullTest() {
		JnServiceContactUs.ListContactUsByUser.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getContactUsKpisApplyNullTest() {
		JnServiceContactUs.GetContactUsKpis.apply(null);
	}
}
