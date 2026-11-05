package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.entity.decorators.CountingMensageriaSender;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.jn.business.bots.JnBusinessHandleBotUpdate;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotExplanation;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jb.entities.JbEntityBotUpdateId;
import com.jb.instant.messenger.reader.JbInstantMessengerMessageReader;
import com.jn.entities.JnEntityBotCommandExplanation;
import com.jn.entities.JnEntityBotCommandName;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * An update pushed by Telegram reaches the bot in the same form the reading by polling delivers it, and the delivery that
 * Telegram repeats runs nothing. The database is the real Elasticsearch; Telegram is a test double.
 */
public class JbBusinessReceiveBotUpdateTest {

	private static final SwitchableInstantMessenger TELEGRAM = new SwitchableInstantMessenger();

	private static final FakeEmailSender EMAIL_INBOX = new FakeEmailSender();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailProvider> email = () -> EMAIL_INBOX;

		// counts what is published and runs it at once, as the synchronous messaging does: it proves the queue of the command
		CcpInstanceProvider<CcpMensageriaSender> countingMensageria = () -> CountingMensageriaSender.INSTANCE;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				countingMensageria,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
	}

	private static final Long SUPPORT_CHAT = 751717896L;

	/** The queues of production, relative to this module. */
	private static final String TOPICS_FILE = "../documentation/deploy/topicos.tsv";

	/** The ack deadline of the queues of the bot, in seconds: a command may run a complex query. */
	private static final int ACK_DEADLINE_OF_A_COMMAND = 600;

	/** The provider of the sender of the consumers of the bots, in the column {@code senderClass} of {@code topicos.tsv}. */
	private static final String PUB_SUB_SENDER = "com.ccp.implementations.mensageria.sender.gcp.pubsub.CcpGcpPubSubMensageriaSender";

	/** The threads of the consumer of each command, by bot. */
	private static final CcpJsonRepresentation THREADS_OF_THE_COMMANDS = CcpOtherConstants.EMPTY_JSON
			.put(JbBotType.support, 1)
			.put(JbBotType.user, 4);

	private static final String SUPPORT = JbBotType.support.name();

	/** The answers of the default commands are seeds: they are saved again so that the index follows the java. */
	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(JbEntityBot.INSTANCE, JnEntityBotCommandName.INSTANCE, JbEntityBotExplanation.INSTANCE, JnEntityBotCommandExplanation.INSTANCE);
		for (CcpEntityConfigurator configurator : configurators) {
			List<CcpBulkItem> records = configurator.getFirstRecordsToInsert();
			for (CcpBulkItem record : records) {
				boolean scripted = record.operation != CcpBulkEntityOperationType.create;
				if (scripted) {
					continue;
				}
				record.entity.save(record.json);
			}
		}
	}

	@Before
	public void clearTheMessagesAndTheOffset() {
		TELEGRAM.sentMessages.clear();
		TELEGRAM.failing = false;
		JbEntityBotUpdateId.ENTITY.delete(this.offsetKey());
	}

	@After
	public void clearTheOffset() {
		JbEntityBotUpdateId.ENTITY.delete(this.offsetKey());
	}

	private CcpJsonRepresentation offsetKey() {
		CcpJsonRepresentation offsetKey = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.botName, SUPPORT);
		return offsetKey;
	}

	private long savedOffset() {
		CcpJsonRepresentation offset = JbEntityBotUpdateId.ENTITY.getOneById(this.offsetKey());
		long savedOffset = offset.getAsLongNumber(JbEntityBotUpdateId.Fields.updateId);
		return savedOffset;
	}

	private CcpJsonRepresentation updateWithMessage(long updateId, String typedText) {
		CcpJsonRepresentation update = new CcpJsonRepresentation("{"
				+ "\"botName\": \"support\","
				+ "\"update_id\": " + updateId + ","
				+ "\"message\": {"
				+ "\"message_id\": 363,"
				+ "\"date\": 1790121741,"
				+ "\"from\": {\"id\": " + SUPPORT_CHAT + ", \"username\": \"OniasJr\"},"
				+ "\"chat\": {\"id\": " + SUPPORT_CHAT + "},"
				+ "\"text\": \"" + typedText + "\""
				+ "}}");
		return update;
	}

	private CcpJsonRepresentation updateWithoutMessage(long updateId) {
		CcpJsonRepresentation update = new CcpJsonRepresentation("{"
				+ "\"botName\": \"support\","
				+ "\"update_id\": " + updateId + ","
				+ "\"edited_message\": {\"message_id\": 363, \"chat\": {\"id\": " + SUPPORT_CHAT + "}, \"text\": \"edited\"}"
				+ "}");
		return update;
	}

	@Test
	public void theMessageOfTheUpdateReachesTheBot() {
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateWithMessage(1000L, "/chatId"));

		assertEquals(1, TELEGRAM.sentMessages.size());
		assertEquals("" + SUPPORT_CHAT, TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message));
		assertEquals(1001L, this.savedOffset());
	}

	/** The body comes from Spring with the ids as Integer or Long, and from Gson as double: both must reach the same chat. */
	@Test
	public void theIdsOfTheUpdateAreReadWhateverTheirNumericType() {
		CcpJsonRepresentation update = this.updateWithMessage(1010L, "/chatId");
		CcpJsonRepresentation message = update.getInnerJson(JnJsonInstantMessengerFields.message);
		CcpJsonRepresentation chatWithALong = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.id, SUPPORT_CHAT);
		CcpJsonRepresentation messageWithALong = message.put(JbBusinessReceiveBotUpdate.Fields.chat, chatWithALong.content);
		CcpJsonRepresentation updateWithALong = update.put(JnJsonInstantMessengerFields.message, messageWithALong.content);

		JbBusinessReceiveBotUpdate.INSTANCE.execute(updateWithALong);

		assertEquals("" + SUPPORT_CHAT, TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message));
	}

	@Test
	public void aRepeatedDeliveryDoesNotRunTheBotAgain() {
		CcpJsonRepresentation update = this.updateWithMessage(2000L, "/chatId");

		JbBusinessReceiveBotUpdate.INSTANCE.execute(update);
		JbBusinessReceiveBotUpdate.INSTANCE.execute(update);

		assertEquals(1, TELEGRAM.sentMessages.size());
		assertEquals(2001L, this.savedOffset());
	}

	/** A bot that fails must not be run again by the redelivery of Telegram, nor hold the updates behind it. */
	@Test
	public void aFailureOfTheBotIsNotRepeatedByTheRedelivery() {
		CcpJsonRepresentation update = this.updateWithMessage(3000L, "/chatId");
		TELEGRAM.failing = true;

		try {
			JbBusinessReceiveBotUpdate.INSTANCE.execute(update);
			fail("the failure of the bot must reach the API, which records it");
		} catch (IllegalStateException e) {
			assertEquals(3001L, this.savedOffset());
		}

		TELEGRAM.failing = false;
		JbBusinessReceiveBotUpdate.INSTANCE.execute(update);
		assertTrue(TELEGRAM.sentMessages.isEmpty());

		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateWithMessage(3001L, "/chatId"));
		assertEquals(1, TELEGRAM.sentMessages.size());
	}

	/** After a week without updates Telegram picks the next id at random: a lower id must not be taken for an old one. */
	@Test
	public void anIdLowerThanTheOffsetIsANewUpdate() {
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateWithMessage(900000L, "/chatId"));
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateWithMessage(5L, "/chatId"));

		assertEquals(2, TELEGRAM.sentMessages.size());
		assertEquals(6L, this.savedOffset());
	}

	@Test
	public void anUpdateWithoutMessageOnlyMovesTheOffset() {
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateWithoutMessage(4000L));

		assertTrue(TELEGRAM.sentMessages.isEmpty());
		assertEquals(4001L, this.savedOffset());
	}

	/** Both ways of receiving share the offset: the reading by polling starts after what the webhook received. */
	@Test
	public void theOffsetIsTheOneTheReadingByPollingUses() {
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateWithoutMessage(4500L));

		Long offsetOfThePolling = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPPORT);
		assertEquals(4501L, offsetOfThePolling.longValue());
	}

	/** How many messages went to the queue of the command so far. */
	private long publishedToTheQueueOf(String commandName) {
		String topic = CcpMensageriaReceiver.getQualifiedTopic(JbBusinessRunBotCommand.class, SUPPORT, commandName);
		String noOperation = CcpOtherConstants.EMPTY_STRING.getValue();
		long howMany = CountingMensageriaSender.howMany(topic, noOperation);
		return howMany;
	}

	/** Since 2026-11-04 the update only chooses the queue: the bot runs in the queue of the command typed. */
	@Test
	public void theMessageGoesToTheQueueOfItsCommand() {
		String chatId = JbDefaultBotCommandStep.chatId.name();
		long publishedBefore = this.publishedToTheQueueOf(chatId);

		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateWithMessage(6000L, "/chatId"));

		assertEquals(publishedBefore + 1, this.publishedToTheQueueOf(chatId));
		assertEquals(1, TELEGRAM.sentMessages.size());
	}

	/** A text that is no command, in a chat without session, goes to the queue of the list of commands. */
	@Test
	public void aTextWithoutSessionGoesToTheQueueOfTheListOfCommands() {
		JbEntityBotCommandStepSession.ENTITY.delete(this.sessionKey());
		String showAllCommands = JbDefaultBotCommandStep.showAllCommands.name();
		long publishedBefore = this.publishedToTheQueueOf(showAllCommands);

		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateWithMessage(6100L, "hello"));

		assertEquals(publishedBefore + 1, this.publishedToTheQueueOf(showAllCommands));
	}

	private CcpJsonRepresentation sessionKey() {
		CcpJsonRepresentation sessionKey = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, SUPPORT)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		return sessionKey;
	}

	/**
	 * Every command of every bot needs its queue in production: a message to a topic that does not exist fails in the
	 * dispatcher. Only the queue of each bot (the dispatcher) names the Pub/Sub sender, without which it would run the
	 * command in its own thread; the commands keep the synchronous one (decided on 2026-11-05). Threads: 1 in
	 * each command of the support bot, 4 in each one of the user bot, 1 in the queue of each bot (decided on 2026-11-05).
	 */
	@Test
	public void everyCommandOfEveryBotHasItsQueueInTopicos() {
		CcpStringDecorator topicsPath = new CcpStringDecorator(TOPICS_FILE);
		CcpFileDecorator topicsFile = topicsPath.file();
		List<String> lines = topicsFile.getLines();
		List<String> missingLines = new ArrayList<>();
		for (JbBotType bot : JbBotType.values()) {
			List<String> commandNames = this.commandsOf(bot);
			assertFalse(commandNames.isEmpty());
			String botName = bot.name();
			int threads = THREADS_OF_THE_COMMANDS.getAsIntegerNumber(bot);
			for (String commandName : commandNames) {
				String topic = CcpMensageriaReceiver.getQualifiedTopic(JbBusinessRunBotCommand.class, botName, commandName);
				// no senderClass: the command keeps the synchronous sender, and what it publishes runs in its own thread
				String expectedLine = topic + "\t" + threads + "\t" + ACK_DEADLINE_OF_A_COMMAND;
				boolean missing = false == lines.contains(expectedLine);
				if (missing) {
					missingLines.add(expectedLine);
				}
			}
			String queueOfTheBot = CcpMensageriaReceiver.getQualifiedTopic(JnBusinessHandleBotUpdate.class, botName);
			String dispatcher = queueOfTheBot + "\t1\t" + ACK_DEADLINE_OF_A_COMMAND + "\t" + PUB_SUB_SENDER;
			boolean dispatcherMissing = false == lines.contains(dispatcher);
			if (dispatcherMissing) {
				missingLines.add(dispatcher);
			}
		}
		assertTrue("missing in topicos.tsv: " + missingLines, missingLines.isEmpty());
	}

	/**
	 * The commands of a bot: the configured ones (records of {@code JnEntityBotCommand} with its name) and the default
	 * ones, which every bot has. A command record without name is skipped: a session without command goes to
	 * {@code showAllCommands}.
	 */
	private List<String> commandsOf(JbBotType bot) {
		List<String> commandNames = new ArrayList<>(bot.getConfiguredCommands());
		Set<String> engineCommands = JbBotEngine.INSTANCE.allCommands.keySet();
		for (JbDefaultBotCommandStep defaultCommand : JbDefaultBotCommandStep.values()) {
			String defaultCommandName = defaultCommand.name();
			boolean isCommand = engineCommands.contains(defaultCommandName);
			if (isCommand) {
				commandNames.add(defaultCommandName);
			}
		}
		commandNames.removeIf(String::isEmpty);
		return commandNames;
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JbBusinessReceiveBotUpdate.INSTANCE.execute(null);
	}
}
