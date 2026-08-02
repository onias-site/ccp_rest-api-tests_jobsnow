package com.jn.db.bulk.handlers;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBulkHandlerRegisterLoginTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBulkHandlerRegisterLogin.INSTANCE);
	}

	@Test
	public void getEntityToSearchTest() {
		assertNotNull(JnBulkHandlerRegisterLogin.INSTANCE.getEntityToSearch());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasFoundInTheEntitySearchJsonNullTest() {
		JnBulkHandlerRegisterLogin.INSTANCE.whenRecordWasFoundInTheEntitySearch(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasFoundInTheEntitySearchRecordNullTest() {
		JnBulkHandlerRegisterLogin.INSTANCE.whenRecordWasFoundInTheEntitySearch(CcpOtherConstants.EMPTY_JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasNotFoundInTheEntitySearchNullTest() {
		JnBulkHandlerRegisterLogin.INSTANCE.whenRecordWasNotFoundInTheEntitySearch(null);
	}
}
 