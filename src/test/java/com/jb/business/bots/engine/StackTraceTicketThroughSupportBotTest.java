package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jb.entities.JbEntityPendingTickets;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.json.fields.validation.JnApiOrigins;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.js.json.fields.validation.JsContactUsTypes;

/**
 * An unhandled error from its record in {@code jn_jobsnow_error} to its handling by the support operator through
 * {@code /solveStackTrace <errorId>}: the ticket of priority 0, the notice in the group of the stack traces, the error
 * shown, closing the ticket and going to the next one.
 */
public class StackTraceTicketThroughSupportBotTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram
		);
	}

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final Long STACK_TRACES_GROUP = -1004489667665L;

	private static final String ERROR_TYPE = "java.lang.IllegalStateException";

	private static final String ERROR_FRAME = "at com.vis.Teste.metodo(Teste.java:1)";

	private static final String ERROR_TEXT = "erro de teste do ticket";

	private final String unique = "hash" + System.nanoTime();

	/**
	 * Saves what the command needs again so that the index follows what is declared in java (the create operation of the
	 * initial records does not overwrite what is already there).
	 */
	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(JbEntityBot.INSTANCE, JnEntityBotCommand.INSTANCE, JnEntityBotCommandStep.INSTANCE, JnEntityBotCommandStepEndMessage.INSTANCE, JnEntityJobsnowError.INSTANCE, JnEntityInstantMessengerParametersToSend.INSTANCE, JnEntityInstantMessengerTemplateMessage.INSTANCE);
		for (CcpEntityConfigurator configurator : configurators) {
			List<CcpBulkItem> records = configurator.getFirstRecordsToInsert();
			for (CcpBulkItem record : records) {
				// the subscribers of a write event are appended by a script, not written as a record
				boolean scripted = record.operation != CcpBulkEntityOperationType.create;
				if(scripted) {
					continue;
				}
				record.entity.save(record.json);
			}
		}
	}

	@Before
	public void clearTheBotSession() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		JbEntityBotCommandStepSession.ENTITY.delete(session);
		TELEGRAM.sentMessages.clear();
	}

	/**
	 * Leaves no ticket of the test behind in the {@code /pendingTickets} of the real support chat.
	 */
	@After
	public void discardTheTicketOfTheTest() {
		String ticket = this.ticket();
		CcpJsonRepresentation pendingCommand = this.pendingCommand(ticket);
		CcpJsonRepresentation pendingTicket = this.pendingTicket(ticket);
		JnEntitySupportPendingCommand.ENTITY.delete(pendingCommand);
		JbEntityPendingTickets.ENTITY.delete(pendingTicket);
		CcpJsonRepresentation error = this.error();
		String errorId = JnEntityJobsnowError.ENTITY.calculateId(error);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		String entityName = JnEntityJobsnowError.ENTITY.name();
		crud.delete(entityName, errorId);
	}

	/**
	 * The error goes to the operator as a command of priority 0, in the private chat, and the file with the stack trace
	 * goes to the group.
	 */
	@Test
	public void anErrorOpensATicketOfPriorityZero() {

		JnEntityJobsnowError.ENTITY.save(this.error());
		String ticket = this.ticket();

		CcpJsonRepresentation sentCommand = TELEGRAM.lastMessageFor(SUPPORT_CHAT);
		assertEquals(ticket, sentCommand.getAsString(JnJsonCommonsFields.message));

		CcpJsonRepresentation pendingCommand = JnEntitySupportPendingCommand.ENTITY.getOneById(this.pendingCommand(ticket));
		assertEquals(0, pendingCommand.getAsIntegerNumber(JnEntitySupportPendingCommand.Fields.priority).intValue());

		CcpJsonRepresentation sentFile = TELEGRAM.lastMessageFor(STACK_TRACES_GROUP);
		assertEquals(ERROR_TYPE, sentFile.getAsString(JnJsonCommonsFields.message));
	}

	/**
	 * No other kind of ticket takes the priority of the errors.
	 */
	@Test
	public void noTicketOfTheContactUsIsAsUrgentAsAnError() {
		for (JsContactUsTypes type : JsContactUsTypes.values()) {
			assertTrue(type.name(), type.priority > 0);
		}
	}

	/**
	 * The operator sees the error, an answer not understood is asked again, and solving it closes the ticket and keeps
	 * the record.
	 */
	@Test
	public void operatorSolvesTheError() {

		JnEntityJobsnowError.ENTITY.save(this.error());
		String ticket = this.ticket();

		String shown = this.operatorTypes(ticket);
		assertTrue(shown, shown.startsWith("Erro não tratado"));
		assertTrue(shown, shown.contains("Origem: vis"));
		assertTrue(shown, shown.contains("Tipo: " + ERROR_TYPE));
		assertTrue(shown, shown.contains("Mensagem: " + ERROR_TEXT));
		assertTrue(shown, shown.contains(ERROR_FRAME));

		String notUnderstood = this.operatorTypes("talvez");
		assertTrue(notUnderstood, notUnderstood.startsWith("Não entendi a resposta"));
		assertTrue(this.isPendingTicket(ticket));

		String solved = this.operatorTypes("1");
		assertTrue(solved, solved.contains("foi marcado como resolvido"));
		assertFalse(this.isPendingTicket(ticket));

		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		String errorId = JnEntityJobsnowError.ENTITY.calculateId(this.error());
		String entityName = JnEntityJobsnowError.ENTITY.name();
		assertTrue(crud.exists(entityName, errorId));
	}

	/**
	 * Going to the next ticket keeps this one open.
	 */
	@Test
	public void operatorLeavesTheTicketOpen() {

		JnEntityJobsnowError.ENTITY.save(this.error());
		String ticket = this.ticket();

		this.operatorTypes(ticket);
		this.operatorTypes("2");

		assertTrue(this.isPendingTicket(ticket));
	}

	/**
	 * An id that does not exist ends the session with a notice.
	 */
	@Test
	public void anUnknownErrorEndsTheSession() {

		String notFound = this.operatorTypes("/solveStackTrace " + this.unique);

		assertTrue(notFound, notFound.startsWith("Não existe o erro " + this.unique));
	}

	private CcpJsonRepresentation error() {
		CcpJsonRepresentation error = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityJobsnowError.Fields.stackTraceHash, this.unique)
				.put(JnEntityJobsnowError.Fields.type, ERROR_TYPE)
				.put(JnEntityJobsnowError.Fields.message, ERROR_TEXT + " " + this.unique)
				.put(JnEntityJobsnowError.Fields.origin, JnApiOrigins.vis)
				.put(JnEntityJobsnowError.Fields.stackTrace, Arrays.asList(ERROR_FRAME));
		return error;
	}

	private String ticket() {
		String errorId = JnEntityJobsnowError.ENTITY.calculateId(this.error());
		return "/solveStackTrace " + errorId;
	}

	private CcpJsonRepresentation pendingCommand(String command) {
		CcpJsonRepresentation pendingCommand = CcpOtherConstants.EMPTY_JSON
				.put(JnEntitySupportPendingCommand.Fields.botName, JbBotType.support)
				.put(JnEntitySupportPendingCommand.Fields.chatId, SUPPORT_CHAT)
				.put(JnEntitySupportPendingCommand.Fields.command, command);
		return pendingCommand;
	}

	private CcpJsonRepresentation pendingTicket(String command) {
		CcpJsonRepresentation pendingTicket = CcpOtherConstants.EMPTY_JSON
				.put(JbEntityPendingTickets.Fields.botName, JbBotType.support)
				.put(JbEntityPendingTickets.Fields.chatId, SUPPORT_CHAT)
				.put(JbEntityPendingTickets.Fields.ticket, command);
		return pendingTicket;
	}

	private boolean isPendingTicket(String command) {
		boolean inTheInbox = JnEntitySupportPendingCommand.ENTITY.exists(this.pendingCommand(command));
		boolean inTheList = JbEntityPendingTickets.ENTITY.exists(this.pendingTicket(command));
		boolean pending = inTheInbox || inTheList;
		return pending;
	}

	private String operatorTypes(String text) {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT)
				.put(JnJsonCommonsFields.message, text)
				.put(JbBotEngine.Fields.message_id, 1);
		JbBotType.support.getBot().execute(message);
		CcpJsonRepresentation reply = TELEGRAM.lastMessageFor(SUPPORT_CHAT);
		String replyText = reply.getAsString(JnJsonCommonsFields.message);
		return replyText;
	}
}
