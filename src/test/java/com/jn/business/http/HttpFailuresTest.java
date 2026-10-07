package com.jn.business.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.http.CcpErrorHttpClient;
import com.ccp.especifications.http.CcpErrorHttpServer;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityHttpApiErrorClient;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.entities.JnEntityHttpApiRetrySendRequest;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves how {@link JnBusinessSendHttpRequest} handles the failures of the call, with an executor that fails without any
 * network: a server error (5xx) is retried, one attempt registered per try, until the maximum; a client error (4xx) is
 * rethrown at once; any other failure goes to the exception handler.
 */
public class HttpFailuresTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
	}

	private final String url = "http://localhost/test/" + System.nanoTime();

	private final CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.apiName, "testApi");

	/** The error the HTTP requester raises, with the same fields. */
	private CcpJsonRepresentation errorEntity(int status) {
		return CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("url"), this.url)
				.put(new CcpFieldName("method"), "POST")
				.put(new CcpFieldName("headers"), CcpOtherConstants.EMPTY_JSON.content)
				.put(new CcpFieldName("request"), "{}")
				.put(new CcpFieldName("status"), status)
				.put(new CcpFieldName("response"), "failed")
				.put(new CcpFieldName("expectedStatusList"), Arrays.asList(200));
	}

	/** The primary key of the record of the failure: the call, the api and the whole error as details. */
	private CcpJsonRepresentation errorKey(CcpJsonRepresentation errorEntity) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.url, this.url)
				.put(JnJsonCommonsFields.method, "POST")
				.put(JnJsonCommonsFields.headers, CcpOtherConstants.EMPTY_JSON.content)
				.put(JnJsonCommonsFields.apiName, "testApi")
				.put(JnJsonCommonsFields.details, errorEntity.asUgglyJson());
	}

	/** An executor that always fails with the given error and counts its attempts. */
	class FailingExecutor implements CcpHttpApiExecutor {
		final AtomicInteger attempts = new AtomicInteger();

		final RuntimeException error;

		FailingExecutor(RuntimeException error) {
			this.error = error;
		}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			this.attempts.incrementAndGet();
			throw this.error;
		}

		public int getMaxTries() {
			return 2;
		}

		public int getSleepTimeToRetry() {
			return 1;
		}
	}

	@Test
	public void aServerErrorIsRetriedUntilTheMaximumAndThenRethrown() {
		CcpErrorHttpServer error = new CcpErrorHttpServer(this.errorEntity(503));
		FailingExecutor executor = new FailingExecutor(error);
		JnBusinessSendHttpRequest business = new JnBusinessSendHttpRequest(executor, e -> CcpOtherConstants.EMPTY_JSON);

		try {
			business.execute(this.request);
			fail("every attempt failed");
		} catch (CcpErrorHttpServer e) {
			assertSame(error, e);
		}

		assertEquals("the first call plus one per registered attempt", 3, executor.attempts.get());
		assertTrue("the error was recorded after the last attempt", JnEntityHttpApiErrorServer.ENTITY.exists(this.errorKey(error.entity)));
	}

	@Test
	public void aClientErrorIsRethrownAtOnce() {
		CcpErrorHttpClient error = new CcpErrorHttpClient(this.errorEntity(404));
		FailingExecutor executor = new FailingExecutor(error);
		JnBusinessSendHttpRequest business = new JnBusinessSendHttpRequest(executor, e -> CcpOtherConstants.EMPTY_JSON);

		try {
			business.execute(this.request);
			fail("a client error is not retried");
		} catch (CcpErrorHttpClient e) {
			assertSame(error, e);
		}

		assertEquals(1, executor.attempts.get());
		assertTrue("the error was recorded", JnEntityHttpApiErrorClient.ENTITY.exists(this.errorKey(error.entity)));
	}

	@Test
	public void anyOtherFailureGoesToTheExceptionHandler() {
		IllegalStateException error = new IllegalStateException("no connection");
		CcpJsonRepresentation handled = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("handled"), true);
		JnBusinessSendHttpRequest business = new JnBusinessSendHttpRequest(new FailingExecutor(error), e -> {
			assertSame(error, e);
			return handled;
		});

		assertEquals(handled, business.execute(this.request));
	}

	@Test
	public void theAttemptsOfARequestAreCountedUpToTheLimit() {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.url, this.url)
				.put(JnJsonCommonsFields.method, "POST")
				.put(JnJsonCommonsFields.headers, CcpOtherConstants.EMPTY_JSON.content)
				.put(JnJsonCommonsFields.apiName, "testApi")
				.put(JnJsonCommonsFields.details, "failed")
				.put(JnJsonCommonsFields.timestamp, System.currentTimeMillis())
				.put(JnJsonCommonsFields.date, new CcpTimeDecorator().getFormattedDateTime(CcpEntityExpurgableOptions.millisecond.format));
		String attempts = JnJsonCommonsFields.attempts.name();

		assertEquals(false, JnEntityHttpApiRetrySendRequest.exceededTries(key, attempts, 2));
		assertEquals(false, JnEntityHttpApiRetrySendRequest.exceededTries(key, attempts, 2));
		assertTrue(JnEntityHttpApiRetrySendRequest.exceededTries(key, attempts, 2));
	}
}
