package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre {@code JnDisposableEntity}. O construtor é
 * {@code protected}, portanto só alcançável de dentro deste pacote.
 */
public class JnDisposableEntityAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static JnDisposableEntity disposable() {
		return new JnDisposableEntity(ENTITY, JnEntityJobsnowError.class);
	}

	private static CcpSelectUnionAll unionAll() {
		return new CcpSelectUnionAll(new CcpJsonRepresentation[] { JSON },
				new ArrayList<CcpJsonRepresentation>(Arrays.asList(JSON)), ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorEntityNullTest() {
		new JnDisposableEntity(null, JnEntityJobsnowError.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void construtorClassNullTest() {
		new JnDisposableEntity(ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void calculateIdNullTest() {
		disposable().calculateId(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void existsNullTest() {
		disposable().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOneByIdNullTest() {
		disposable().getOneById(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getParametersToSearchNullTest() {
		disposable().getParametersToSearch(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRecordFromUnionAllJsonUnionNullTest() {
		disposable().getRecordFromUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRecordFromUnionAllJsonNullTest() {
		disposable().getRecordFromUnionAll(unionAll(), (CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRecordFromUnionAllSupplierUnionNullTest() {
		disposable().getRecordFromUnionAll(null, () -> JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRecordFromUnionAllSupplierNullTest() {
		disposable().getRecordFromUnionAll(unionAll(), (java.util.function.Supplier<CcpJsonRepresentation>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isPresentInThisUnionAllUnionNullTest() {
		disposable().isPresentInThisUnionAll(null, JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isPresentInThisUnionAllJsonNullTest() {
		disposable().isPresentInThisUnionAll(unionAll(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getIdToSearchDisposableRecordNullTest() {
		disposable().getIdToSearchDisposableRecord(null);
	}
}
