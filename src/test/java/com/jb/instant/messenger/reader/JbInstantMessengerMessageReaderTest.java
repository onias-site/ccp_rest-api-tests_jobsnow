package com.jb.instant.messenger.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.entities.JbEntityBotUpdateId;
import com.jb.instant.messenger.reader.JbInstantMessengerMessageReader.JsonFieldNames;
import com.jn.business.messages.JnMessageType;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.business.messages.JnMessageType.JnBotType;

/**
 * Tests reading the messages received by the support bot. The Telegram api is replaced by a fake
 * {@code CcpHttpRequester} injected into {@code CcpDependencyInjection}, so that the tests exercise
 * the interpretation of {@code getUpdates} and the status mapping of {@code CcpHttpHandler} without
 * depending on the network. The offset, which is saved in the {@code JbEntityBotUpdateId} entity, is
 * kept by an in-memory {@code CcpCrud}.
 */
public class JbInstantMessengerMessageReaderTest {

	/**
	 * The cache is the null one, not the map one: the map one also keeps state in a static field, and
	 * with no key to clear it the offset of one test would remain visible to the next one even after the
	 * database is emptied. What is measured here is the saving of the offset, not the cache.
	 */
	static {
		CcpInstanceProvider<CcpCrud> inMemoryCrud = () -> new FakeCrud();
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.mock, inMemoryCrud);
	}

	@Before
	public void clearDatabase() {
		FakeCrud.clear();
	}

	/**
	 * The reader identifies the bot by name, not by the enum, so the tests convert the
	 * {@link JnBotType} only once here, keeping the enum as the source of truth.
	 */
	private static final String SUPPORT = JnBotType.support.name();

	private static final String USER = JnBotType.user.name();

	private static final String TWO_MESSAGES_AND_ONE_EDIT = "{\"ok\":true,\"result\":["
			+ "{\"update_id\":100,\"message\":{\"message_id\":11,\"date\":1700000000,\"from\":{\"id\":55,\"username\":\"onias\"},\"chat\":{\"id\":55},\"text\":\"/solveLoginTokenTicket\"}},"
			+ "{\"update_id\":101,\"edited_message\":{\"message_id\":11,\"chat\":{\"id\":55},\"text\":\"edited text\"}},"
			+ "{\"update_id\":102,\"message\":{\"message_id\":12,\"date\":1700000060,\"from\":{\"id\":66,\"username\":\"maria\"},\"chat\":{\"id\":66},\"text\":\"good morning\"}}"
			+ "]}";

	private static final String NO_MESSAGES = "{\"ok\":true,\"result\":[]}";

	private static final String NOT_OK_RESPONSE = "{\"ok\":false,\"error_code\":401,\"description\":\"Unauthorized\"}";

	private static final String BOT_BLOCKED = "{\"ok\":false,\"error_code\":403,\"description\":\"Forbidden: bot was blocked by the user\"}";

	private static final String TOO_MANY_REQUESTS = "{\"ok\":false,\"error_code\":429,\"description\":\"Too Many Requests\"}";

	// ── reading the support bot messages ──────────────────────────────────────

	@Test
	public void readSupportBotMessagesTest() {

		this.telegramResponding(200, TWO_MESSAGES_AND_ONE_EDIT);

		List<CcpJsonRepresentation> messages = JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPPORT, 0L, 0);

		assertEquals(2, messages.size());

		CcpJsonRepresentation firstMessage = messages.get(0);

		assertEquals("support", firstMessage.getAsString(JsonFieldNames.botName));
		// the text leaves the reader in the `message` field; the one that renames it to `typedValue` is
		// JbBotEngine.Bot, already inside the bot's handling flow
		assertEquals("/solveLoginTokenTicket", firstMessage.getAsString(JsonFieldNames.message));
		assertEquals("onias", firstMessage.getAsString(JsonFieldNames.userName));
		assertEquals(55L, firstMessage.getAsLongNumber(JsonFieldNames.chatId).longValue());
		assertEquals(11L, firstMessage.getAsLongNumber(JsonFieldNames.message_id).longValue());
		assertEquals(100L, firstMessage.getAsLongNumber(JsonFieldNames.updateId).longValue());
		assertEquals(1700000000L, firstMessage.getAsLongNumber(JsonFieldNames.sentAt).longValue());

		CcpJsonRepresentation secondMessage = messages.get(1);

		assertEquals("good morning", secondMessage.getAsString(JsonFieldNames.message));
		assertEquals(66L, secondMessage.getAsLongNumber(JsonFieldNames.chatId).longValue());
		assertEquals(102L, secondMessage.getAsLongNumber(JsonFieldNames.updateId).longValue());
	}

	@Test
	public void updateWithoutMessageIsIgnoredTest() {

		this.telegramResponding(200, TWO_MESSAGES_AND_ONE_EDIT);

		List<CcpJsonRepresentation> messages = JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPPORT, 0L, 0);

		boolean editWasReturned = messages.stream()
				.anyMatch(x -> 101L == x.getAsLongNumber(JsonFieldNames.updateId).longValue());

		assertFalse(editWasReturned);
	}

	@Test
	public void noMessagesToReadTest() {

		this.telegramResponding(200, NO_MESSAGES);

		List<CcpJsonRepresentation> messages = JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPPORT, 0L, 0);

		assertTrue(messages.isEmpty());
	}

	@Test
	public void offsetAdvancesToNotRereadSameMessagesTest() {

		FakeHttpRequester telegram = this.telegramResponding(200, TWO_MESSAGES_AND_ONE_EDIT, NO_MESSAGES);

		this.saveOffset(SUPPORT, 0L);

		FakeBot firstRead = this.readNewMessages(JnBotType.support);

		FakeBot secondRead = this.readNewMessages(JnBotType.support);

		CcpJsonRepresentation secondRequest = new CcpStringDecorator(telegram.lastRequest).json();

		assertEquals(2, firstRead.received.size());
		assertEquals(103L, secondRequest.getAsLongNumber(JsonFieldNames.offset).longValue());
		assertTrue(secondRead.received.isEmpty());
	}

	// ── offset saved in the JbEntityBotUpdateId entity ────────────────────────

	@Test
	public void savedOffsetIsRetrievedTest() {

		this.saveOffset(SUPPORT, 500L);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPPORT);

		assertEquals(500L, offset.longValue());
	}

	@Test
	public void incrementedOffsetIsSavedWhenReadingNewMessagesTest() {

		this.telegramResponding(200, TWO_MESSAGES_AND_ONE_EDIT);

		this.saveOffset(SUPPORT, 0L);

		this.readNewMessages(JnBotType.support);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPPORT);

		assertEquals(103L, offset.longValue());
	}

	@Test
	public void offsetIsNotSavedWhenThereAreNoNewMessagesTest() {

		this.telegramResponding(200, NO_MESSAGES);

		this.saveOffset(SUPPORT, 777L);

		this.readNewMessages(JnBotType.support);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPPORT);

		assertEquals(777L, offset.longValue());
	}

	@Test
	public void eachBotHasItsOwnOffsetTest() {

		this.saveOffset(SUPPORT, 111L);
		this.saveOffset(USER, 222L);

		Long supportOffset = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPPORT);
		Long userOffset = JbInstantMessengerMessageReader.INSTANCE.getOffset(USER);

		assertEquals(111L, supportOffset.longValue());
		assertEquals(222L, userOffset.longValue());
	}

	@Test
	public void supportBotTokenTest() {

		String botToken = JbInstantMessengerMessageReader.INSTANCE.getBotToken(SUPPORT);

		assertFalse(botToken.trim().isEmpty());
	}

	// ── handling of the statuses returned by the api ──────────────────────────

	@Test(expected = JbErrorUnableToReadInstantMessages.class)
	public void notOkResponseTest() {
		this.telegramResponding(200, NOT_OK_RESPONSE);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPPORT, 0L, 0);
	}

	@Test(expected = CcpErrorInstantMessageThisBotWasBlockedByThisUser.class)
	public void botBlockedByUserTest() {
		this.telegramResponding(403, BOT_BLOCKED);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPPORT, 0L, 0);
	}

	@Test(expected = CcpHttpTooManyRequests.class)
	public void tooManyRequestsTest() {
		this.telegramResponding(429, TOO_MANY_REQUESTS);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPPORT, 0L, 0);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(null, 0L, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(SUPPORT, null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(SUPPORT, 0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(null, 0L, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPPORT, null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPPORT, 0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readNewMessagesMessageReaderNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(0, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readNewMessagesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(null, new FakeBot(JnMessageType.JnBotType.support));
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOffsetBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getOffset(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveOffsetBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.saveOffset(null, 0L, new ArrayList<>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveOffsetMessagesNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.saveOffset(SUPPORT, 0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getBotTokenBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getBotToken(null);
	}

	// ── replaced Telegram api ─────────────────────────────────────────────────

	private FakeHttpRequester telegramResponding(int httpStatus, String... responses) {
		FakeHttpRequester telegram = new FakeHttpRequester(httpStatus, responses);
		CcpInstanceProvider<CcpHttpRequester> provider = () -> telegram;
		CcpDependencyInjection.loadAllDependencies(provider);
		return telegram;
	}

	/**
	 * Saves the bot's offset directly in the entity that keeps it.
	 *
	 * <p>This scenario cannot be set up by calling {@code saveOffset} with an empty list: without new
	 * messages it returns the received offset without saving anything — and that is the right behavior,
	 * it is what {@link #offsetIsNotSavedWhenThereAreNoNewMessagesTest()} checks. Using it to prepare the
	 * scenario made the tests claim to have saved a value that never moved.
	 */
	private void saveOffset(String botType, long offset) {
		CcpJsonRepresentation botOffset = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, botType)
				.put(JbEntityBotUpdateId.Fields.updateId, offset);

		JbEntityBotUpdateId.ENTITY.save(botOffset);
	}

	/**
	 * Reads the support bot's new messages, returning the test double that received them, since
	 * {@code readNewMessages} hands each message to the {@code CcpBusiness} instead of returning them.
	 */
	private FakeBot readNewMessages(JnBotType botType) {
		FakeBot bot = new FakeBot(botType);
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(0, bot);
		return bot;
	}



	// ── replaced database ─────────────────────────────────────────────────────


}
