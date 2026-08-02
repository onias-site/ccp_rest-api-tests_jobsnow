package com.ccp.dependency.injection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.json.CcpJsonHandler;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class CcpDependencyInjectionTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void loadAndGetDependencyTest() {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
		assertTrue(CcpDependencyInjection.hasDependency(CcpJsonHandler.class));
		assertNotNull(CcpDependencyInjection.getDependency(CcpJsonHandler.class));
	}

	// Nota: replaceDependenciesTemporally requer que a interface CcpInstanceProvider
	// já esteja registrada como dependência, o que não é o caso em testes isolados.
	// Não há teste positivo trivial sem infraestrutura mais elaborada.

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void replaceDependenciesTemporallyJsonNullTest() {
		CcpDependencyInjection.replaceDependenciesTemporally(null, j -> j, new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void replaceDependenciesTemporallyBusinessNullTest() {
		CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, null, new CcpGsonJsonHandler());
	}

	@Test(expected = CcpNullParameterException.class)
	public void replaceDependenciesTemporallyProvidersNullTest() {
		CcpDependencyInjection.replaceDependenciesTemporally(CcpOtherConstants.EMPTY_JSON, j -> j, (CcpInstanceProvider<?>[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void loadAllDependenciesNullTest() {
		CcpDependencyInjection.loadAllDependencies((CcpInstanceProvider<?>[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void hasDependencyNullTest() {
		CcpDependencyInjection.hasDependency(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getDependencyNullTest() {
		CcpDependencyInjection.getDependency(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void removeDependecyNullTest() {
		CcpDependencyInjection.removeDependecy(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getInstanceNullTest() {
		CcpDependencyInjection.getInstance(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// Nota: getDependency lança exceção quando não encontra (nunca retorna null).
	// hasDependency retorna primitivo boolean.
	// replaceDependenciesTemporally e getInstance dependem de argumentos válidos.
}
