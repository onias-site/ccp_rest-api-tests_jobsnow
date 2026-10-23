package com.js.db.bulk.handlers;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JsBulkHandlerRegisterLoginTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JsBulkHandlerRegisterLogin.INSTANCE);
	}

	@Test
	public void getEntityToSearchTest() {
		assertNotNull(JsBulkHandlerRegisterLogin.INSTANCE.getEntityToSearch());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasFoundInTheEntitySearchJsonNullTest() {
		JsBulkHandlerRegisterLogin.INSTANCE.whenRecordWasFoundInTheEntitySearch(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasFoundInTheEntitySearchRecordNullTest() {
		JsBulkHandlerRegisterLogin.INSTANCE.whenRecordWasFoundInTheEntitySearch(CcpOtherConstants.EMPTY_JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasNotFoundInTheEntitySearchNullTest() {
		JsBulkHandlerRegisterLogin.INSTANCE.whenRecordWasNotFoundInTheEntitySearch(null);
	}
}
 