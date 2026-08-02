package com.ccp.implementations.db.crud.elasticsearch;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;

public class CcpElasticSearchCrudTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpElasticSearchCrud());
	}

	private static CcpCrud getCrud() {
		return CcpDependencyInjection.getDependency(CcpCrud.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpElasticSearchCrud());
	}

	@Test
	public void getInstanceTest() {
		CcpCrud instance = new CcpElasticSearchCrud().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getOneByIdEntityNullTest() {
		getCrud().getOneById(null, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOneByIdIdNullTest() {
		getCrud().getOneById("entity", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void existsEntityNullTest() {
		getCrud().exists(null, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void existsIdNullTest() {
		getCrud().exists("entity", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveEntityNullTest() {
		getCrud().save(null, CcpOtherConstants.EMPTY_JSON, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveJsonNullTest() {
		getCrud().save("entity", null, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveIdNullTest() {
		getCrud().save("entity", CcpOtherConstants.EMPTY_JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteEntityNullTest() {
		getCrud().delete(null, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteIdNullTest() {
		getCrud().delete("entity", null);
	}
}
