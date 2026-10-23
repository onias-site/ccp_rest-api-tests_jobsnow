package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;

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
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * {@link BotNewSessionProducer} turns the message received into the session of a new command: the message becomes the
 * typed value, the language of the chat is kept, and the command is the one typed (a text that is no command shows all
 * the commands). The input it receives is ignored.
 */
public class BotNewSessionProducerTest {

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

	private static CcpJsonRepresentation messageTyped(String text) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support.name())
				.put(JnJsonInstantMessengerFields.chatId, 1L)
				.put(JnJsonInstantMessengerFields.message, text);
	}

	@Test
	public void theMessageBecomesTheTypedValueInTheLanguageOfTheChat() {
		CcpJsonRepresentation session = new BotNewSessionProducer(messageTyped("/chatId"), "english").execute(CcpOtherConstants.EMPTY_JSON);

		assertEquals("/chatId", session.getAsString(JnJsonCommonsFields.typedValue));
		assertEquals("english", session.getAsString(JnJsonCommonsFields.language));
		assertEquals("chatId", session.getAsString(JnJsonInstantMessengerFields.commandName));
	}

	@Test
	public void aTextThatIsNoCommandShowsAllTheCommands() {
		CcpJsonRepresentation session = new BotNewSessionProducer(messageTyped("hello"), "portuguese").execute(CcpOtherConstants.EMPTY_JSON);

		assertEquals(JbDefaultBotCommandStep.showAllCommands.name(), session.getAsString(JnJsonInstantMessengerFields.commandName));
		assertEquals(JbDefaultBotCommandStep.showAllCommands.name(), session.getAsString(JnJsonInstantMessengerFields.stepName));
	}
}
