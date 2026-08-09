package com.ccp.especifications.db.bulk.handlers;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityContactUs;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os handlers bulk
 * ({@code CcpBulkHandlerCreate}, {@code Delete}, {@code Read}, {@code Save} e os handlers de
 * entidade twin).
 */
public class CcpBulkHandlersAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final CcpEntity TWIN_ENTITY = JnEntityContactUs.ENTITY;

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static java.util.function.Function<CcpBulkItem, List<CcpBulkItem>> notFound() {
		return item -> new ArrayList<CcpBulkItem>();
	}

	// ── CcpBulkHandlerCreate ──────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void createConstrutorNullTest() {
		new CcpBulkHandlerCreate(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createWhenFoundSearchParameterNullTest() {
		new CcpBulkHandlerCreate(ENTITY).whenRecordWasFoundInTheEntitySearch(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createWhenFoundRecordNullTest() {
		new CcpBulkHandlerCreate(ENTITY).whenRecordWasFoundInTheEntitySearch(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void createWhenNotFoundNullTest() {
		new CcpBulkHandlerCreate(ENTITY).whenRecordWasNotFoundInTheEntitySearch(null);
	}

	// ── CcpBulkHandlerDelete ──────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void deleteConstrutorNullTest() {
		new CcpBulkHandlerDelete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteConstrutorComCallbackEntityNullTest() {
		new CcpBulkHandlerDelete(null, notFound());
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteConstrutorComCallbackFunctionNullTest() {
		new CcpBulkHandlerDelete(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteWhenFoundSearchParameterNullTest() {
		new CcpBulkHandlerDelete(ENTITY).whenRecordWasFoundInTheEntitySearch(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteWhenFoundRecordNullTest() {
		new CcpBulkHandlerDelete(ENTITY).whenRecordWasFoundInTheEntitySearch(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteWhenNotFoundNullTest() {
		new CcpBulkHandlerDelete(ENTITY).whenRecordWasNotFoundInTheEntitySearch(null);
	}

	// ── CcpBulkHandlerRead ────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void readConstrutorNullTest() {
		new CcpBulkHandlerRead(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readConstrutorComCallbackEntityNullTest() {
		new CcpBulkHandlerRead(null, notFound());
	}

	@Test(expected = CcpNullParameterException.class)
	public void readConstrutorComCallbackFunctionNullTest() {
		new CcpBulkHandlerRead(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readWhenFoundSearchParameterNullTest() {
		new CcpBulkHandlerRead(ENTITY).whenRecordWasFoundInTheEntitySearch(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readWhenFoundRecordNullTest() {
		new CcpBulkHandlerRead(ENTITY).whenRecordWasFoundInTheEntitySearch(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readWhenNotFoundNullTest() {
		new CcpBulkHandlerRead(ENTITY).whenRecordWasNotFoundInTheEntitySearch(null);
	}

	// ── CcpBulkHandlerSave ────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void saveConstrutorNullTest() {
		new CcpBulkHandlerSave(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveWhenFoundSearchParameterNullTest() {
		new CcpBulkHandlerSave(ENTITY).whenRecordWasFoundInTheEntitySearch(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveWhenFoundRecordNullTest() {
		new CcpBulkHandlerSave(ENTITY).whenRecordWasFoundInTheEntitySearch(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveWhenNotFoundNullTest() {
		new CcpBulkHandlerSave(ENTITY).whenRecordWasNotFoundInTheEntitySearch(null);
	}

	// ── CcpEntityBulkHandlerSaveTwinEntity ────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void saveTwinConstrutorNullTest() {
		new CcpEntityBulkHandlerSaveTwinEntity(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveTwinWhenFoundSearchParameterNullTest() {
		new CcpEntityBulkHandlerSaveTwinEntity(TWIN_ENTITY).whenRecordWasFoundInTheEntitySearch(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveTwinWhenFoundRecordNullTest() {
		new CcpEntityBulkHandlerSaveTwinEntity(TWIN_ENTITY).whenRecordWasFoundInTheEntitySearch(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveTwinWhenNotFoundNullTest() {
		new CcpEntityBulkHandlerSaveTwinEntity(TWIN_ENTITY).whenRecordWasNotFoundInTheEntitySearch(null);
	}

	// ── CcpEntityBulkHandlerTransferRecordToTwinEntity ────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void transferTwinConstrutorNullTest() {
		new CcpEntityBulkHandlerTransferRecordToTwinEntity(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTwinWhenFoundSearchParameterNullTest() {
		new CcpEntityBulkHandlerTransferRecordToTwinEntity(TWIN_ENTITY).whenRecordWasFoundInTheEntitySearch(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTwinWhenFoundRecordNullTest() {
		new CcpEntityBulkHandlerTransferRecordToTwinEntity(TWIN_ENTITY).whenRecordWasFoundInTheEntitySearch(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTwinWhenNotFoundNullTest() {
		new CcpEntityBulkHandlerTransferRecordToTwinEntity(TWIN_ENTITY).whenRecordWasNotFoundInTheEntitySearch(null);
	}
}
