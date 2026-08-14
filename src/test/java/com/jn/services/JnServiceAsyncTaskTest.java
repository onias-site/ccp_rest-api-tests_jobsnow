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

	@Test
	public void getAsyncTaskStatusByIdReturnsSameJsonTest() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation r = JnServiceAsyncTask.GetAsyncTaskStatusById.execute(input);
		assertSame(input, r);
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
