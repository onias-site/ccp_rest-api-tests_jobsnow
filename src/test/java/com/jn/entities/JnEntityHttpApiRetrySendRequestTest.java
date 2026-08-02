package com.jn.entities;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnEntityHttpApiRetrySendRequestTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void exceededTriesJsonNullTest() {
		JnEntityHttpApiRetrySendRequest.exceededTries(null, "attempts", 3);
	}

	@Test(expected = CcpNullParameterException.class)
	public void exceededTriesFieldNameNullTest() {
		JnEntityHttpApiRetrySendRequest.exceededTries(CcpOtherConstants.EMPTY_JSON, null, 3);
	}
}
