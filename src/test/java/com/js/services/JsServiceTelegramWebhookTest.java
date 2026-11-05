package com.js.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.CountingMensageriaSender;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.jn.business.bots.JnBusinessHandleBotUpdate;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.business.bots.engine.JbBusinessReceiveBotUpdate;
import com.jb.entities.JbEntityBotUpdateId;
import com.jn.entities.JnEntityBotUpdateHandler;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.js.business.telegram.JsBusinessValidateTelegramWebhookSecret;
import com.js.business.telegram.JsTelegramWebhookSecret;
import com.js.status.telegram.JsProcessStatusTelegramWebhook;

/**
 * Only a request with the secret of the bot reaches the bot, through the business that jb seeds and js does not import.
 * An update without message is used so that nothing goes out to Telegram: it proves the way up to the offset of the bot.
 * The messaging counts what is published (and runs it at once, as the synchronous messaging does), which proves that
 * the update goes to the queue and not straight to the bot (OQL, since 2026-11-04).
 */
public class JsServiceTelegramWebhookTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpInstanceProvider<CcpMensageriaSender> countingMensageria = () -> CountingMensageriaSender.INSTANCE;
		CcpDependencyInjection.loadAllDependencies(
				countingMensageria,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE);
	}

	private static final String SUPPORT = "support";

	private static final long UPDATE_ID = 7000L;

	@BeforeClass
	public static void saveTheHandlerOfTheBot() {
		List<CcpBulkItem> seeds = JnEntityBotUpdateHandler.getSeeds(SUPPORT, JbBusinessReceiveBotUpdate.class);
		JnEntityBotUpdateHandler.ENTITY.save(seeds.get(0).json);
	}

	@Before
	@After
	public void clearTheOffset() {
		JbEntityBotUpdateId.ENTITY.delete(this.offsetKey());
	}

	private CcpJsonRepresentation offsetKey() {
		CcpJsonRepresentation offsetKey = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.botName, SUPPORT);
		return offsetKey;
	}

	private Map<String, Object> request(String botName, String secret) {
		CcpJsonRepresentation update = new CcpJsonRepresentation("{\"update_id\": " + UPDATE_ID + ", \"edited_message\": {\"text\": \"edited\"}}");
		CcpJsonRepresentation request = update
				.put(JnJsonInstantMessengerFields.botName, botName)
				.put(JsBusinessValidateTelegramWebhookSecret.Fields.webhookSecret, secret);
		return request.content;
	}

	/** How many updates have been published to the topic of the bot so far. */
	private long publishedUpdates() {
		String topic = CcpMensageriaReceiver.getQualifiedTopic(JnBusinessHandleBotUpdate.class, SUPPORT);
		String noOperation = CcpOtherConstants.EMPTY_STRING.getValue();
		long howMany = CountingMensageriaSender.howMany(topic, noOperation);
		return howMany;
	}

	private void assertRefused(Map<String, Object> request) {
		long publishedBefore = this.publishedUpdates();
		try {
			JsServiceTelegramWebhook.ReceiveTelegramUpdate.execute(request);
			fail("the request was accepted");
		} catch (CcpErrorFlowDisturb e) {
			assertEquals(JsProcessStatusTelegramWebhook.invalidWebhookSecret, e.status);
			assertEquals(401, e.status.asNumber());
		}
		assertFalse("a refused request reached the bot", JbEntityBotUpdateId.ENTITY.exists(this.offsetKey()));
		assertEquals("a refused request reached the queue", publishedBefore, this.publishedUpdates());
	}

	@Test
	public void theSecretOfTheBotLetsTheUpdateThroughToTheSeededHandler() {
		String secret = JsTelegramWebhookSecret.INSTANCE.getSecret(SUPPORT);
		long publishedBefore = this.publishedUpdates();

		Map<String, Object> answer = JsServiceTelegramWebhook.ReceiveTelegramUpdate.execute(this.request(SUPPORT, secret));

		assertTrue(answer.isEmpty());
		assertEquals("the update did not go through the queue", publishedBefore + 1, this.publishedUpdates());
		CcpJsonRepresentation offset = JbEntityBotUpdateId.ENTITY.getOneById(this.offsetKey());
		assertEquals(UPDATE_ID + 1, offset.getAsLongNumber(JbEntityBotUpdateId.Fields.updateId).longValue());
	}

	@Test
	public void aWrongSecretIsRefused() {
		this.assertRefused(this.request(SUPPORT, "wrong"));
	}

	/** A missing header arrives as an empty text, which is a 401 and not a 422 of the validation. */
	@Test
	public void aMissingSecretIsRefused() {
		this.assertRefused(this.request(SUPPORT, ""));
	}

	/** The token of the bot is not the secret: the secret is what Telegram sends back, and the token must not travel. */
	@Test
	public void theSecretIsNotTheToken() {
		String botToken = JsBotTokenForTest.supportBotToken();
		assertFalse(botToken.isEmpty());
		this.assertRefused(this.request(SUPPORT, botToken));
	}

	/** Until 2026-11-01 the secret was this hash: whoever had the token could speak as the operator of the support bot. */
	@Test
	public void theHashOfTheTokenIsNotTheSecret() {
		CcpStringDecorator botToken = new CcpStringDecorator(JsBotTokenForTest.supportBotToken());
		String hashOfTheToken = botToken.hash().asString(CcpHashAlgorithm.SHA256);
		this.assertRefused(this.request(SUPPORT, hashOfTheToken));
	}

	/** Without a configured secret the one of the bot is empty, and a request without the header must not match it. */
	@Test
	public void aBotWithoutSecretRefusesEvenAnEmptySecret() {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, "botWithoutSecret")
				.put(JsBusinessValidateTelegramWebhookSecret.Fields.webhookSecret, "");
		assertEquals("", JsTelegramWebhookSecret.INSTANCE.getSecret("botWithoutSecret"));
		try {
			JsBusinessValidateTelegramWebhookSecret.INSTANCE.execute(request);
			fail("a bot without secret accepted an empty one");
		} catch (CcpErrorFlowDisturb e) {
			assertEquals(401, e.status.asNumber());
		}
	}

	/** The secret travels in a header, so it must be accepted by Telegram: 1 to 256 of A-Z, a-z, 0-9, _ and -. */
	@Test
	public void theSecretHasTheCharactersTelegramAccepts() {
		String secret = JsTelegramWebhookSecret.INSTANCE.getSecret(SUPPORT);
		assertTrue(secret, secret.matches("[A-Za-z0-9_-]{1,256}"));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void anUnknownBotIsRefused() {
		JsServiceTelegramWebhook.ReceiveTelegramUpdate.execute(this.request("unknownBot", "any"));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void anUpdateWithoutIdIsRefused() {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, SUPPORT)
				.put(JsBusinessValidateTelegramWebhookSecret.Fields.webhookSecret, "any");
		JsServiceTelegramWebhook.ReceiveTelegramUpdate.execute(request.content);
	}
}
