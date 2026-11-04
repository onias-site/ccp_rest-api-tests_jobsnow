package com.js.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.business.bots.engine.JbBusinessReceiveBotUpdate;
import com.jb.entities.JbEntityBotUpdateId;
import com.jn.entities.JnEntityBotUpdateHandler;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.js.business.telegram.JsTelegramWebhookSecret;

/**
 * Proves {@code JsRestApiTelegramWebhook} ({@code POST /telegram-webhook/{botName}}, port 8082, which must be up), the
 * door of production of the support bot: the secret comes in the header {@code X-Telegram-Bot-Api-Secret-Token}, and
 * only with it the update reaches the bot. An update without message is used, so nothing goes out to Telegram. The rules
 * behind the door are proved in {@code JsServiceTelegramWebhookTest}; until 2026-11-03 the door itself had no test.
 */
public class JsRestApiTelegramWebhookTest {

	enum JsonFieldNames implements CcpJsonFieldName {
		X_Telegram_Bot_Api_Secret_Token {
			public String getValue() {
				return "X-Telegram-Bot-Api-Secret-Token";
			}
		},
		Content_Type {
			public String getValue() {
				return "Content-Type";
			}
		},
		application_json {
			public String getValue() {
				return "application/json";
			}
		},
	}

	static {
		CcpDependencyInjection.removeAllDependencies();
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

	private static final String SUPPORT = "support";

	private static final long UPDATE_ID = 7100L;

	private static final String JS_API_HOST = "localhost";

	private static final int JS_API_PORT = 8082;

	@BeforeClass
	public static void theJsApiMustBeUpAndTheBotHasItsHandler() {
		try (Socket socket = new Socket()) {
			InetSocketAddress jsApi = new InetSocketAddress(JS_API_HOST, JS_API_PORT);
			socket.connect(jsApi, 2_000);
		} catch (IOException e) {
			throw new AssertionError("The js API is down on port " + JS_API_PORT + ": start JsRestApiSpringStarter before running " + JsRestApiTelegramWebhookTest.class.getSimpleName(), e);
		}
		List<CcpBulkItem> seeds = JnEntityBotUpdateHandler.getSeeds(SUPPORT, JbBusinessReceiveBotUpdate.class);
		CcpBulkItem seed = seeds.get(0);
		JnEntityBotUpdateHandler.ENTITY.save(seed.json);
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

	private String update() {
		String update = "{\"update_id\": " + UPDATE_ID + ", \"edited_message\": {\"text\": \"edited\"}}";
		return update;
	}

	private HttpResponse<String> post(String botName, String secret, String body) throws Exception {
		URI uri = URI.create("http://" + JS_API_HOST + ":" + JS_API_PORT + "/telegram-webhook/" + botName);
		String contentTypeHeader = JsonFieldNames.Content_Type.getValue();
		String contentType = JsonFieldNames.application_json.getValue();
		HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
				.header(contentTypeHeader, contentType)
				.POST(HttpRequest.BodyPublishers.ofString(body));
		boolean noSecret = secret.isEmpty();
		HttpRequest.Builder builderWithSecret = noSecret ? builder : builder.header(JsonFieldNames.X_Telegram_Bot_Api_Secret_Token.getValue(), secret);
		HttpRequest request = builderWithSecret.build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response;
	}

	private long savedOffset() {
		CcpJsonRepresentation offset = JbEntityBotUpdateId.ENTITY.getOneById(this.offsetKey());
		Long updateId = offset.getAsLongNumber(JbEntityBotUpdateId.Fields.updateId);
		return updateId;
	}

	@Test
	public void theUpdateWithTheSecretOfTheBotReachesTheBot() throws Exception {
		String secret = JsTelegramWebhookSecret.INSTANCE.getSecret(SUPPORT);

		HttpResponse<String> response = this.post(SUPPORT, secret, this.update());

		assertEquals(response.body(), 200, response.statusCode());
		assertEquals(UPDATE_ID + 1, this.savedOffset());
	}

	/** Telegram delivers again what it thinks was lost: the same update answers 200 and keeps the offset. */
	@Test
	public void theSameUpdateDeliveredAgainIsAcceptedOnce() throws Exception {
		String secret = JsTelegramWebhookSecret.INSTANCE.getSecret(SUPPORT);
		this.post(SUPPORT, secret, this.update());

		HttpResponse<String> response = this.post(SUPPORT, secret, this.update());

		assertEquals(response.body(), 200, response.statusCode());
		assertEquals(UPDATE_ID + 1, this.savedOffset());
	}

	@Test
	public void anUpdateWithAWrongSecretIsUnauthorized() throws Exception {
		HttpResponse<String> response = this.post(SUPPORT, "wrong", this.update());

		assertEquals(response.body(), 401, response.statusCode());
		assertFalse("a refused request reached the bot", JbEntityBotUpdateId.ENTITY.exists(this.offsetKey()));
	}

	@Test
	public void anUpdateWithNoSecretHeaderIsUnauthorized() throws Exception {
		HttpResponse<String> response = this.post(SUPPORT, "", this.update());

		assertEquals(response.body(), 401, response.statusCode());
		assertFalse("a refused request reached the bot", JbEntityBotUpdateId.ENTITY.exists(this.offsetKey()));
	}

	@Test
	public void anUnknownBotIsAValidationError() throws Exception {
		HttpResponse<String> response = this.post("unknownBot", "any", this.update());

		assertEquals(response.body(), 422, response.statusCode());
	}

	@Test
	public void aBodyThatIsNotJsonIsABadRequest() throws Exception {
		String secret = JsTelegramWebhookSecret.INSTANCE.getSecret(SUPPORT);

		HttpResponse<String> response = this.post(SUPPORT, secret, "not json");

		assertEquals(response.body(), 400, response.statusCode());
		assertFalse(JbEntityBotUpdateId.ENTITY.exists(this.offsetKey()));
	}
}
