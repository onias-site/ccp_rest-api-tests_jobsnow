package com.jn.business.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Test;

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
import com.jn.business.messages.JnMessageSenderExceptionHandler.JnErrorMessageSenderFailed;
import com.jn.entities.JnEntityJobsnowWarning;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves the policies of {@link JnMessageSenderExceptionHandler} for a failed sending: {@code THROWS} wraps and
 * rethrows; {@code LENIENT} and {@code LOG} record a warning (type, message or type when there is no message, stack
 * trace and cause) in {@code jn_jobsnow_warning} and return it.
 */
public class SenderFailurePoliciesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				(com.ccp.dependency.injection.CcpInstanceProvider<com.ccp.especifications.instant.messenger.CcpInstantMessenger>) () -> new com.jn.entities.decorators.CountingInstantMessenger());
	}

	/** A failure whose type names only this test, so its warning does not collide with real ones. */
	@SuppressWarnings("serial")
	static class SendingFailedInTest extends RuntimeException {
		SendingFailedInTest(String message) {
			super(message);
		}

		SendingFailedInTest(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private CcpJsonRepresentation warning;

	@After
	public void removeTheWarning() {
		if (this.warning != null) {
			JnEntityJobsnowWarning.ENTITY.delete(this.warning);
		}
	}

	@Test
	public void throwsWrapsTheFailure() {
		SendingFailedInTest failure = new SendingFailedInTest("down", new IllegalStateException("x"));
		try {
			JnMessageSenderExceptionHandler.THROWS.apply(failure);
			fail("the policy rethrows");
		} catch (JnErrorMessageSenderFailed e) {
			assertSame(failure, e.getCause());
		}
	}

	@Test
	public void lenientRecordsTheWarningWithItsCause() {
		this.warning = JnMessageSenderExceptionHandler.LENIENT.apply(new SendingFailedInTest("server down", new IllegalStateException("socket closed")));

		assertTrue(this.warning.getAsString(JnJsonCommonsFields.type), this.warning.getAsString(JnJsonCommonsFields.type).endsWith("SendingFailedInTest"));
		assertEquals("server down", this.warning.getAsString(JnJsonCommonsFields.message));
		assertTrue(this.warning.getAsString(JnJsonCommonsFields.stackTrace).contains("lenientRecordsTheWarningWithItsCause"));
		assertFalse(this.warning.getAsString(JnJsonCommonsFields.cause).isEmpty());
		assertTrue(JnEntityJobsnowWarning.ENTITY.exists(this.warning));
	}

	@Test
	public void logUsesTheTypeWhenThereIsNoMessageAndOmitsAnAbsentCause() {
		this.warning = JnMessageSenderExceptionHandler.LOG.apply(new SendingFailedInTest(" "));

		assertEquals(this.warning.getAsString(JnJsonCommonsFields.type), this.warning.getAsString(JnJsonCommonsFields.message));
		assertFalse(this.warning.containsAllFields(JnJsonCommonsFields.cause));
		assertTrue(JnEntityJobsnowWarning.ENTITY.exists(this.warning));
	}
}
