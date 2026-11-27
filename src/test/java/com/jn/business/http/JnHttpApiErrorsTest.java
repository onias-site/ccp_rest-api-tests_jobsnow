package com.jn.business.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.especifications.http.CcpErrorHttpClient;
import com.ccp.especifications.http.CcpErrorHttpClientPermanent;
import com.ccp.especifications.http.CcpErrorHttpServer;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.business.messages.JnSendHtmlInstantMessage;
import com.jn.entities.JnEntityHttpApiErrorClient;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Since 2026-11-27 the HTTP failures of the instant messenger and of the e-mail provider are recorded with the moment of
 * the failure ({@link JnHttpApiErrors}): a 4xx in {@code jn_http_api_error_client}, thrown as permanent; a 5xx in
 * {@code jn_http_api_error_server}, thrown as it came. Until then nothing was recorded: the 400 of a bot without the right
 * to write in a group and the 500 of the suppressions of the e-mail provider left no record.
 */
public class JnHttpApiErrorsTest {

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

	private final String url = "https://api.telegram.org/bot***/sendMessage?test=" + System.nanoTime();

	/** The error the HTTP requester raises, with the same fields. */
	private CcpJsonRepresentation errorEntity(int status, String response) {
		return CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("url"), this.url)
				.put(new CcpFieldName("method"), "POST")
				.put(new CcpFieldName("headers"), CcpOtherConstants.EMPTY_JSON.content)
				.put(new CcpFieldName("request"), "{\"chat_id\":\"-1001303593538\",\"text\":\"vaga\"}")
				.put(new CcpFieldName("status"), status)
				.put(new CcpFieldName("response"), response)
				.put(new CcpFieldName("trace"), "sendInstantMessage")
				.put(new CcpFieldName("expectedStatusList"), Arrays.asList(200));
	}

	/** The primary key of the record: the call, the api and the whole error as details. */
	private CcpJsonRepresentation recordKey(CcpErrorHttp error, String apiName) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.url, this.url)
				.put(JnJsonCommonsFields.method, "POST")
				.put(JnJsonCommonsFields.headers, CcpOtherConstants.EMPTY_JSON.content)
				.put(JnJsonCommonsFields.apiName, apiName)
				.put(JnJsonCommonsFields.details, error.entity.asUgglyJson());
	}

	/** An instant messenger that fails every sending with the given error. */
	static class FailingInstantMessenger implements CcpInstantMessenger {
		final RuntimeException error;

		FailingInstantMessenger(RuntimeException error) {
			this.error = error;
		}

		public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
			throw this.error;
		}

		public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
			throw this.error;
		}
	}

	private static CcpJsonFieldName support() {
		return new CcpStringDecorator("support").jsonFieldName();
	}

	@Test
	public void aClientErrorOfTheInstantMessengerIsRecordedAndThrownAsPermanent() {
		String response = "{\"ok\":false,\"error_code\":400,\"description\":\"Bad Request: not enough rights to send text messages to the chat\"}";
		CcpErrorHttpClient error = new CcpErrorHttpClient(this.errorEntity(400, response));
		FailingInstantMessenger instantMessenger = new FailingInstantMessenger(error);

		try {
			JnSendHtmlInstantMessage.send(instantMessenger, support(), "token", -1001303593538L, 0L, "vaga");
			fail("the sending failed");
		} catch (CcpErrorHttpClientPermanent e) {
			assertSame(error, e.clientError);
		}

		CcpJsonRepresentation key = this.recordKey(error, "telegram");
		assertTrue("the failure was recorded", JnEntityHttpApiErrorClient.ENTITY.exists(key));
		CcpJsonRepresentation record = JnEntityHttpApiErrorClient.ENTITY.getOneById(key);
		assertEquals(400, (int) record.getAsIntegerNumber(JnJsonCommonsFields.httpStatus));
		assertEquals(response, record.getAsString(JnJsonCommonsFields.response));
		assertTrue("the moment of the failure", record.containsAllFields(JnJsonCommonsFields.timestamp));
		assertFalse(JnEntityHttpApiErrorServer.ENTITY.exists(key));
	}

	@Test
	public void aServerErrorOfTheEmailProviderIsRecordedAndThrownAsItCame() {
		String response = "{\"errors\":[{\"field\":null,\"message\":\"Internal Server Error\"}]}";
		CcpErrorHttpServer error = new CcpErrorHttpServer(this.errorEntity(500, response));

		RuntimeException thrown = JnHttpApiErrors.record(JnHttpApiErrors.ExternalApi.sendgrid, error);

		assertSame("a server error may be retried", error, thrown);
		CcpJsonRepresentation key = this.recordKey(error, "sendgrid");
		assertTrue("the failure was recorded", JnEntityHttpApiErrorServer.ENTITY.exists(key));
		CcpJsonRepresentation record = JnEntityHttpApiErrorServer.ENTITY.getOneById(key);
		assertEquals(500, (int) record.getAsIntegerNumber(JnJsonCommonsFields.httpStatus));
	}

	@Test
	public void aFailureThatIsNotOfHttpIsNotRecorded() {
		IllegalStateException error = new IllegalStateException("no connection");
		FailingInstantMessenger instantMessenger = new FailingInstantMessenger(error);

		try {
			JnSendHtmlInstantMessage.send(instantMessenger, support(), "token", -1001303593538L, 0L, "vaga");
			fail("the sending failed");
		} catch (IllegalStateException e) {
			assertSame(error, e);
		}
	}
}
