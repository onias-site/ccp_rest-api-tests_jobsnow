package com.jn.db.bulk.handlers;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnBulkHandlerSolveLoginConflictTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBulkHandlerSolveLoginConflict.INSTANCE);
	}

	@Test
	public void getEntityToSearchTest() {
		assertNotNull(JnBulkHandlerSolveLoginConflict.INSTANCE.getEntityToSearch());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasFoundInTheEntitySearchJsonNullTest() {
		JnBulkHandlerSolveLoginConflict.INSTANCE.whenRecordWasFoundInTheEntitySearch(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasFoundInTheEntitySearchRecordNullTest() {
		JnBulkHandlerSolveLoginConflict.INSTANCE.whenRecordWasFoundInTheEntitySearch(CcpOtherConstants.EMPTY_JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void whenRecordWasNotFoundInTheEntitySearchNullTest() {
		JnBulkHandlerSolveLoginConflict.INSTANCE.whenRecordWasNotFoundInTheEntitySearch(null);
	}
}
 