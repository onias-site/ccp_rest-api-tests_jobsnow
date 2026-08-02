package com.jn.entities.decorators;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

public class JnAsyncWriterEntityTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── construtor null-parameter tests ──────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorEntityNullTest() {
		new JnAsyncWriterEntity(null, JnAsyncWriterEntityTest.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorClassNullTest() {
		new JnAsyncWriterEntity(JnEntityJobsnowError.ENTITY, null);
	}

	// ── métodos com null-parameter ────────────────────────────────────────────
	// Todos os métodos (delete, deleteAnyWhere, save, transferDataTo, copyDataTo)
	// recebem null como primeiro arg. Como não temos setup de mensageria,
	// vamos criar a instância e chamar cada método com null.

	private JnAsyncWriterEntity newInstance() {
		return new JnAsyncWriterEntity(JnEntityJobsnowError.ENTITY, JnAsyncWriterEntityTest.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteNullTest() {
		newInstance().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteAnyWhereNullTest() {
		newInstance().deleteAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveNullTest() {
		newInstance().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDataToJsonNullTest() {
		newInstance().transferDataTo(null, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDataToEntitiesNullTest() {
		newInstance().transferDataTo(CcpOtherConstants.EMPTY_JSON, (CcpEntity) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void copyDataToJsonNullTest() {
		newInstance().copyDataTo(null, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void copyDataToEntitiesNullTest() {
		newInstance().copyDataTo(CcpOtherConstants.EMPTY_JSON, (CcpEntity) null);
	}
}
