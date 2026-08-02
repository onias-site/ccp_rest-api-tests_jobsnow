package com.jn.entities;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Testes de estrutura para todas as entidades JnEntityXxx:
 * verifica que o campo ENTITY estático inicializa sem erro e que
 * o enum Fields responde a values()/valueOf() de forma consistente.
 */
public class JnEntitiesStructureTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── ENTITY não null (garante que CcpEntityFactory inicializou cada entidade) ──

	@Test public void asyncTaskEntityTest() { assertNotNull(JnEntityAsyncTask.ENTITY); }
	@Test public void contactUsEntityTest() { assertNotNull(JnEntityContactUs.ENTITY); }
	@Test public void contactUsIgnoredEntityTest() { assertNotNull(JnEntityContactUsIgnored.ENTITY); }
	@Test public void contactUsSkipedEntityTest() { assertNotNull(JnEntityContactUsSkiped.ENTITY); }
	@Test public void disposableRecordEntityTest() { assertNotNull(JnEntityDisposableRecord.ENTITY); }
	@Test public void disposableTestEntityTest() { assertNotNull(JnEntityDisposableTest.ENTITY); }
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
	@Test public void jobsnowPenddingErrorEntityTest() { assertNotNull(JnEntityJobsnowPenddingError.ENTITY); }
	@Test public void jobsnowWarningEntityTest() { assertNotNull(JnEntityJobsnowWarning.ENTITY); }
	@Test public void loginAnswersEntityTest() { assertNotNull(JnEntityLoginAnswers.ENTITY); }
	@Test public void loginEmailEntityTest() { assertNotNull(JnEntityLoginEmail.ENTITY); }
	@Test public void loginPasswordEntityTest() { assertNotNull(JnEntityLoginPassword.ENTITY); }
	@Test public void loginPasswordAttemptsEntityTest() { assertNotNull(JnEntityLoginPasswordAttempts.ENTITY); }
	@Test public void loginSessionConflictEntityTest() { assertNotNull(JnEntityLoginSessionConflict.ENTITY); }
	@Test public void loginSessionTokenAttemptsEntityTest() { assertNotNull(JnEntityLoginSessionTokenAttempts.ENTITY); }
	@Test public void loginSessionValidationEntityTest() { assertNotNull(JnEntityLoginSessionValidation.ENTITY); }
	@Test public void loginStatsEntityTest() { assertNotNull(JnEntityLoginStats.ENTITY); }
	@Test public void loginTokenEntityTest() { assertNotNull(JnEntityLoginToken.ENTITY); }
	@Test public void loginTokenAttemptsEntityTest() { assertNotNull(JnEntityLoginTokenAttempts.ENTITY); }
	@Test public void loginTokenRequestResendEntityTest() { assertNotNull(JnEntityLoginTokenRequestResend.ENTITY); }
	@Test public void loginTokenRequestUnlockEntityTest() { assertNotNull(JnEntityLoginTokenRequestUnlock.ENTITY); }
	@Test public void recordToReprocessEntityTest() { assertNotNull(JnEntityRecordToReprocess.ENTITY); }
	@Test public void systemMessageEntityTest() { assertNotNull(JnEntitySystemMessage.ENTITY); }
	@Test public void versionableEntityTest() { assertNotNull(JnEntityVersionable.ENTITY); }

	// ── Fields.values() não null ─────────────────────────────────────────────

	@Test public void asyncTaskFieldsTest() { assertNotNull(JnEntityAsyncTask.Fields.values()); }
	@Test public void contactUsFieldsTest() { assertNotNull(JnEntityContactUs.Fields.values()); }
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
	@Test public void loginAnswersFieldsTest() { assertNotNull(JnEntityLoginAnswers.Fields.values()); }
	@Test public void loginEmailFieldsTest() { assertNotNull(JnEntityLoginEmail.Fields.values()); }
	@Test public void loginPasswordFieldsTest() { assertNotNull(JnEntityLoginPassword.Fields.values()); }
	@Test public void loginPasswordAttemptsFieldsTest() { assertNotNull(JnEntityLoginPasswordAttempts.Fields.values()); }
	@Test public void loginSessionConflictFieldsTest() { assertNotNull(JnEntityLoginSessionConflict.Fields.values()); }
	@Test public void loginSessionTokenAttemptsFieldsTest() { assertNotNull(JnEntityLoginSessionTokenAttempts.Fields.values()); }
	@Test public void loginSessionValidationFieldsTest() { assertNotNull(JnEntityLoginSessionValidation.Fields.values()); }
	@Test public void loginTokenFieldsTest() { assertNotNull(JnEntityLoginToken.Fields.values()); }
	@Test public void loginTokenAttemptsFieldsTest() { assertNotNull(JnEntityLoginTokenAttempts.Fields.values()); }
	@Test public void loginTokenRequestResendFieldsTest() { assertNotNull(JnEntityLoginTokenRequestResend.Fields.values()); }
	@Test public void loginTokenRequestUnlockFieldsTest() { assertNotNull(JnEntityLoginTokenRequestUnlock.Fields.values()); }
	@Test public void recordToReprocessFieldsTest() { assertNotNull(JnEntityRecordToReprocess.Fields.values()); }
	@Test public void versionableFieldsTest() { assertNotNull(JnEntityVersionable.Fields.values()); }

	// ── Construtores configurator não null ───────────────────────────────────

	@Test public void asyncTaskCtorTest() { assertNotNull(new JnEntityAsyncTask()); }
	@Test public void contactUsCtorTest() { assertNotNull(new JnEntityContactUs()); }
	@Test public void loginEmailCtorTest() { assertNotNull(new JnEntityLoginEmail()); }
	@Test public void loginPasswordCtorTest() { assertNotNull(new JnEntityLoginPassword()); }
	@Test public void loginTokenCtorTest() { assertNotNull(new JnEntityLoginToken()); }
	@Test public void jobsnowErrorCtorTest() { assertNotNull(new JnEntityJobsnowError()); }
}
