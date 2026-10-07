package com.jn.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnServiceAsyncTaskTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void valuesTest() {
		assertNotNull(JnServiceAsyncTask.values());
	}

	@Test
	public void valueOfTest() {
		assertNotNull(JnServiceAsyncTask.valueOf("GetAsyncTaskStatusById"));
	}

	/** Finding 41: until 2026-10-07 the service was a stub that answered the request itself. */
	@Test(expected = com.ccp.json.validations.global.engine.CcpJsonValidationError.class)
	public void getAsyncTaskStatusByIdRequiresTheIdTest() {
		JnServiceAsyncTask.GetAsyncTaskStatusById.execute(CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnServiceAsyncTask.GetAsyncTaskStatusById.execute((CcpJsonRepresentation) null);
	}

	// ── JsonFieldNames inner enum ────────────────────────────────────────────

	@Test
	public void jsonFieldNamesValuesTest() {
		assertNotNull(JnServiceAsyncTask.JsonFieldNames.values());
	}

	@Test
	public void jsonFieldNamesValueOfTest() {
		assertNotNull(JnServiceAsyncTask.JsonFieldNames.valueOf("asyncTaskId"));
	}
}
