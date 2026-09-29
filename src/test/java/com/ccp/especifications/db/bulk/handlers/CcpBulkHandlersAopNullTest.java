package com.ccp.especifications.db.bulk.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
 * Coverage of {@code CcpNullParameterAspect} over the bulk handlers
 * ({@code CcpBulkHandlerCreate}, {@code Delete}, {@code Read}, {@code Save} and the twin entity
 * handlers).
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
	public void createConstructorNullTest() {
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
	public void deleteConstructorNullTest() {
		new CcpBulkHandlerDelete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteConstructorWithCallbackEntityNullTest() {
		new CcpBulkHandlerDelete(null, notFound());
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteConstructorWithCallbackFunctionNullTest() {
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
	public void readConstructorNullTest() {
		new CcpBulkHandlerRead(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readConstructorWithCallbackEntityNullTest() {
		new CcpBulkHandlerRead(null, notFound());
	}

	@Test(expected = CcpNullParameterException.class)
	public void readConstructorWithCallbackFunctionNullTest() {
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
	public void saveConstructorNullTest() {
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
	public void saveTwinConstructorNullTest() {
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

	/** Any non-null function will do: these tests only exercise the null-parameter check. */
	private static final Function<CcpBulkItem, List<CcpBulkItem>> NOTHING_TO_DO = item -> new ArrayList<>();

	@Test(expected = CcpNullParameterException.class)
	public void transferTwinConstructorNullTest() {
		new CcpEntityBulkHandlerTransferRecordToTwinEntity(null, NOTHING_TO_DO);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTwinWhenFoundSearchParameterNullTest() {
		new CcpEntityBulkHandlerTransferRecordToTwinEntity(TWIN_ENTITY, NOTHING_TO_DO).whenRecordWasFoundInTheEntitySearch(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTwinWhenFoundRecordNullTest() {
		new CcpEntityBulkHandlerTransferRecordToTwinEntity(TWIN_ENTITY, NOTHING_TO_DO).whenRecordWasFoundInTheEntitySearch(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferTwinWhenNotFoundNullTest() {
		new CcpEntityBulkHandlerTransferRecordToTwinEntity(TWIN_ENTITY, NOTHING_TO_DO).whenRecordWasNotFoundInTheEntitySearch(null);
	}
}
