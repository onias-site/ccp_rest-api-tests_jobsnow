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
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Proves that the text and the file sendings read {@code replyTo} as a number. After going through JSON the value comes
 * back as a double; until 2026-10-06 the file sending cast it to {@code Long} and failed with
 * {@code ClassCastException}.
 */
public class ReplyToAfterJsonTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** Keeps the replyTo of each sending. */
	static class RecordingMessenger implements CcpInstantMessenger {
		final List<Long> replies = new ArrayList<>();

		public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
			this.replies.add(replyTo);
			return CcpOtherConstants.EMPTY_JSON;
		}

		public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
			this.replies.add(replyTo);
			return CcpOtherConstants.EMPTY_JSON;
		}
	}

	private RecordingMessenger messengerReplaced() {
		RecordingMessenger messenger = new RecordingMessenger();
		CcpInstanceProvider<CcpInstantMessenger> provider = () -> messenger;
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), provider);
		return messenger;
	}

	/** The message as it comes after a trip through JSON: numbers are doubles. */
	private CcpJsonRepresentation messageThroughJson(String replyTo) {
		return new CcpJsonRepresentation("{"
				+ "\"botName\": \"support\", \"botToken\": \"token\", \"chatId\": 751717896,"
				+ "\"message\": \"the content\", \"caption\": \"the caption\", \"fileName\": \"file.txt\""
				+ replyTo
				+ "}");
	}

	private Long sendAndGetReplyTo(JnInstantMessageType type, CcpJsonRepresentation json) {
		RecordingMessenger messenger = this.messengerReplaced();
		CcpJsonRepresentation messageFields = json.getJsonPiece(JnJsonCommonsFields.message, JnJsonInstantMessengerFields.caption, JnJsonInstantMessengerFields.fileName);
		type.sendMessage(json, messageFields);
		return messenger.replies.get(0);
	}

	@Test
	public void aFileAnsweringAMessageThatCameThroughJsonIsSent() {
		CcpJsonRepresentation json = this.messageThroughJson(", \"replyTo\": 363");

		assertEquals(Long.valueOf(363), this.sendAndGetReplyTo(JnInstantMessageType.file, json));
	}

	@Test
	public void aTextAnsweringAMessageThatCameThroughJsonIsSent() {
		CcpJsonRepresentation json = this.messageThroughJson(", \"replyTo\": 363");

		assertEquals(Long.valueOf(363), this.sendAndGetReplyTo(JnInstantMessageType.text, json));
	}

	@Test
	public void aFileAnsweringNoMessageGoesWithZero() {
		CcpJsonRepresentation json = this.messageThroughJson("");

		assertEquals(Long.valueOf(0), this.sendAndGetReplyTo(JnInstantMessageType.file, json));
	}
}
