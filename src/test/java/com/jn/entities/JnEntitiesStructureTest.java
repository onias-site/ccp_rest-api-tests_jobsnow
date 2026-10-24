package com.jn.entities;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginPasswordAttempts;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginSessionTokenAttempts;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.JsEntityLoginToken;
import com.js.entities.JsEntityLoginTokenAttempts;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.js.entities.JsEntityLoginTokenRequestUnlock;

/**
 * Structure tests for every JnEntityXxx entity: checks that the static ENTITY field initializes without error
 * and that the Fields enum answers values()/valueOf() consistently.
 */
public class JnEntitiesStructureTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	// ── ENTITY not null (ensures CcpEntityFactory initialized each entity) ────────

	@Test public void disposableRecordEntityTest() { assertNotNull(JnEntityDisposableRecord.ENTITY); }
	@Test public void emailMessageSentEntityTest() { assertNotNull(JnEntityEmailMessageSent.ENTITY); }
	@Test public void emailParametersToSendEntityTest() { assertNotNull(JnEntityEmailParametersToSend.ENTITY); }
	@Test public void emailReportedAsSpamEntityTest() { assertNotNull(JnEntityEmailReportedAsSpam.ENTITY); }
	@Test public void emailTemplateMessageEntityTest() { assertNotNull(JnEntityEmailTemplateMessage.ENTITY); }
	@Test public void httpApiErrorClientEntityTest() { assertNotNull(JnEntityHttpApiErrorClient.ENTITY); }
	@Test public void httpApiErrorServerEntityTest() { assertNotNull(JnEntityHttpApiErrorServer.ENTITY); }
	@Test public void httpApiRetrySendRequestEntityTest() { assertNotNull(JnEntityHttpApiRetrySendRequest.ENTITY); }
	@Test public void instantMessengerBotLockedEntityTest() { assertNotNull(JnEntityInstantMessengerBotLocked.ENTITY); }
	@Test public void instantMessengerMessageSentEntityTest() { assertNotNull(JnEntityInstantMessengerMessageSent.ENTITY); }
	@Test public void instantMessengerParametersToSendEntityTest() { assertNotNull(JnEntityInstantMessengerParametersToSend.ENTITY); }
	@Test public void instantMessengerTemplateMessageEntityTest() { assertNotNull(JnEntityInstantMessengerTemplateMessage.ENTITY); }
	@Test public void jobsnowErrorEntityTest() { assertNotNull(JnEntityJobsnowError.ENTITY); }
	@Test public void jobsnowWarningEntityTest() { assertNotNull(JnEntityJobsnowWarning.ENTITY); }
	@Test public void loginAnswersEntityTest() { assertNotNull(JsEntityLoginAnswers.ENTITY); }
	@Test public void loginEmailEntityTest() { assertNotNull(JnEntityLoginEmail.ENTITY); }
	@Test public void loginPasswordEntityTest() { assertNotNull(JsEntityLoginPassword.ENTITY); }
	@Test public void loginPasswordAttemptsEntityTest() { assertNotNull(JsEntityLoginPasswordAttempts.ENTITY); }
	@Test public void loginSessionConflictEntityTest() { assertNotNull(JsEntityLoginSessionConflict.ENTITY); }
	@Test public void loginSessionTokenAttemptsEntityTest() { assertNotNull(JsEntityLoginSessionTokenAttempts.ENTITY); }
	@Test public void loginSessionValidationEntityTest() { assertNotNull(JsEntityLoginSessionValidation.ENTITY); }
	@Test public void loginTokenEntityTest() { assertNotNull(JsEntityLoginToken.ENTITY); }
	@Test public void loginTokenAttemptsEntityTest() { assertNotNull(JsEntityLoginTokenAttempts.ENTITY); }
	@Test public void loginTokenRequestResendEntityTest() { assertNotNull(JsEntityLoginTokenRequestResend.ENTITY); }
	@Test public void loginTokenRequestUnlockEntityTest() { assertNotNull(JsEntityLoginTokenRequestUnlock.ENTITY); }
	@Test public void sessionValidatorEntityTest() { assertNotNull(JnEntitySessionValidator.ENTITY); }
	@Test public void recordToReprocessEntityTest() { assertNotNull(JnEntityRecordToReprocess.ENTITY); }
	@Test public void systemMessageEntityTest() { assertNotNull(JnEntitySystemMessage.ENTITY); }
	@Test public void versionableEntityTest() { assertNotNull(JnEntityVersionable.ENTITY); }

	// ── Fields.values() not null ─────────────────────────────────────────────

	@Test public void disposableRecordFieldsTest() { assertNotNull(JnEntityDisposableRecord.Fields.values()); }
	@Test public void emailMessageSentFieldsTest() { assertNotNull(JnEntityEmailMessageSent.Fields.values()); }
	@Test public void emailParametersToSendFieldsTest() { assertNotNull(JnEntityEmailParametersToSend.Fields.values()); }
	@Test public void emailTemplateMessageFieldsTest() { assertNotNull(JnEntityEmailTemplateMessage.Fields.values()); }
	@Test public void httpApiErrorClientFieldsTest() { assertNotNull(JnEntityHttpApiErrorClient.Fields.values()); }
	@Test public void httpApiRetrySendRequestFieldsTest() { assertNotNull(JnEntityHttpApiRetrySendRequest.Fields.values()); }
	@Test public void instantMessengerBotLockedFieldsTest() { assertNotNull(JnEntityInstantMessengerBotLocked.Fields.values()); }
	@Test public void instantMessengerMessageSentFieldsTest() { assertNotNull(JnEntityInstantMessengerMessageSent.Fields.values()); }
	@Test public void instantMessengerParametersToSendFieldsTest() { assertNotNull(JnEntityInstantMessengerParametersToSend.Fields.values()); }
	@Test public void instantMessengerTemplateMessageFieldsTest() { assertNotNull(JnEntityInstantMessengerTemplateMessage.Fields.values()); }
	@Test public void jobsnowErrorFieldsTest() { assertNotNull(JnEntityJobsnowError.Fields.values()); }
	@Test public void loginAnswersFieldsTest() { assertNotNull(JsEntityLoginAnswers.Fields.values()); }
	@Test public void loginEmailFieldsTest() { assertNotNull(JnEntityLoginEmail.Fields.values()); }
	@Test public void loginPasswordFieldsTest() { assertNotNull(JsEntityLoginPassword.Fields.values()); }
	@Test public void loginPasswordAttemptsFieldsTest() { assertNotNull(JsEntityLoginPasswordAttempts.Fields.values()); }
	@Test public void loginSessionConflictFieldsTest() { assertNotNull(JsEntityLoginSessionConflict.Fields.values()); }
	@Test public void loginSessionTokenAttemptsFieldsTest() { assertNotNull(JsEntityLoginSessionTokenAttempts.Fields.values()); }
	@Test public void loginSessionValidationFieldsTest() { assertNotNull(JsEntityLoginSessionValidation.Fields.values()); }
	@Test public void loginTokenFieldsTest() { assertNotNull(JsEntityLoginToken.Fields.values()); }
	@Test public void loginTokenAttemptsFieldsTest() { assertNotNull(JsEntityLoginTokenAttempts.Fields.values()); }
	@Test public void loginTokenRequestResendFieldsTest() { assertNotNull(JsEntityLoginTokenRequestResend.Fields.values()); }
	@Test public void loginTokenRequestUnlockFieldsTest() { assertNotNull(JsEntityLoginTokenRequestUnlock.Fields.values()); }
	@Test public void sessionValidatorFieldsTest() { assertNotNull(JnEntitySessionValidator.Fields.values()); }
	@Test public void recordToReprocessFieldsTest() { assertNotNull(JnEntityRecordToReprocess.Fields.values()); }
	@Test public void versionableFieldsTest() { assertNotNull(JnEntityVersionable.Fields.values()); }

	// ── configurator constructors not null ───────────────────────────────────

	@Test public void loginEmailCtorTest() { assertNotNull(JnEntityLoginEmail.INSTANCE); } 
	@Test public void loginPasswordCtorTest() { assertNotNull(JsEntityLoginPassword.INSTANCE); }
	@Test public void loginTokenCtorTest() { assertNotNull(JsEntityLoginToken.INSTANCE); }
	@Test public void jobsnowErrorCtorTest() { assertNotNull(JnEntityJobsnowError.INSTANCE); }
}
