package com.jn.entities.decorators;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotAllowedUser;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandExplanation;
import com.jn.entities.JnEntityBotCommandName;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.entities.JnEntityBotCommandStepExplanation;
import com.jb.entities.JbEntityBotChatLanguage;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jn.entities.JnEntityBotCommandStepStartMessage;
import com.jb.entities.JbEntityBotExplanation;
import com.jb.entities.JbEntityBotUpdateId;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailReportedAsSpam;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityHttpApiErrorClient;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.entities.JnEntityHttpApiRetrySendRequest;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityJobsnowWarning;
import com.js.entities.JsEntityLoginAnswers;
import com.js.entities.JsEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginPasswordAttempts;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginSessionTokenAttempts;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenAttempts;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.js.entities.JsEntityLoginTokenRequestUnlock;
import com.jn.entities.JnEntityMessageDidNotSent;
import com.jn.entities.JnEntityRecordToReprocess;
import com.jn.entities.JnEntitySystemMessage;
import com.jn.entities.JnEntityVersionable;

/**
 * Guards against a validation rule silently lost. {@code @CcpJsonCopyFieldValidationsFrom(X.class)}
 * copies the rules of the field with the same name in {@code X}; when {@code X} does not declare the
 * field, the validation engine swallows the {@code NoSuchFieldException} and the field ends up with no
 * rule at all — nobody notices. On 2026-09-27 there were 15 such cases in jn and jb (templateId,
 * contentType, chatId, subjectType, type, token, trueTimestamp pointing to the wrong centralizer or to
 * one that did not declare them).
 */
public class ValidationRulesCopyTest {

	private static final List<Class<?>> ENTITIES = Arrays.asList(
			JbEntityBot.class, JbEntityBotAllowedUser.class, JnEntityBotCommand.class, JnEntityBotCommandExplanation.class,
			JnEntityBotCommandName.class, JnEntityBotCommandStep.class, JnEntityBotCommandStepEndMessage.class,
			JnEntityBotCommandStepExplanation.class, JbEntityBotCommandStepSession.class, JnEntityBotCommandStepStartMessage.class,
			JbEntityBotExplanation.class, JbEntityBotUpdateId.class, JbEntityBotChatLanguage.class,
			JnEntityDisposableRecord.class, JnEntityEmailMessageSent.class,
			JnEntityEmailParametersToSend.class, JnEntityEmailReportedAsSpam.class, JnEntityEmailTemplateMessage.class,
			JnEntityHttpApiErrorClient.class, JnEntityHttpApiErrorServer.class, JnEntityHttpApiRetrySendRequest.class,
			JnEntityInstantMessengerBotLocked.class, JnEntityInstantMessengerMessageSent.class,
			JnEntityInstantMessengerParametersToSend.class, JnEntityInstantMessengerTemplateMessage.class,
			JnEntityJobsnowError.class, JnEntityJobsnowWarning.class,
			JsEntityLoginAnswers.class, JsEntityLoginEmail.class, JsEntityLoginPassword.class, JsEntityLoginPasswordAttempts.class,
			JsEntityLoginSessionConflict.class, JsEntityLoginSessionTokenAttempts.class, JsEntityLoginSessionValidation.class,
			JsEntityLoginToken.class, JsEntityLoginTokenAttempts.class,
			JsEntityLoginTokenRequestResend.class, JsEntityLoginTokenRequestUnlock.class, JnEntityMessageDidNotSent.class,
			JnEntityRecordToReprocess.class, JnEntitySystemMessage.class, JnEntityVersionable.class
	);

	@Test
	public void everyCopiedFieldExistsInSourceClass() {
		List<String> lostFields = new ArrayList<>();

		for (Class<?> entityUnderTest : ENTITIES) {
			CcpEntityFieldsValidator validator = entityUnderTest.getAnnotation(CcpEntityFieldsValidator.class);
			this.findLostFields(validator.value(), lostFields);
		}

		for (Class<?> messageValidator : JnInstantMessageType.class.getDeclaredClasses()) {
			this.findLostFields(messageValidator, lostFields);
		}

		assertEquals("fields that copy rules from a class that does not declare them", new ArrayList<>(), lostFields);
	}

	private void findLostFields(Class<?> rulesClass, List<String> lostFields) {
		for (Field field : rulesClass.getDeclaredFields()) {
			CcpJsonCopyFieldValidationsFrom copyAnnotation = field.getAnnotation(CcpJsonCopyFieldValidationsFrom.class);
			if(copyAnnotation == null) {
				continue;
			}
			Class<?> sourceClass = copyAnnotation.value();
			try {
				sourceClass.getDeclaredField(field.getName());
			} catch (NoSuchFieldException e) {
				lostFields.add(rulesClass.getName() + "." + field.getName() + " <- " + sourceClass.getSimpleName());
			}
		}
	}
}
