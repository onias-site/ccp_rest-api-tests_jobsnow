package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.flow.CcpErrorFlowDisturb;
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
import com.jn.entities.JnEntityInstantMessengerUser;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.business.messages.JnMessageType;
import com.jn.business.messages.JnMessageType.InstantMessengerApiFields;
import com.jn.utils.JnLanguage;
import com.jn.utils.JnSystemProperties;
import com.vis.business.bots.linkedin.VisSupportVerifyLinkedinConfirmInvalid;
import com.vis.business.recruiter.resumes.VisRecruiterLinks;
import com.vis.business.recruiter.resumes.VisRecruiterResumesFields;
import com.vis.business.resume.VisBusinessSaveResumeAndSpendTheUpdate;
import com.vis.entities.VisEntityInvalidLinkedinReport;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityResumeMessage;
import com.vis.entities.VisEntityViewedResume;
import com.vis.json.fields.validation.VisResumeMessageTypes;
import com.vis.messages.VisMessages;
import com.vis.services.VisServiceRecruiterResumes;
import com.vis.services.VisServiceResume;
import com.vis.status.VisProcessStatusInvalidLinkedin;
import com.vis.status.VisProcessStatusViewedResume;

/**
 * The LinkedIn address reported as invalid (since 2026-11-12): the recruiter reports it on the page of a resume of the
 * e-mail, the ticket {@code /verifyLinkedinAddress <linkedinAddress> <resumeEmailSha1> <recruiterEmail>} goes to the
 * support operator, who answers whether the recruiter is right ("Sim": the resume is marked, the view deleted and the
 * owner told; "Não": the recruiter is e-mailed and the view goes to the twin with a comment).
 *
 * <p>Needs {@code recruiterLinksSecret} in {@code application_properties}.
 */
public class InvalidLinkedinThroughSupportBotTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	private static final FakeEmailSender EMAIL_INBOX = new FakeEmailSender();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailProvider> email = () -> EMAIL_INBOX;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
	}

	/** The warning that the form of the owner receives with the record. */
	enum Fields implements CcpJsonFieldName {
		invalidLinkedinAddressMessage,
	}

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final Long OWNER_CHAT = 987654321L;

	private final long run = System.currentTimeMillis();

	private final String owner = "dono.linkedin" + this.run + "@teste.com";

	private final String recruiter = "recrutador.linkedin" + this.run + "@teste.com";

	private final String linkedinAddress = "https://www.linkedin.com/in/dono-linkedin-" + this.run;

	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(JbEntityBot.INSTANCE, JnEntityBotCommand.INSTANCE, JnEntityBotCommandStep.INSTANCE, JnEntityBotCommandStepEndMessage.INSTANCE, VisEntityResumeMessage.INSTANCE);
		for (CcpEntityConfigurator configurator : configurators) {
			List<CcpBulkItem> records = configurator.getFirstRecordsToInsert();
			for (CcpBulkItem record : records) {
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
		EMAIL_INBOX.sentEmails.clear();
	}

	@After
	public void deleteWhatTheTestCreated() {
		VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.owner));
		JnEntityInstantMessengerUser.ENTITY.delete(CcpOtherConstants.EMPTY_JSON.put(JnEntityInstantMessengerUser.Fields.email, this.owner));
		VisEntityViewedResume.ENTITY.deleteAnyWhere(this.viewKey());
		VisEntityInvalidLinkedinReport.ENTITY.deleteAnyWhere(this.reportKey());
		CcpJsonRepresentation resumeMessage = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeMessage.Fields.chatId, OWNER_CHAT)
				.put(VisEntityResumeMessage.Fields.messageType, VisResumeMessageTypes.invalidLinkedinAddress);
		VisEntityResumeMessage.ENTITY.delete(resumeMessage);
		String ticket = this.ticketText();
		JnEntitySupportPendingCommand.ENTITY.delete(this.pendingCommand(ticket));
		JbEntityPendingTickets.ENTITY.delete(this.pendingTicket(ticket));
	}

	/**
	 * "Sim": the resume is marked as having an invalid LinkedIn address, the view of the recruiter is deleted from both
	 * sides, the owner gets the message through the user bot (and keeps it in {@code vis_resume_message}), the next
	 * recruiter who opens the resume gets 410 and the form of the owner shows the warning in the language of the screen.
	 */
	@Test
	public void operatorConfirmsThatTheLinkedinIsInvalid() {
		CcpJsonRepresentation link = this.openTheResumeAndReport();
		String ticket = this.ticketText();

		String request = this.operatorTypes(ticket);
		assertTrue(request, request.startsWith("LinkedIn reportado como inválido"));
		assertTrue(request, request.contains("O recrutador " + this.recruiter));
		assertTrue(request, request.contains("Digite \"Sim\" ou \"Não\""));

		String notUnderstood = this.operatorTypes("talvez");
		assertTrue(notUnderstood, notUnderstood.startsWith("Não entendi a resposta"));

		String confirmed = this.operatorTypes(" Sim ");
		assertTrue(confirmed, confirmed.startsWith("O endereço " + this.linkedinAddress + " foi marcado como inválido"));
		assertFalse(this.isPendingTicket(ticket));
		String supportResponse = this.solvedReportResponse();
		assertTrue(supportResponse, supportResponse.startsWith("Confirmamos que o LinkedIn " + this.linkedinAddress + " não abre"));

		CcpJsonRepresentation resume = VisEntityResume.ENTITY.getOneById(this.resumeKey());
		assertTrue(resume.getAsBoolean(VisEntityResume.Fields.invalidLinkedinAddress));
		assertFalse(VisEntityViewedResume.ENTITY.exists(this.viewKey()));
		assertFalse(VisEntityViewedResume.ENTITY.getTwinEntity().exists(this.viewKey()));

		CcpJsonRepresentation resumeMessage = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeMessage.Fields.chatId, OWNER_CHAT)
				.put(VisEntityResumeMessage.Fields.messageType, VisResumeMessageTypes.invalidLinkedinAddress);
		CcpJsonRepresentation storedMessage = VisEntityResumeMessage.ENTITY.getOneById(resumeMessage);
		String messageToTheOwner = storedMessage.getAsString(VisEntityResumeMessage.Fields.message);
		assertTrue(messageToTheOwner, messageToTheOwner.startsWith("O endereço de linkedin " + this.linkedinAddress + " que você informou"));
		// the delivery through the user bot needs its token in application_properties, which a local machine may not have
		String userBotToken = JnSystemProperties.INSTANCE.getSystemInnerProperty(InstantMessengerApiFields.bots, JnMessageType.JnBotType.user);
		boolean userBotConfigured = false == userBotToken.isEmpty();
		if(userBotConfigured) {
			CcpJsonRepresentation sentToTheOwner = TELEGRAM.lastMessageFor(OWNER_CHAT);
			assertEquals(TELEGRAM.sentMessages.toString(), messageToTheOwner, sentToTheOwner.getAsString(JnJsonCommonsFields.message));
			assertEquals(JbBotType.user.name(), sentToTheOwner.getAsString(JnJsonInstantMessengerFields.botName));
		}

		try {
			VisServiceRecruiterResumes.ViewResumeFromEmail.execute(link);
			fail("a resume with an invalid LinkedIn address was opened");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(VisProcessStatusInvalidLinkedin.invalidLinkedinAddress, expected.status);
		}

		CcpJsonRepresentation screen = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, this.owner)
				.put(JnJsonCommonsFields.language, JnLanguage.english.name());
		CcpJsonRepresentation data = VisServiceResume.GetData.execute(screen);
		CcpJsonRepresentation record = data.getInnerJson(VisEntityResume.ENTITY);
		String warning = record.getAsString(Fields.invalidLinkedinAddressMessage);
		assertTrue(warning, warning.startsWith("The LinkedIn address " + this.linkedinAddress));
	}

	/**
	 * "Não": the recruiter is e-mailed that the profile exists, the view goes to the twin with the comment (so the resume
	 * no longer goes to them) and the resume stays as it was.
	 */
	@Test
	public void operatorSaysThatTheLinkedinIsValid() {
		this.openTheResumeAndReport();
		String ticket = this.ticketText();

		this.operatorTypes(ticket);
		String answered = this.operatorTypes("não");
		assertTrue(answered, answered.startsWith("O recrutador " + this.recruiter + " foi avisado por e-mail"));
		assertFalse(this.isPendingTicket(ticket));
		String supportResponse = this.solvedReportResponse();
		assertTrue(supportResponse, supportResponse.startsWith("O perfil " + this.linkedinAddress + " existe no LinkedIn"));

		CcpJsonRepresentation resume = VisEntityResume.ENTITY.getOneById(this.resumeKey());
		assertFalse(resume.getOrDefault(VisEntityResume.Fields.invalidLinkedinAddress, () -> false));
		assertFalse(VisEntityViewedResume.ENTITY.exists(this.viewKey()));
		CcpJsonRepresentation twinView = VisEntityViewedResume.ENTITY.getTwinEntity().getOneById(this.viewKey());
		assertEquals("O endereço de linkedin deste currículo não pôde ser aberto por este recrutador", twinView.getAsString(VisEntityViewedResume.Fields.comment));

		String templateId = VisMessages.VisNotifyRecruiterAboutUnreachableLinkedin.class.getName();
		boolean emailed = EMAIL_INBOX.sentEmails.stream().anyMatch(sentEmail -> templateId.equals(sentEmail.getAsString(JnJsonCommonsFields.templateId))
				&& sentEmail.getAsStringList(JnJsonCommonsFields.email).contains(this.recruiter));
		assertTrue(EMAIL_INBOX.sentEmails.toString(), emailed);
		assertTrue(TELEGRAM.sentMessages.stream().noneMatch(sent -> OWNER_CHAT.equals(sent.getAsLongNumber(JnJsonInstantMessengerFields.chatId))));
	}

	/** A resume that no longer exists ends the ticket with that notice. */
	@Test
	public void theTicketOfAResumeThatNoLongerExists() {
		this.openTheResumeAndReport();
		VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.owner));
		String ticket = this.ticketText();

		String answer = this.operatorTypes(ticket);
		assertTrue(answer, answer.startsWith("Não existe mais o currículo " + this.resumeId()));
	}

	/**
	 * The save of the owner replaces the whole record: the mark is kept, unless the owner changed the LinkedIn address,
	 * which takes the mark away. The chat is no longer kept (since 2026-11-13 it lives in jn_instant_messenger_user).
	 */
	@Test
	public void theSaveOfTheOwnerKeepsTheMarkWhileTheAddressIsTheSame() {
		CcpJsonRepresentation stored = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.linkedinAddress, this.linkedinAddress)
				.put(VisEntityResume.Fields.chatId, OWNER_CHAT)
				.put(VisEntityResume.Fields.invalidLinkedinAddress, true);
		CcpJsonRepresentation sameAddress = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.linkedinAddress, this.linkedinAddress);
		CcpJsonRepresentation kept = VisBusinessSaveResumeAndSpendTheUpdate.getResumeWithKeptFields(sameAddress, stored);
		assertFalse(kept.containsField(VisEntityResume.Fields.chatId));
		assertTrue(kept.getAsBoolean(VisEntityResume.Fields.invalidLinkedinAddress));

		CcpJsonRepresentation newAddress = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.linkedinAddress, this.linkedinAddress + "-novo");
		CcpJsonRepresentation fixed = VisBusinessSaveResumeAndSpendTheUpdate.getResumeWithKeptFields(newAddress, stored);
		assertFalse(fixed.containsField(VisEntityResume.Fields.chatId));
		assertFalse(fixed.getAsBoolean(VisEntityResume.Fields.invalidLinkedinAddress));
	}

	/** The warning of the form, in both languages of the template. */
	@Test
	public void theWarningToTheOwnerInEachLanguage() {
		String portuguese = VisSupportVerifyLinkedinConfirmInvalid.getMessageToTheOwner(this.linkedinAddress, JnLanguage.portuguese);
		String english = VisSupportVerifyLinkedinConfirmInvalid.getMessageToTheOwner(this.linkedinAddress, JnLanguage.english);
		assertTrue(portuguese, portuguese.contains("Por isso, pedimos que corrija a falha."));
		assertTrue(english, english.contains(this.linkedinAddress));
		assertFalse(english, english.contains("{linkedinAddress}"));
	}

	/** Saves the resume of the owner and the chat of its e-mail (jn_instant_messenger_user), opens it from the e-mail as the recruiter and reports the LinkedIn. */
	private CcpJsonRepresentation openTheResumeAndReport() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.owner)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(21))
				.put(VisEntityResume.Fields.desiredJob, "Analista do LinkedIn " + this.run)
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 6)
				.put(VisEntityResume.Fields.linkedinAddress, this.linkedinAddress)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.clt, 8_000)
				.put(VisEntityResume.Fields.pj, 12_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[]}")))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(new CcpJsonRepresentation("{\"name\":\"Inglês\",\"level\":2,\"id\":0}")));
		VisEntityResume.ENTITY.save(resume);
		CcpJsonRepresentation instantMessengerUser = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityInstantMessengerUser.Fields.email, this.owner)
				.put(JnEntityInstantMessengerUser.Fields.chatId, OWNER_CHAT);
		JnEntityInstantMessengerUser.ENTITY.save(instantMessengerUser);
		CcpJsonRepresentation storedResume = VisEntityResume.ENTITY.getOneById(this.resumeKey());
		this.resumeId = VisEntityResume.ENTITY.calculateId(storedResume);
		String resumeId = this.resumeId;
		String token = VisRecruiterLinks.view.sign(this.recruiter, resumeId);
		CcpJsonRepresentation link = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, this.recruiter)
				.put(VisRecruiterResumesFields.resumeId, resumeId)
				.put(VisRecruiterResumesFields.token, token);
		CcpJsonRepresentation opened = VisServiceRecruiterResumes.ViewResumeFromEmail.execute(link);
		assertFalse(opened.getAsBoolean(VisRecruiterResumesFields.inactiveView));
		CcpJsonRepresentation aliasToSave = link.put(VisEntityViewedResume.Fields.alias, "Analista LinkedIn " + this.run);
		VisServiceRecruiterResumes.SaveResumeAliasFromEmail.execute(aliasToSave);
		assertTrue(VisEntityViewedResume.ENTITY.exists(this.viewKey()));

		CcpJsonRepresentation reported = VisServiceRecruiterResumes.ReportInvalidLinkedinFromEmail.execute(link);
		assertEquals(this.linkedinAddress, reported.getAsString(VisEntityResume.Fields.linkedinAddress));
		String ticket = this.ticketText();
		assertEquals(ticket, TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message));
		assertTrue(this.isPendingTicket(ticket));
		// the report is a ticket of the recruiter while it waits, with the priority of the user who cannot go on (2)
		CcpJsonRepresentation storedReport = VisEntityInvalidLinkedinReport.ENTITY.getOneById(this.reportKey());
		assertEquals(resumeId, storedReport.getAsString(VisEntityInvalidLinkedinReport.Fields.resumeEmailSha1));
		CcpJsonRepresentation pendingCommand = JnEntitySupportPendingCommand.ENTITY.getOneById(this.pendingCommand(ticket));
		assertEquals(2L, (long) pendingCommand.getAsLongNumber(JnEntitySupportPendingCommand.Fields.priority));
		// the view goes to the twin at once, before the support decides, with the comment and its alias, in the same write
		assertFalse(VisEntityViewedResume.ENTITY.exists(this.viewKey()));
		CcpJsonRepresentation inactiveView = VisEntityViewedResume.ENTITY.getTwinEntity().getOneById(this.viewKey());
		assertEquals("O endereço de linkedin deste currículo não pôde ser aberto por este recrutador", inactiveView.getAsString(VisEntityViewedResume.Fields.comment));
		assertEquals("Analista LinkedIn " + this.run, inactiveView.getAsString(VisEntityViewedResume.Fields.alias));
		// reporting it again while it waits opens no second ticket
		TELEGRAM.sentMessages.clear();
		VisServiceRecruiterResumes.ReportInvalidLinkedinFromEmail.execute(link);
		assertTrue(TELEGRAM.sentMessages.toString(), TELEGRAM.sentMessages.isEmpty());
		// opening it again keeps the view in the twin, with its alias, and tells the page that it can not be saved
		CcpJsonRepresentation reopened = VisServiceRecruiterResumes.ViewResumeFromEmail.execute(link);
		assertTrue(reopened.getAsBoolean(VisRecruiterResumesFields.inactiveView));
		assertEquals("Analista LinkedIn " + this.run, reopened.getAsString(VisEntityViewedResume.Fields.alias));
		assertFalse(VisEntityViewedResume.ENTITY.exists(this.viewKey()));
		try {
			VisServiceRecruiterResumes.SaveResumeAliasFromEmail.execute(aliasToSave);
			fail("a view in the twin was saved");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(VisProcessStatusViewedResume.inactiveView, expected.status);
		}
		assertFalse(VisEntityViewedResume.ENTITY.exists(this.viewKey()));
		TELEGRAM.sentMessages.clear();
		EMAIL_INBOX.sentEmails.clear();
		return link;
	}

	private CcpJsonRepresentation resumeKey() {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.owner);
	}

	/** The id of the resume, as the e-mail of resumes puts it in the link: calculated from the stored record. */
	private String resumeId = "";

	private String resumeId() {
		return this.resumeId;
	}

	private CcpJsonRepresentation reportKey() {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityInvalidLinkedinReport.Fields.email, this.recruiter)
				.put(VisEntityInvalidLinkedinReport.Fields.linkedinAddress, this.linkedinAddress);
	}

	/** The report left the open side for the solved one, with the answer of the support. */
	private String solvedReportResponse() {
		assertFalse(VisEntityInvalidLinkedinReport.ENTITY.exists(this.reportKey()));
		CcpJsonRepresentation solvedReport = VisEntityInvalidLinkedinReport.ENTITY.getTwinEntity().getOneById(this.reportKey());
		String supportResponse = solvedReport.getAsString(VisEntityInvalidLinkedinReport.Fields.supportResponse);
		return supportResponse;
	}

	private CcpJsonRepresentation viewKey() {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedinAddress)
				.put(VisEntityViewedResume.Fields.email, this.recruiter);
	}

	private String ticketText() {
		return "/verifyLinkedinAddress " + this.linkedinAddress + " " + this.resumeId() + " " + this.recruiter;
	}

	private CcpJsonRepresentation pendingCommand(String command) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnEntitySupportPendingCommand.Fields.botName, JbBotType.support)
				.put(JnEntitySupportPendingCommand.Fields.chatId, SUPPORT_CHAT)
				.put(JnEntitySupportPendingCommand.Fields.command, command);
	}

	private CcpJsonRepresentation pendingTicket(String command) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JbEntityPendingTickets.Fields.botName, JbBotType.support)
				.put(JbEntityPendingTickets.Fields.chatId, SUPPORT_CHAT)
				.put(JbEntityPendingTickets.Fields.ticket, command);
	}

	private boolean isPendingTicket(String command) {
		boolean inTheInbox = JnEntitySupportPendingCommand.ENTITY.exists(this.pendingCommand(command));
		boolean inTheList = JbEntityPendingTickets.ENTITY.exists(this.pendingTicket(command));
		return inTheInbox || inTheList;
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
