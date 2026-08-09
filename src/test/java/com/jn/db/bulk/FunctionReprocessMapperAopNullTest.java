package com.jn.db.bulk;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre {@code FunctionReprocessMapper}, classe de
 * visibilidade de pacote.
 */
public class FunctionReprocessMapperAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		FunctionReprocessMapper.INSTANCE.apply(null);
	}
}
