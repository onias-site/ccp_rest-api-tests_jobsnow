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
 * The handlers of the updates of the bots kept by jn and seeded by the cost center of the bot engine: the key is the name
 * of the bot, and the seed is a {@code create} with the class name of the business.
 */
public class JnEntityBotUpdateHandlerTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void theKeyHasOnlyTheNameOfTheBot() {
		CcpJsonRepresentation key = JnEntityBotUpdateHandler.getKey("support");
		assertEquals("support", key.getAsString(JnEntityBotUpdateHandler.Fields.botName));
		assertEquals(1, key.fieldSet().size());
	}

	@Test
	public void theSeedCreatesTheHandlerWithTheClassNameOfTheBusiness() {
		List<CcpBulkItem> seeds = JnEntityBotUpdateHandler.getSeeds("support", NoopBusiness.class);
		assertEquals(1, seeds.size());
		CcpBulkItem seed = seeds.get(0);
		assertEquals(CcpBulkEntityOperationType.create, seed.operation);
		assertEquals("jn_bot_update_handler", seed.entity.getEntityMetaData().entityName);
		assertEquals("support", seed.json.getAsString(JnEntityBotUpdateHandler.Fields.botName));
		assertEquals(NoopBusiness.class.getName(), seed.json.getAsString(JnEntityBotUpdateHandler.Fields.business));
	}

	@Test(expected = CcpNullParameterException.class)
	public void getKeyNullTest() {
		JnEntityBotUpdateHandler.getKey(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getSeedsNullBusinessTest() {
		JnEntityBotUpdateHandler.getSeeds("support", null);
	}
}
