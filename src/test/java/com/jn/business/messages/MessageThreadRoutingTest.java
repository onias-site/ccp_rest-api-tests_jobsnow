package com.jn.business.messages;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Proves the choice of the topic of a group: the {@code messageThreadKey} put by the template picks an item of the
 * {@code messageThreadIds} of the sending parameters, in both kinds of message, and anything else goes to no topic.
 */
public class MessageThreadRoutingTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	/** Keeps the topic of each sending. */
	static class RecordingMessenger implements CcpInstantMessenger {
		final List<Long> threads = new ArrayList<>();

		public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
			return CcpOtherConstants.EMPTY_JSON;
		}

		public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
			return CcpOtherConstants.EMPTY_JSON;
		}

		public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long messageThreadId, Long replyTo, String message) {
			this.threads.add(messageThreadId);
			return CcpOtherConstants.EMPTY_JSON;
		}

		public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long messageThreadId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
			this.threads.add(messageThreadId);
			return CcpOtherConstants.EMPTY_JSON;
		}
	}

	private Long sendAndGetThread(JnInstantMessageType type, String routing) {
		RecordingMessenger messenger = new RecordingMessenger();
		CcpInstanceProvider<CcpInstantMessenger> provider = () -> messenger;
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, provider);
		CcpJsonRepresentation json = new CcpJsonRepresentation("{"
				+ "\"botName\": \"support\", \"botToken\": \"token\", \"chatId\": -1004428777693,"
				+ "\"message\": \"the content\", \"caption\": \"the caption\", \"fileName\": \"file.txt\""
				+ routing
				+ "}");
		CcpJsonRepresentation messageFields = json.getJsonPiece(JnJsonCommonsFields.message, JnJsonInstantMessengerFields.caption, JnJsonInstantMessengerFields.fileName);
		type.sendMessage(json, messageFields);
		return messenger.threads.get(0);
	}

	@Test
	public void aTextGoesToTheTopicOfItsKey() {
		String routing = ", \"messageThreadKey\": \"makeSuggestion\", \"messageThreadIds\": {\"reportBug\": 5, \"makeSuggestion\": 8}";

		assertEquals(Long.valueOf(8), this.sendAndGetThread(JnInstantMessageType.text, routing));
	}

	@Test
	public void aFileGoesToTheTopicOfItsKey() {
		String routing = ", \"messageThreadKey\": \"js\", \"messageThreadIds\": {\"vis\": 4, \"js\": 5}";

		assertEquals(Long.valueOf(5), this.sendAndGetThread(JnInstantMessageType.file, routing));
	}

	@Test
	public void aKeyWithoutTopicGoesToTheGeneralTopic() {
		String routing = ", \"messageThreadKey\": \"another\", \"messageThreadIds\": {\"vis\": 4}";

		assertEquals(Long.valueOf(0), this.sendAndGetThread(JnInstantMessageType.text, routing));
	}

	@Test
	public void aSendingWithoutKeyGoesToTheGeneralTopic() {
		String routing = ", \"messageThreadIds\": {\"vis\": 4}";

		assertEquals(Long.valueOf(0), this.sendAndGetThread(JnInstantMessageType.file, routing));
	}

	@Test
	public void aSendingToAPrivateChatHasNoTopic() {
		assertEquals(Long.valueOf(0), this.sendAndGetThread(JnInstantMessageType.text, ""));
	}

	@Test
	public void theTicketBelongsToTheOperatorWhenTheNoticeGoesToAGroup() {
		CcpJsonRepresentation json = new CcpJsonRepresentation("{\"chatId\": -1004428777693, \"operatorChatId\": 751717896}");

		assertEquals(Long.valueOf(751717896L), JnEntityInstantMessengerParametersToSend.getOperatorChatId(json));
	}

	@Test
	public void theTicketBelongsToTheChatThatReceivedTheNoticeWhenThereIsNoOperatorChat() {
		CcpJsonRepresentation json = new CcpJsonRepresentation("{\"chatId\": 751717896}");

		assertEquals(Long.valueOf(751717896L), JnEntityInstantMessengerParametersToSend.getOperatorChatId(json));
	}
}
