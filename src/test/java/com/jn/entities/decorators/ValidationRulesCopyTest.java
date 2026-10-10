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
import com.jb.entities.JbEntityBotCommand;
import com.jb.entities.JbEntityBotCommandExplanation;
import com.jb.entities.JbEntityBotCommandName;
import com.jb.entities.JbEntityBotCommandStep;
import com.jb.entities.JbEntityBotCommandStepEndMessage;
import com.jb.entities.JbEntityBotCommandStepExplanation;
import com.jb.entities.JbEntityBotChatLanguage;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jb.entities.JbEntityBotCommandStepStartMessage;
import com.jb.entities.JbEntityBotExplanation;
import com.jb.entities.JbEntityBotUpdateId;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.entities.JnEntityAsyncTask;
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
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionTokenAttempts;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginStats;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenAttempts;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
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
			JbEntityBot.class, JbEntityBotAllowedUser.class, JbEntityBotCommand.class, JbEntityBotCommandExplanation.class,
			JbEntityBotCommandName.class, JbEntityBotCommandStep.class, JbEntityBotCommandStepEndMessage.class,
			JbEntityBotCommandStepExplanation.class, JbEntityBotCommandStepSession.class, JbEntityBotCommandStepStartMessage.class,
			JbEntityBotExplanation.class, JbEntityBotUpdateId.class, JbEntityBotChatLanguage.class,
			JnEntityAsyncTask.class,
			JnEntityDisposableRecord.class, JnEntityEmailMessageSent.class,
			JnEntityEmailParametersToSend.class, JnEntityEmailReportedAsSpam.class, JnEntityEmailTemplateMessage.class,
			JnEntityHttpApiErrorClient.class, JnEntityHttpApiErrorServer.class, JnEntityHttpApiRetrySendRequest.class,
			JnEntityInstantMessengerBotLocked.class, JnEntityInstantMessengerMessageSent.class,
			JnEntityInstantMessengerParametersToSend.class, JnEntityInstantMessengerTemplateMessage.class,
			JnEntityJobsnowError.class, JnEntityJobsnowWarning.class,
			JnEntityLoginAnswers.class, JnEntityLoginEmail.class, JnEntityLoginPassword.class, JnEntityLoginPasswordAttempts.class,
			JnEntityLoginSessionConflict.class, JnEntityLoginSessionTokenAttempts.class, JnEntityLoginSessionValidation.class,
			JnEntityLoginStats.class, JnEntityLoginToken.class, JnEntityLoginTokenAttempts.class,
			JnEntityLoginTokenRequestResend.class, JnEntityLoginTokenRequestUnlock.class, JnEntityMessageDidNotSent.class,
			JnEntityRecordToReprocess.class, JnEntitySystemMessage.class, JnEntityVersionable.class
	);

	@Test
	public void everyCopiedFieldExistsInSourceClass() {
		List<String> lostFields = new ArrayList<>();

		for (Class<?> entityUnderTest : ENTITIES) {
			CcpEntityFieldsValidator validator = entityUnderTest.getAnnotation(CcpEntityFieldsValidator.class);
			this.findLostFields(validator.classReferenceWithTheFields(), lostFields);
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
