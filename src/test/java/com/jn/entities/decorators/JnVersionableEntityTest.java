package com.jn.entities.decorators;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

public class JnVersionableEntityTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── construtor null-parameter tests ──────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorEntityNullTest() {
		new JnVersionableEntity(null, JnVersionableEntityTest.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorClassNullTest() {
		new JnVersionableEntity(JnEntityJobsnowError.ENTITY, null);
	}

	// ── métodos com null-parameter ────────────────────────────────────────────
	// deleteAnyWhere(json) — não precisa de setup ES pois retorna imediatamente
	// getOneByIdAnyWhere(json) — chama throwException interno
	// toBulkItems(json, operation) — 2 null-params

	private JnVersionableEntity newInstance() {
		return new JnVersionableEntity(JnEntityJobsnowError.ENTITY, JnVersionableEntityTest.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteAnyWhereNullTest() {
		newInstance().deleteAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOneByIdAnyWhereNullTest() {
		newInstance().getOneByIdAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void toBulkItemsJsonNullTest() {
		newInstance().toBulkItems(null, com.ccp.especifications.db.bulk.CcpBulkEntityOperationType.create);
	}

	@Test(expected = CcpNullParameterException.class)
	public void toBulkItemsOperationNullTest() {
		newInstance().toBulkItems(com.ccp.constants.CcpOtherConstants.EMPTY_JSON, null);
	}
}
