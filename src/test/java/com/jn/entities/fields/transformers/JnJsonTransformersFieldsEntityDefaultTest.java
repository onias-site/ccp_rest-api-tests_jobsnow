package com.jn.entities.fields.transformers;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class JnJsonTransformersFieldsEntityDefaultTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── enum values ──────────────────────────────────────────────────────────

	@Test
	public void allValuesExistTest() {
		assertNotNull(JnJsonTransformersFieldsEntityDefault.email);
		assertNotNull(JnJsonTransformersFieldsEntityDefault.password);
		assertNotNull(JnJsonTransformersFieldsEntityDefault.token);
		assertNotNull(JnJsonTransformersFieldsEntityDefault.timestamp);
		assertNotNull(JnJsonTransformersFieldsEntityDefault.tokenHash);
	}

	@Test
	public void valuesTest() {
		assertNotNull(JnJsonTransformersFieldsEntityDefault.values());
	}

	@Test
	public void valueOfTest() {
		assertNotNull(JnJsonTransformersFieldsEntityDefault.valueOf("email"));
	}

	// ── canBePrimaryKey ──────────────────────────────────────────────────────

	@Test
	public void canBePrimaryKeyTest() {
		JnJsonTransformersFieldsEntityDefault.email.canBePrimaryKey();
		JnJsonTransformersFieldsEntityDefault.password.canBePrimaryKey();
		JnJsonTransformersFieldsEntityDefault.token.canBePrimaryKey();
		JnJsonTransformersFieldsEntityDefault.timestamp.canBePrimaryKey();
		JnJsonTransformersFieldsEntityDefault.tokenHash.canBePrimaryKey();
	}

	// ── getOriginalToken (static utility, sem parâmetros) ────────────────────

	@Test
	public void getOriginalTokenTest() {
		String t = JnJsonTransformersFieldsEntityDefault.getOriginalToken();
		assertNotNull(t);
	}

	// ── null-parameter tests em cada apply() ─────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void applyEmailNullTest() {
		JnJsonTransformersFieldsEntityDefault.email.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyPasswordNullTest() {
		JnJsonTransformersFieldsEntityDefault.password.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyTokenNullTest() {
		JnJsonTransformersFieldsEntityDefault.token.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyTimestampNullTest() {
		JnJsonTransformersFieldsEntityDefault.timestamp.apply(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyTokenHashNullTest() {
		JnJsonTransformersFieldsEntityDefault.tokenHash.apply(null);
	}

	// ── JsonFieldNames inner enum ────────────────────────────────────────────

	@Test
	public void jsonFieldNamesValuesTest() {
		assertNotNull(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.values());
	}

	@Test
	public void jsonFieldNamesValueOfTest() {
		assertNotNull(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.valueOf("email"));
	}
}
