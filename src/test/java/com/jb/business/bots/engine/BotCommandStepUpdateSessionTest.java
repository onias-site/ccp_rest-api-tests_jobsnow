package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * {@link BotCommandStepUpdateSession} saves the session of the command at the next step, and the fields that never
 * change during the command (bot, chat, command) win over the ones the step produced.
 */
public class BotCommandStepUpdateSessionTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE);
	}

	private static final Long CHAT = 987654321L;

	private static final String COMMAND = "solveLoginTokenTicket";

	private final CcpJsonRepresentation ummutableFields = CcpOtherConstants.EMPTY_JSON
			.put(JnJsonInstantMessengerFields.botName, JbBotType.support.name())
			.put(JnJsonInstantMessengerFields.chatId, CHAT)
			.put(JnJsonInstantMessengerFields.commandName, COMMAND);

	@After
	public void deleteTheSession() {
		JbEntityBotCommandStepSession.ENTITY.delete(this.ummutableFields);
	}

	@Test
	public void theSessionIsSavedAtTheNextStepKeepingTheFieldsOfTheCommand() {
		BotCommandStep step = (BotCommandStep) JbBotEngine.INSTANCE.allSteps.get(COMMAND);
		CcpJsonRepresentation stepResult = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, "anotherBot")
				.put(JnJsonInstantMessengerFields.chatId, 1L)
				.put(JnJsonInstantMessengerFields.commandName, "anotherCommand")
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese.name());

		CcpJsonRepresentation savedSession = new BotCommandStepUpdateSession(step, this.ummutableFields, "theNextStep").execute(stepResult);

		assertEquals("theNextStep", savedSession.getAsString(JnJsonInstantMessengerFields.stepName));
		CcpJsonRepresentation stored = JbEntityBotCommandStepSession.ENTITY.getOneById(this.ummutableFields);
		assertEquals("theNextStep", stored.getAsString(JnJsonInstantMessengerFields.stepName));
		assertEquals(JbBotType.support.name(), stored.getAsString(JnJsonInstantMessengerFields.botName));
		assertEquals(CHAT, stored.getAsLongNumber(JnJsonInstantMessengerFields.chatId));
		assertEquals(COMMAND, stored.getAsString(JnJsonInstantMessengerFields.commandName));
	}
}
