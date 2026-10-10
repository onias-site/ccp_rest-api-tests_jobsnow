package com.jn.business.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.http.CcpHttpTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Proves how the instant messenger channel of {@link JnMessageType} handles the failures of the provider: too many
 * requests is retried up to the maximum tries and then reported, and a bot blocked by the user is recorded in
 * {@code jn_instant_messenger_bot_locked} without failing the sending, and a command to the support keeps its ticket
 * even when the delivery fails.
 */
public class InstantMessengerFailuresTest {

	/** What the fake provider does on each call. */
	private static volatile RuntimeException failure;

	private static final AtomicInteger CALLS = new AtomicInteger();

	static {
		CcpInstantMessenger provider = new CcpInstantMessenger() {
			public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
				CALLS.incrementAndGet();
				throw failure;
			}
			public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
				CALLS.incrementAndGet();
				throw failure;
			}
		};
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> provider;
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram);
	}

	/**
	 * Leaves a harmless double registered: the dependency injection is static and shared by the whole run, and a
	 * later test that notifies the support would get the failing messenger and lock the real support chat.
	 */
	@AfterClass
	public static void restoreAHarmlessMessenger() {
		CcpInstanceProvider<CcpInstantMessenger> counting = () -> new com.jn.entities.decorators.CountingInstantMessenger();
		CcpDependencyInjection.loadAllDependencies(counting);
	}

	/** A delivery refused by the provider for a reason with no special handling, such as a dropped connection. */
	private static class FailedDelivery extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private final long chatId = System.nanoTime() % 1_000_000_000L;

	private CcpJsonRepresentation message() {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, "support")
				.put(JnJsonInstantMessengerFields.chatId, this.chatId)
				.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text.name())
				.put(JnJsonCommonsFields.subjectType, "test")
				.put(JnJsonCommonsFields.message, "hello");
		return message;
	}

	@Test
	public void tooManyRequestsIsRetriedUpToTheMaximumTriesAndThenReported() {
		failure = new CcpHttpTooManyRequests();
		CALLS.set(0);
		try {
			JnMessageType.instantMessenger.execute(this.message());
			fail("the provider never accepted the message");
		} catch (JnMessageType.JnErrorUnableToSendInstantMessage e) {
			assertEquals(JnMessageType.instantMessenger.getMaxTries(), CALLS.get());
		}
	}

	/**
	 * Reproduces the skill suggestion lost on 2026-10-10: the notice to the operator failed with a dropped connection
	 * and, since the ticket was recorded only after a successful delivery, nothing reached {@code /pendingTickets}.
	 */
	@Test
	public void aCommandToTheSupportKeepsItsTicketWhenTheDeliveryFails() {
		failure = new FailedDelivery();
		String command = "/reviewSkillSuggestion delivery.failure@teste.com KIPREV";
		CcpJsonRepresentation commandToTheSupport = this.message().put(JnJsonCommonsFields.message, command);
		CcpJsonRepresentation ticket = CcpOtherConstants.EMPTY_JSON
				.put(JnEntitySupportPendingCommand.Fields.botName, "support")
				.put(JnEntitySupportPendingCommand.Fields.chatId, this.chatId)
				.put(JnEntitySupportPendingCommand.Fields.command, command);

		try {
			JnMessageType.instantMessenger.execute(commandToTheSupport);
			fail("the provider never accepted the message");
		} catch (FailedDelivery e) {
			assertTrue(JnEntitySupportPendingCommand.ENTITY.exists(ticket));
		} finally {
			JnEntitySupportPendingCommand.ENTITY.delete(ticket);
		}
	}

	@Test
	public void aBotBlockedByTheUserIsRecordedAndTheSendingGoesOn() {
		failure = new CcpErrorInstantMessageThisBotWasBlockedByThisUser("support");

		CcpJsonRepresentation result = JnMessageType.instantMessenger.execute(this.message());

		assertEquals(this.chatId, (long) result.getAsLongNumber(JnJsonInstantMessengerFields.chatId));
		assertTrue(JnEntityInstantMessengerBotLocked.ENTITY.exists(this.message()));
	}
}
