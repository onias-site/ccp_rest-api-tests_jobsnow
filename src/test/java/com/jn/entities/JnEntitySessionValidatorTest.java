package com.jn.entities;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * The validators of session kept by jn and seeded by the cost center of the login: the key is the name of the validator,
 * and the seed is a {@code create} with the class name of the business.
 */
public class JnEntitySessionValidatorTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void theKeyHasOnlyTheNameOfTheValidator() {
		CcpJsonRepresentation key = JnEntitySessionValidator.getKey(JnSessionValidatorNames.userSession);
		assertEquals("userSession", key.getAsString(JnEntitySessionValidator.Fields.validatorName));
		assertEquals(1, key.fieldSet().size());
	}

	@Test
	public void theSeedCreatesTheValidatorWithTheClassNameOfTheBusiness() {
		List<CcpBulkItem> seeds = JnEntitySessionValidator.getSeeds(JnSessionValidatorNames.userSession, NoopBusiness.class);
		assertEquals(1, seeds.size());
		CcpBulkItem seed = seeds.get(0);
		assertEquals(CcpBulkEntityOperationType.create, seed.operation);
		assertEquals("jn_session_validator", seed.entity.getEntityMetaData().entityName);
		assertEquals("userSession", seed.json.getAsString(JnEntitySessionValidator.Fields.validatorName));
		assertEquals(NoopBusiness.class.getName(), seed.json.getAsString(JnEntitySessionValidator.Fields.business));
	}

	@Test(expected = CcpNullParameterException.class)
	public void getKeyNullTest() {
		JnEntitySessionValidator.getKey(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getSeedsNullBusinessTest() {
		JnEntitySessionValidator.getSeeds(JnSessionValidatorNames.userSession, null);
	}

	@Test
	public void validatorNamesTest() {
		assertEquals(JnSessionValidatorNames.userSession, JnSessionValidatorNames.valueOf("userSession"));
	}
}
