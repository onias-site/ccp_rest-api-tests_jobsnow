package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import com.ccp.especifications.email.CcpEmailProvider;
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
import com.jn.entities.JnEntityInstantMessengerUser;
import com.jn.business.messages.JnMessageType;
import com.jn.business.messages.JnMessageType.InstantMessengerApiFields;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnSystemProperties;
import com.vis.business.recruiter.resumes.VisRecruiterLinks;
import com.vis.business.recruiter.resumes.VisRecruiterResumesFields;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityResumeMessage;
import com.vis.entities.VisEntityViewedResume;
import com.vis.json.fields.validation.VisResumeMessageTypes;
import com.vis.services.VisServiceRecruiterResumes;

/**
 * The message to the owner of a resume about a new view (since 2026-11-12): every insertion in {@code vis_viewed_resume}
 * saves, for each owner of the viewed LinkedIn address with a chat, a record {@code newViewInYourResume} in
 * {@code vis_resume_message}, sent through the user bot: the moment of the view and the domain of the recruiter, or the
 * text of the freelancer.
 *
 * <p>Needs {@code recruiterLinksSecret} in {@code application_properties}.
 */
public class NewViewMessageToTheOwnerTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailProvider> email = () -> new FakeEmailSender();

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

	private static final Long OWNER_CHAT = 987654322L;

	/** The moment of the view, as the message writes it. */
	private static final String DATE_TIME = "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}";

	private final long run = System.currentTimeMillis();

	private final List<String> owners = new ArrayList<>();

	private final List<CcpJsonRepresentation> views = new ArrayList<>();

	/** Saves the templates and the texts again, so that the index follows what is declared in java. */
	@BeforeClass
	public static void resaveTheSeeds() {
		List<CcpBulkItem> records = VisEntityResumeMessage.INSTANCE.getFirstRecordsToInsert();
		for (CcpBulkItem record : records) {
			boolean scripted = record.operation != CcpBulkEntityOperationType.create;
			if(scripted) {
				continue;
			}
			record.entity.save(record.json);
		}
	}

	@Before
	public void clearTheMessages() {
		TELEGRAM.sentMessages.clear();
		this.deleteTheMessageOfTheOwner();
	}

	@After
	public void deleteWhatTheTestCreated() {
		for (String owner : this.owners) {
			VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, owner));
			JnEntityInstantMessengerUser.ENTITY.delete(CcpOtherConstants.EMPTY_JSON.put(JnEntityInstantMessengerUser.Fields.email, owner));
		}
		for (CcpJsonRepresentation view : this.views) {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(view);
		}
		this.deleteTheMessageOfTheOwner();
	}

	/** A recruiter of a company: the moment of the view and the domain, through the user bot. */
	@Test
	public void theOwnerIsToldTheMomentAndTheCompanyOfTheView() {
		String linkedinAddress = this.saveResume("empresa", OWNER_CHAT);

		this.view(linkedinAddress, "recrutadora" + this.run + "@Consultoria-Exemplo.com.br");

		String message = this.messageToTheOwner();
		String expected = "Às " + DATE_TIME + " seu currículo foi visualizado por algum recrutador da consultoria / empresa consultoria-exemplo\\.com\\.br";
		assertTrue(message, message.matches(expected));
		this.assertSentThroughTheUserBot(message);
	}

	/** A recruiter of a non-professional domain is told as a freelancer, with no domain. */
	@Test
	public void aFreelancerRecruiterIsToldWithoutDomain() {
		String linkedinAddress = this.saveResume("freela", OWNER_CHAT);

		this.view(linkedinAddress, "recrutador.freela" + this.run + "@gmail.com");

		String message = this.messageToTheOwner();
		String expected = "Às " + DATE_TIME + " seu currículo foi visualizado por algum recrutador freelancer";
		assertTrue(message, message.matches(expected));
		this.assertSentThroughTheUserBot(message);
	}

	/** An owner whose e-mail has no chat in jn_instant_messenger_user gets no message. */
	@Test
	public void anOwnerWithoutChatGetsNoMessage() {
		String linkedinAddress = this.saveResume("semchat", 0L);

		this.view(linkedinAddress, "recrutadora.semchat" + this.run + "@empresa-exemplo.com.br");

		CcpJsonRepresentation key = this.messageKey(0L);
		assertFalse(VisEntityResumeMessage.ENTITY.exists(key));
		assertTrue(TELEGRAM.sentMessages.toString(), TELEGRAM.sentMessages.isEmpty());
	}

	/** Only the insertion of the view tells the owner: opening the same resume again updates the view and tells nothing. */
	@Test
	public void openingTheResumeAgainTellsNothing() {
		String linkedinAddress = this.saveResume("reaberto", OWNER_CHAT);
		String recruiter = "recrutadora.reaberto" + this.run + "@empresa-exemplo.com.br";
		this.view(linkedinAddress, recruiter);
		CcpJsonRepresentation key = this.messageKey(OWNER_CHAT);
		assertTrue(VisEntityResumeMessage.ENTITY.exists(key));
		this.deleteTheMessageOfTheOwner();
		TELEGRAM.sentMessages.clear();

		this.view(linkedinAddress, recruiter);

		assertFalse(VisEntityResumeMessage.ENTITY.exists(key));
		assertTrue(TELEGRAM.sentMessages.toString(), TELEGRAM.sentMessages.isEmpty());
	}

	/**
	 * Each message class of {@code vis_resume_message} sends only its own kind: the view is not sent again through the
	 * template of the invalid LinkedIn address.
	 */
	@Test
	public void theViewIsSentOnlyOnce() {
		String linkedinAddress = this.saveResume("unica", OWNER_CHAT);

		this.view(linkedinAddress, "recrutadora.unica" + this.run + "@empresa-exemplo.com.br");

		String userBotToken = JnSystemProperties.INSTANCE.getSystemInnerProperty(InstantMessengerApiFields.bots, JnMessageType.JnBotType.user);
		boolean userBotConfigured = false == userBotToken.isEmpty();
		if(userBotConfigured) {
			List<CcpJsonRepresentation> toTheOwner = TELEGRAM.sentMessages.stream()
					.filter(sent -> OWNER_CHAT.equals(sent.getAsLongNumber(JnJsonInstantMessengerFields.chatId))).collect(Collectors.toList());
			assertEquals(toTheOwner.toString(), 1, toTheOwner.size());
		}
	}

	private void assertSentThroughTheUserBot(String message) {
		// the delivery through the user bot needs its token in application_properties, which a local machine may not have
		String userBotToken = JnSystemProperties.INSTANCE.getSystemInnerProperty(InstantMessengerApiFields.bots, JnMessageType.JnBotType.user);
		boolean userBotConfigured = false == userBotToken.isEmpty();
		if(userBotConfigured) {
			CcpJsonRepresentation sent = TELEGRAM.lastMessageFor(OWNER_CHAT);
			assertEquals(TELEGRAM.sentMessages.toString(), message, sent.getAsString(JnJsonCommonsFields.message));
			assertEquals(JnMessageType.JnBotType.user.name(), sent.getAsString(JnJsonInstantMessengerFields.botName));
		}
	}

	/**
	 * Saves a resume of its own LinkedIn address and, when the chat is greater than zero, the chat of its e-mail in
	 * jn_instant_messenger_user (since 2026-11-13 the chat is no longer kept in the resume).
	 */
	private String saveResume(String who, Long chatId) {
		String owner = "dono.visto." + who + this.run + "@teste.com";
		String linkedinAddress = "https://www.linkedin.com/in/dono-visto-" + who + "-" + this.run;
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, owner)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(21))
				.put(VisEntityResume.Fields.desiredJob, "Analista visto " + this.run)
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 6)
				.put(VisEntityResume.Fields.linkedinAddress, linkedinAddress)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.clt, 8_000)
				.put(VisEntityResume.Fields.pj, 12_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[]}")))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(new CcpJsonRepresentation("{\"name\":\"Inglês\",\"level\":2,\"id\":0}")));
		VisEntityResume.ENTITY.save(resume);
		this.owners.add(owner);
		boolean withoutChat = chatId <= 0;
		if(withoutChat) {
			return linkedinAddress;
		}
		CcpJsonRepresentation instantMessengerUser = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityInstantMessengerUser.Fields.email, owner)
				.put(JnEntityInstantMessengerUser.Fields.chatId, chatId);
		JnEntityInstantMessengerUser.ENTITY.save(instantMessengerUser);
		return linkedinAddress;
	}

	/** Opens the resume from the e-mail as the recruiter, which saves the view. */
	private void view(String linkedinAddress, String recruiter) {
		String owner = this.owners.get(this.owners.size() - 1);
		CcpJsonRepresentation storedResume = VisEntityResume.ENTITY.getOneById(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, owner));
		String resumeId = VisEntityResume.ENTITY.calculateId(storedResume);
		String token = VisRecruiterLinks.view.sign(recruiter, resumeId);
		CcpJsonRepresentation link = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, recruiter)
				.put(VisRecruiterResumesFields.resumeId, resumeId)
				.put(VisRecruiterResumesFields.token, token);
		VisServiceRecruiterResumes.ViewResumeFromEmail.execute(link);
		CcpJsonRepresentation view = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, linkedinAddress)
				.put(VisEntityViewedResume.Fields.email, recruiter);
		this.views.add(view);
	}

	private CcpJsonRepresentation messageKey(Long chatId) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeMessage.Fields.chatId, chatId)
				.put(VisEntityResumeMessage.Fields.messageType, VisResumeMessageTypes.newViewInYourResume);
	}

	private String messageToTheOwner() {
		CcpJsonRepresentation stored = VisEntityResumeMessage.ENTITY.getOneById(this.messageKey(OWNER_CHAT));
		String message = stored.getAsString(VisEntityResumeMessage.Fields.message);
		return message;
	}

	private void deleteTheMessageOfTheOwner() {
		VisEntityResumeMessage.ENTITY.delete(this.messageKey(OWNER_CHAT));
		VisEntityResumeMessage.ENTITY.delete(this.messageKey(0L));
	}
}
