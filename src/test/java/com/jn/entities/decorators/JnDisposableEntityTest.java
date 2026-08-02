package com.jn.entities.decorators;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

// Test está no mesmo package para acessar o construtor protected de JnDisposableEntity.
public class JnDisposableEntityTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── construtor null-parameter tests ──────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorEntityNullTest() {
		new JnDisposableEntity(null, JnDisposableEntityTest.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorClassNullTest() {
		new JnDisposableEntity(JnEntityJobsnowError.ENTITY, null);
	}
}
