package com.jn.entities;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnEntityDisposableRecordTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void getDataWithTimeStampNullTest() {
		JnEntityDisposableRecord.getDataWithTimeStamp(null);
	}
}
