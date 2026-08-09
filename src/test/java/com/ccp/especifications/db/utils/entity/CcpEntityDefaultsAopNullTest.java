package com.ccp.especifications.db.utils.entity;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os métodos {@code default} da interface
 * {@code CcpEntity}. Uma implementação mínima é usada para que as chamadas resolvam para as
 * implementações default e não para as dos decorators.
 */
public class CcpEntityDefaultsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	/** Implementação mínima de {@code CcpEntity} que só resolve os metadados. */
	private static final class PlainEntity implements CcpEntity {

		public CcpEntityMetaData getEntityMetaData() {
			return JnEntityJobsnowError.ENTITY.getEntityMetaData();
		}
	}

	private static CcpEntity entity() {
		return new PlainEntity();
	}

	private static CcpSelectUnionAll unionAll() {
		return new CcpSelectUnionAll(new CcpJsonRepresentation[] { JSON },
				new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)), JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void calculateIdNullTest() {
		entity().calculateId(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void copyDataToJsonNullTest() {
		entity().copyDataTo(null, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void copyDataToEntityNullTest() {
		entity().copyDataTo(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteNullTest() {
		entity().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteAnyWhereNullTest() {
		entity().deleteAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void existsNullTest() {
		entity().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getHandledJsonNullTest() {
		entity().getHandledJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOneByIdNullTest() {
		entity().getOneById(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOneByIdAnyWhereNullTest() {
		entity().getOneByIdAnyWhere(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getParametersToSearchNullTest() {
		entity().getParametersToSearch(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRecordFromUnionAllUnionNullTest() {
		entity().getRecordFromUnionAll(null, () -> JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRecordFromUnionAllSupplierNullTest() {
		entity().getRecordFromUnionAll(unionAll(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isPresentInThisUnionAllUnionNullTest() {
		entity().isPresentInThisUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isPresentInThisUnionAllJsonNullTest() {
		entity().isPresentInThisUnionAll(unionAll(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveNullTest() {
		entity().save(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void toBulkItemsJsonNullTest() {
		entity().toBulkItems(null, CcpBulkEntityOperationType.create);
	}

	@Test(expected = CcpNullParameterException.class)
	public void toBulkItemsOperationNullTest() {
		entity().toBulkItems(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDataToJsonNullTest() {
		entity().transferDataTo(null, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void transferDataToEntityNullTest() {
		entity().transferDataTo(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validateJsonNullTest() {
		entity().validateJson(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getIdToSearchDisposableRecordNullTest() {
		entity().getIdToSearchDisposableRecord(null);
	}
}
