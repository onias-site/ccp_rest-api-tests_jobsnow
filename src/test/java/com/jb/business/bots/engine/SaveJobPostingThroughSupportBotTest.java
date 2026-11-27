package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.ElasticsearchLocal;
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
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandName;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnGroupers;
import com.vis.business.job.posting.VisBusinessFindTheMostCoveredSkills;
import com.vis.business.job.posting.VisBusinessJobPostingPublishingMessage;
import com.vis.business.job.posting.VisBusinessPublishJobPostingsOfTheHour;
import com.vis.business.job.posting.VisBusinessRegisterTheRecruitersOfTheJobPosting;
import com.vis.business.job.posting.VisBusinessSaveTheResumeFilterOfTheJobPosting;
import com.vis.business.job.posting.VisBusinessFindTheSkillsOfTheJobPosting;
import com.vis.entities.VisEntityJobPosting;
import com.vis.entities.VisEntityRecruiter;
import com.vis.entities.VisEntityRecruiterResumes;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityResumeFilterDescription;
import com.vis.entities.VisEntityVirtualJobPostingHour;
import com.vis.json.transformers.VisJobPostingHourCombinations;
import com.vis.json.transformers.VisJsonTransformerJobPostingId;

/**
 * {@code /saveJobPosting} of the support bot (since 2026-11-26): the bot asks for the text of a job posting, refuses a
 * text out of 100 to 5,000 characters, and saves it in {@code vis_job_posting}; before the save, the skills of the text
 * are found, crossed in the groups of the resumes by skill, searched as the required skills of the screen
 * {@code curriculos/buscar} and the text is shared there under {@code curriculos@onias.site}. The record is the child of
 * the group of its day and of its millisecond modulo 24. The queue of the entity runs in the thread of the test
 * ({@code syncMensageriaListener}).
 */
public class SaveJobPostingThroughSupportBotTest {

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

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final String SHARING_EMAIL = "curriculos@onias.site";

	private final long run = System.currentTimeMillis();

	private final List<CcpJsonRepresentation> savedResumes = new ArrayList<>();

	private final List<CcpJsonRepresentation> savedJobPostings = new ArrayList<>();

	private final List<String> recruiterEmails = new ArrayList<>();

	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(JbEntityBot.INSTANCE, JnEntityBotCommand.INSTANCE, JnEntityBotCommandStep.INSTANCE, JnEntityBotCommandStepEndMessage.INSTANCE, JnEntityBotCommandName.INSTANCE, VisEntityJobPosting.INSTANCE);
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
		// the engine finds the commands of the bot by a query when it starts: the new command must be visible to it
		String commandsIndex = JnEntityBotCommand.ENTITY.getEntityMetaData().entityName;
		ElasticsearchLocal.refresh(commandsIndex);
	}

	@Before
	public void clearTheBotSession() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		JbEntityBotCommandStepSession.ENTITY.delete(session);
		TELEGRAM.sentMessages.clear();
	}

	@After
	public void deleteWhatTheTestCreated() {
		for (CcpJsonRepresentation resumeKey : this.savedResumes) {
			VisEntityResume.ENTITY.deleteAnyWhere(resumeKey);
		}
		for (CcpJsonRepresentation jobPosting : this.savedJobPostings) {
			VisEntityJobPosting.ENTITY.delete(jobPosting);
		}
		for (String recruiterEmail : this.recruiterEmails) {
			CcpJsonRepresentation recruiterKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, recruiterEmail);
			VisEntityRecruiter.ENTITY.deleteAnyWhere(recruiterKey);
			VisEntityRecruiterResumes.ENTITY.delete(recruiterKey);
		}
	}

	/** The command, in Portuguese, asks for the text; a short text is asked again with its size. */
	@Test
	public void theBotAsksForTheTextAndRefusesAShortOne() {
		String question = this.operatorTypes("/salvarVaga");
		assertEquals("Digite o texto da vaga, de 100 a 5000 caracteres.", question);

		String refusal = this.operatorTypes("   vaga curta   ");
		assertEquals("O texto tem 10 caracteres. Digite o texto da vaga com 100 a 5000 caracteres.", refusal);

		// the session goes on in the step of the text: a valid text is accepted after the refusal
		String text = this.textWithSkills();
		String reply = this.operatorTypes(text);
		assertTrue(reply, reply.startsWith("A vaga foi recebida."));
		this.savedJobPostings.add(this.keyOf(text));
	}

	/**
	 * A valid text is saved with the count and the averages of the search of its resumes, the link of the screen with the
	 * filters and the text shared under curriculos@onias.site, and is the child of the group of its day and hour.
	 */
	@Test
	public void theTextIsSavedWithItsResumesAndInTheGroupOfItsHour() {
		String text = this.textWithSkills();
		this.operatorTypes("/saveJobPosting");
		String reply = this.operatorTypes("  " + text + "  ");
		assertEquals("A vaga foi recebida. Em instantes ela será gravada, junto com os currículos que atendem ao maior número possível das habilidades dela.", reply);

		CcpJsonRepresentation key = this.keyOf(text);
		this.savedJobPostings.add(key);
		CcpJsonRepresentation saved = VisEntityJobPosting.ENTITY.getOneById(key);
		System.out.println("JOB POSTING: " + saved.getJsonPiece(VisEntityJobPosting.Fields.postId, VisEntityJobPosting.Fields.linkToShowResumes, VisEntityJobPosting.Fields.resumesCount, VisEntityJobPosting.Fields.avgClt, VisEntityJobPosting.Fields.avgPj, VisEntityJobPosting.Fields.avgExperience));
		// the text without the spaces around it; only the fields of the entity are saved
		assertEquals(text, saved.getAsString(VisEntityJobPosting.Fields.description));
		assertFalse(saved.containsField(VisBusinessFindTheSkillsOfTheJobPosting.Fields.requestedSkills));
		assertFalse(saved.containsField(VisBusinessFindTheMostCoveredSkills.Fields.possibleSkills));
		assertTrue(saved.containsField(VisEntityJobPosting.Fields.resumesCount));

		// the link opens the screen with the filters of the search and the text shared by curriculos@onias.site
		String link = saved.getAsString(VisEntityJobPosting.Fields.linkToShowResumes);
		assertTrue(link, link.contains("/curriculos/buscar?filtro="));
		assertTrue(link, link.endsWith("&email=" + SHARING_EMAIL));
		String filterId = link.substring(link.indexOf("filtro=") + "filtro=".length(), link.indexOf("&email="));
		CcpJsonRepresentation descriptionKey = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeFilterDescription.Fields.filterId, filterId)
				.put(VisEntityResumeFilterDescription.Fields.email, SHARING_EMAIL);
		CcpJsonRepresentation sharedText = VisEntityResumeFilterDescription.ENTITY.getOneById(descriptionKey);
		assertEquals(text, sharedText.getAsString(VisEntityResumeFilterDescription.Fields.description));

		// the child of the group of the day and of an hour still to come of the saving: the text and the id
		Long timestamp = saved.getAsLongNumber(JnJsonCommonsFields.timestamp);
		CcpTimeDecorator savingTime = new CcpTimeDecorator(timestamp);
		String day = savingTime.getFormattedDateTime("yyyyMMdd");
		int hourOfTheSaving = Integer.parseInt(savingTime.getFormattedDateTime("HH"));
		int expectedHour = hourOfTheSaving == 23 ? 23 : hourOfTheSaving + 1 + (int) (timestamp % (23 - hourOfTheSaving));
		CcpJsonRepresentation groupOfTheHour = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityVirtualJobPostingHour.Fields.day, day)
				.put(VisEntityVirtualJobPostingHour.Fields.hour, expectedHour);
		String parentId = VisEntityVirtualJobPostingHour.ENTITY.calculateId(groupOfTheHour);
		String parentName = VisEntityVirtualJobPostingHour.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityJobPosting.ENTITY.getEntityMetaData().entityName;
		List<String> children = JnGroupers.getChildren(false, parentName, childName, parentId);
		String recordId = VisEntityJobPosting.ENTITY.calculateId(saved);
		boolean found = false;
		for (String childText : children) {
			CcpJsonRepresentation child = new CcpJsonRepresentation(childText);
			boolean thisRecord = recordId.equals(child.getAsString(JnJsonCommonsFields.id));
			if (thisRecord) {
				assertEquals(text, child.getAsString(VisEntityJobPosting.Fields.description));
				found = true;
			}
		}
		assertTrue("the job posting is not a child of the group of its hour", found);
	}

	/** A text with no skill known: no search, the count is zero and there is no link. */
	@Test
	public void aTextWithoutSkillsIsSavedWithNoResume() {
		// lorem ipsum: no word of the dictionary of skills (a Portuguese text easily has one, "so" is SO)
		String text = "Lorem ipsum " + this.run + " dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
		this.operatorTypes("/saveJobPosting");
		this.operatorTypes(text);

		CcpJsonRepresentation key = this.keyOf(text);
		this.savedJobPostings.add(key);
		CcpJsonRepresentation saved = VisEntityJobPosting.ENTITY.getOneById(key);
		assertEquals(0, saved.getAsIntegerNumber(VisEntityJobPosting.Fields.resumesCount).intValue());
		assertFalse(saved.containsField(VisEntityJobPosting.Fields.linkToShowResumes));
	}

	/**
	 * The crossing of the groups of the skills: the largest set of the skills of the text that some resume has. One
	 * resume has three of them, another two, a third only one that the others do not have: the answer is the three of the
	 * first, in the order of the text; a skill no resume has is left out.
	 */
	@Test
	public void theCrossingGivesTheLargestSetOfSkillsSomeResumeHas() {
		String first = "JOBPOSTA" + this.run;
		String second = "JOBPOSTB" + this.run;
		String third = "JOBPOSTC" + this.run;
		String fourth = "JOBPOSTD" + this.run;
		String nobody = "JOBPOSTE" + this.run;
		this.saveResume("all3", first, second, third);
		this.saveResume("only2", first, second);
		this.saveResume("other1", fourth);

		List<String> possibleSkills = this.possibleSkills(nobody, third, fourth, second, first);
		assertEquals(Arrays.asList(third, second, first), possibleSkills);

		assertEquals("no resume has any of them", Arrays.asList(), this.possibleSkills(nobody));
	}

	/** On a tie of size, the set that more resumes share wins. */
	@Test
	public void onATieTheSetMoreResumesShareWins() {
		String first = "JOBTIEA" + this.run;
		String second = "JOBTIEB" + this.run;
		String third = "JOBTIEC" + this.run;
		this.saveResume("tie1", first, second);
		this.saveResume("tie2", second, third);
		this.saveResume("tie3", second, third);

		List<String> possibleSkills = this.possibleSkills(first, second, third);
		assertEquals(Arrays.asList(second, third), possibleSkills);
	}

	/**
	 * The hour of the group is one of the hours still to come in the day of the saving, from the next hour to 23: the
	 * milliseconds of 19h give 20 to 23, of 16h 17 to 23, all of them reached; at 23h the group is 23. The day is always
	 * the day of the saving.
	 */
	@Test
	public void theHourOfTheGroupIsAnHourStillToComeInTheDay() {
		this.assertTheHoursOfTheGroups(19, Arrays.asList(20, 21, 22, 23));
		this.assertTheHoursOfTheGroups(16, Arrays.asList(17, 18, 19, 20, 21, 22, 23));
		this.assertTheHoursOfTheGroups(22, Arrays.asList(23));
		this.assertTheHoursOfTheGroups(23, Arrays.asList(23));
		this.assertTheHoursOfTheGroups(0, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
	}

	private void assertTheHoursOfTheGroups(int hourOfTheSaving, List<Integer> expectedHours) {
		ZonedDateTime saving = ZonedDateTime.of(2026, 11, 26, hourOfTheSaving, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
		long start = saving.toInstant().toEpochMilli();
		Set<Integer> hours = new TreeSet<>();
		for (long millisecond = start; millisecond < start + 100; millisecond++) {
			CcpJsonRepresentation jobPosting = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.timestamp, millisecond);
			List<CcpJsonRepresentation> groups = VisJobPostingHourCombinations.INSTANCE.apply(jobPosting);
			CcpJsonRepresentation group = groups.get(0);
			assertEquals("20261126", group.getAsString(VisEntityVirtualJobPostingHour.Fields.day));
			hours.add(group.getAsIntegerNumber(VisEntityVirtualJobPostingHour.Fields.hour));
		}
		assertEquals("saved at " + hourOfTheSaving + "h", expectedHours, new ArrayList<>(hours));
	}

	/**
	 * The message of a job posting that found resumes: the header, the text escaped, the paragraph to the candidates and
	 * the one to the recruiters with the count, the averages in Brazilian format (an average no resume informed as "não
	 * informada") and the link of the resumes.
	 */
	@Test
	public void theMessageOfAJobPostingWithResumesHasTheCountTheAveragesAndTheLink() {
		String text = "Vaga <Java> & Spring " + "x".repeat(100);
		CcpJsonRepresentation jobPosting = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityJobPosting.Fields.description, text)
				.put(VisEntityJobPosting.Fields.resumesCount, 12)
				.put(VisEntityJobPosting.Fields.avgClt, 9500.0)
				.put(VisEntityJobPosting.Fields.avgExperience, 4.46)
				.put(VisEntityJobPosting.Fields.linkToShowResumes, "https://onias.site/curriculos/buscar?filtro=abc&email=" + SHARING_EMAIL);
		List<String> pieces = this.piecesOf(jobPosting);
		assertEquals(1, pieces.size());
		String message = pieces.get(0);
		System.out.println("MESSAGE WITH RESUMES:\n" + message);
		assertTrue(message, message.startsWith("<b>[AJUDE AS PESSOAS ENCAMINHANDO ESTA VAGA AO MÁXIMO DE GRUPOS E PESSOAS QUE VOCÊ PUDER 🙏🙏🙏]</b>\nVaga &lt;Java&gt; &amp; Spring "));
		assertTrue(message, message.contains("<b>Envie seu curriculo a 2.000 recrutadores, receba vagas aderentes ao teu perfil, basta <a href=\""));
		assertTrue(message, message.contains("/meus-dados/sobre-mim\">clicar aqui</a></b>"));
		assertTrue(message, message.endsWith("<b>Recrutador, cadastre vagas e receba currículos exatamente aderentes à sua vaga.\n"
				+ "Esta vaga por exemplo encontrou 12 currículos (média CLT R$ 9.500,00, média PJ não informada, média de experiência 4,5 anos) "
				+ "<a href=\"https://onias.site/curriculos/buscar?filtro=abc&email=" + SHARING_EMAIL + "\">Ver quais são estes currículos</a></b>"));
	}

	/** A job posting with no resume (no link) gets the paragraph to the recruiters without the example. */
	@Test
	public void theMessageOfAJobPostingWithoutResumesHasNoExample() {
		CcpJsonRepresentation jobPosting = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityJobPosting.Fields.description, "y".repeat(120))
				.put(VisEntityJobPosting.Fields.resumesCount, 0);
		String message = this.piecesOf(jobPosting).get(0);
		assertTrue(message, message.endsWith("<b>Recrutador, cadastre vagas e receba currículos exatamente aderentes à sua vaga.</b>"));
		assertFalse(message, message.contains("Esta vaga por exemplo"));
	}

	/**
	 * A text of 5,000 characters goes in pieces of up to 4,096 characters, cut at a blank, the paragraphs in the last one;
	 * the pieces together keep the whole text, escaped.
	 */
	@Test
	public void aLongMessageGoesInPiecesWithinTheLimitOfTelegram() {
		StringBuilder text = new StringBuilder();
		for (int word = 0; text.length() < 5000; word++) {
			text.append("palavra&").append(word).append(' ');
		}
		String description = text.substring(0, 5000).trim();
		CcpJsonRepresentation jobPosting = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityJobPosting.Fields.description, description)
				.put(VisEntityJobPosting.Fields.resumesCount, 0);
		List<String> pieces = this.piecesOf(jobPosting);
		assertTrue(pieces.toString(), pieces.size() >= 3);
		for (String piece : pieces) {
			assertTrue("piece of " + piece.length(), piece.length() <= VisBusinessJobPostingPublishingMessage.TELEGRAM_LIMIT);
		}
		String lastPiece = pieces.get(pieces.size() - 1);
		assertTrue(lastPiece, lastPiece.startsWith("<b>Envie seu curriculo"));
		String textPieces = String.join(" ", pieces.subList(0, pieces.size() - 1));
		String expected = description.replace("&", "&amp;").replaceAll("\\s+", " ");
		assertTrue("the text is kept whole", textPieces.replaceAll("\\s+", " ").contains(expected));
	}

	/**
	 * The hourly process publishes the job postings of the group of the current hour in every group of technology (the
	 * chats of resumeType 1, each once) with the user bot. The job posting is saved in the previous hour with a
	 * millisecond whose group is the current hour; at midnight there is no previous hour of the day and the test is
	 * skipped.
	 */
	@Test
	public void theJobPostingsOfTheHourArePublishedInTheGroupsOfTechnology() {
		ZoneId saoPaulo = ZoneId.of("America/Sao_Paulo");
		ZonedDateTime now = ZonedDateTime.now(saoPaulo);
		int hour = now.getHour();
		Assume.assumeTrue("no previous hour at midnight", hour > 0);
		long startOfThePreviousHour = now.withHour(hour - 1).withMinute(0).withSecond(0).withNano(0).toInstant().toEpochMilli();
		// saved at hour - 1, the group is hour - 1 + 1 + millisecond % (24 - hour): the current hour when the remainder is 0
		long hoursStillToCome = 24 - hour;
		long timestamp = startOfThePreviousHour + (hoursStillToCome - startOfThePreviousHour % hoursStillToCome) % hoursStillToCome;
		String text = this.textWithSkills();
		CcpJsonRepresentation jobPosting = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityJobPosting.Fields.description, text)
				.put(VisEntityJobPosting.Fields.timestamp, timestamp);
		CcpJsonRepresentation withTheId = VisEntityJobPosting.ENTITY.getJsonWithHandledPrimaryKey(jobPosting);
		VisEntityJobPosting.ENTITY.save(withTheId);
		this.savedJobPostings.add(this.keyOf(text));
		ElasticsearchLocal.refresh("jn_grouper");

		TELEGRAM.sentMessages.clear();
		CcpJsonRepresentation result = VisBusinessPublishJobPostingsOfTheHour.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
		System.out.println("PUBLISHING: " + result);
		assertEquals(hour, result.getAsIntegerNumber(VisBusinessPublishJobPostingsOfTheHour.Fields.hour).intValue());
		int groups = result.getAsIntegerNumber(VisBusinessPublishJobPostingsOfTheHour.Fields.groups);
		assertTrue("no group of technology in the local database", groups > 0);
		assertEquals(0, result.getAsIntegerNumber(VisBusinessPublishJobPostingsOfTheHour.Fields.failures).intValue());

		Set<Long> chatsOfThisJobPosting = new TreeSet<>();
		List<CcpJsonRepresentation> sent = new ArrayList<>(TELEGRAM.sentMessages);
		for (CcpJsonRepresentation message : sent) {
			String messageText = message.getAsString(JnJsonCommonsFields.message);
			boolean ofThisJobPosting = messageText.contains(text);
			if (false == ofThisJobPosting) {
				continue;
			}
			assertEquals("user", message.getAsString(JnJsonInstantMessengerFields.botName));
			assertTrue(messageText, messageText.startsWith("<b>[AJUDE AS PESSOAS"));
			boolean firstTime = chatsOfThisJobPosting.add(message.getAsLongNumber(JnJsonInstantMessengerFields.chatId));
			assertTrue("a group got the job posting twice", firstTime);
		}
		assertEquals("every group of technology gets the job posting once", groups, chatsOfThisJobPosting.size());
	}

	/**
	 * The e-mails of the text become recruiters with the source support (one written twice, in another case, once) and
	 * get the usual e-mail of resumes with the resumes found by the job posting; a second job posting on the same day sends
	 * them nothing (one e-mail of resumes a day). The record keeps only the fields of the entity.
	 */
	@Test
	public void theEmailsOfTheTextBecomeRecruitersAndGetTheResumesOnceADay() {
		String recruiterEmail = "rh." + this.run + "@empresa-exemplo.com.br";
		this.recruiterEmails.add(recruiterEmail);
		String text = "Vaga " + this.run + " para pessoa desenvolvedora com Java, SQL e Git, contrato CLT, inicio imediato, envie o curriculo para "
				+ recruiterEmail + " ou RH." + this.run + "@Empresa-Exemplo.com.br hoje.";
		EMAIL_INBOX.sentEmails.clear();
		this.operatorTypes("/salvarVaga");
		this.operatorTypes(text);
		this.savedJobPostings.add(this.keyOf(text));

		CcpJsonRepresentation saved = VisEntityJobPosting.ENTITY.getOneById(this.keyOf(text));
		int resumesCount = saved.getAsIntegerNumber(VisEntityJobPosting.Fields.resumesCount);
		assertTrue("the text finds no resume in the local database", resumesCount > 0);
		assertFalse(saved.containsField(VisBusinessSaveTheResumeFilterOfTheJobPosting.Fields.resumeIds));
		assertFalse(saved.containsField(VisBusinessRegisterTheRecruitersOfTheJobPosting.Fields.recruiterEmails));

		CcpJsonRepresentation recruiterKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, recruiterEmail);
		CcpJsonRepresentation recruiter = VisEntityRecruiter.ENTITY.getOneById(recruiterKey);
		assertEquals("support", recruiter.getAsString(VisEntityRecruiter.Fields.source));
		List<CcpJsonRepresentation> emailsToTheRecruiter = this.emailsTo(recruiterEmail);
		assertEquals("one e-mail to the recruiter, the two ways the text writes the address being the same", 1, emailsToTheRecruiter.size());
		String message = emailsToTheRecruiter.get(0).getAsString(JnJsonCommonsFields.message);
		assertTrue("the table of resumes of the usual e-mail", message.contains("Idiomas e habilidades"));
		assertTrue(VisEntityRecruiterResumes.ENTITY.exists(recruiterKey));

		String secondText = "Segunda vaga " + this.run + " para pessoa desenvolvedora com Java, SQL e Git, contrato PJ, envie o curriculo para "
				+ recruiterEmail + " ainda hoje, por favor.";
		this.operatorTypes("/salvarVaga");
		this.operatorTypes(secondText);
		this.savedJobPostings.add(this.keyOf(secondText));
		assertTrue(VisEntityJobPosting.ENTITY.exists(this.keyOf(secondText)));
		assertEquals("a second job posting on the same day sends no e-mail", 1, this.emailsTo(recruiterEmail).size());
	}

	/** An inactive recruiter (in the twin) is not brought back by a job posting and gets no e-mail. */
	@Test
	public void anInactiveRecruiterIsNotRegisteredAgainNorGetsTheResumes() {
		String recruiterEmail = "inativo." + this.run + "@empresa-exemplo.com.br";
		this.recruiterEmails.add(recruiterEmail);
		CcpJsonRepresentation recruiterKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, recruiterEmail);
		CcpJsonRepresentation recruiterInput = recruiterKey.put(VisEntityRecruiter.Fields.source, "support");
		CcpJsonRepresentation recruiter = com.vis.business.recruiter.VisBusinessPrepareRecruiter.INSTANCE.execute(recruiterInput);
		VisEntityRecruiter.ENTITY.save(recruiter);
		// deleting from the recruiters moves the record to the twin, the inactive recruiters
		VisEntityRecruiter.ENTITY.delete(recruiterKey);
		CcpEntity twinEntity = VisEntityRecruiter.ENTITY.getTwinEntity();
		assertTrue(twinEntity.exists(recruiterKey));

		String text = "Vaga " + this.run + " de pessoa desenvolvedora com Java, SQL e Git, envie o curriculo para " + recruiterEmail + " o quanto antes, obrigado.";
		EMAIL_INBOX.sentEmails.clear();
		this.operatorTypes("/salvarVaga");
		this.operatorTypes(text);
		this.savedJobPostings.add(this.keyOf(text));
		assertTrue(VisEntityJobPosting.ENTITY.exists(this.keyOf(text)));
		assertFalse("not brought back to the recruiters", VisEntityRecruiter.ENTITY.exists(recruiterKey));
		assertTrue(twinEntity.exists(recruiterKey));
		assertEquals(0, this.emailsTo(recruiterEmail).size());
	}

	private List<CcpJsonRepresentation> emailsTo(String recipient) {
		List<CcpJsonRepresentation> emails = new ArrayList<>();
		for (CcpJsonRepresentation email : EMAIL_INBOX.sentEmails) {
			List<String> recipients = email.getAsStringList(JnJsonCommonsFields.email);
			boolean toTheRecipient = recipients.contains(recipient);
			if (toTheRecipient) {
				emails.add(email);
			}
		}
		return emails;
	}

	private List<String> piecesOf(CcpJsonRepresentation jobPosting) {
		CcpJsonRepresentation message = VisBusinessJobPostingPublishingMessage.INSTANCE.execute(jobPosting);
		return message.getAsStringList(VisBusinessJobPostingPublishingMessage.Fields.pieces);
	}

	private List<String> possibleSkills(String... requestedSkills) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(VisBusinessFindTheSkillsOfTheJobPosting.Fields.requestedSkills, Arrays.asList(requestedSkills));
		CcpJsonRepresentation answer = VisBusinessFindTheMostCoveredSkills.INSTANCE.execute(json);
		return answer.getAsStringList(VisBusinessFindTheMostCoveredSkills.Fields.possibleSkills);
	}

	private void saveResume(String name, String... skills) {
		String email = "jobposting." + name + "." + this.run + "@jobsnow.com";
		List<CcpJsonRepresentation> resumeSkills = new ArrayList<>();
		for (String skill : skills) {
			resumeSkills.add(new CcpJsonRepresentation("{\"skill\":\"" + skill + "\",\"word\":\"" + skill + "\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}"));
		}
		CcpJsonRepresentation english = new CcpJsonRepresentation("{\"name\":\"English\",\"level\":2,\"id\":0}");
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/onias")
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000)
				.put(VisEntityResume.Fields.skill, resumeSkills)
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(english));
		VisEntityResume.ENTITY.save(resume);
		this.savedResumes.add(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email));
	}

	/** A text of this run with skills of the dictionary, from 100 to 5,000 characters. */
	private String textWithSkills() {
		return "Vaga " + this.run + " para pessoa desenvolvedora backend com Java, Spring Boot, SQL e Git, "
				+ "trabalhando com APIs REST, testes automatizados e metodologias ageis como Scrum.";
	}

	private CcpJsonRepresentation keyOf(String text) {
		CcpJsonRepresentation withTheText = CcpOtherConstants.EMPTY_JSON.put(VisEntityJobPosting.Fields.description, text);
		CcpJsonRepresentation withTheId = VisJsonTransformerJobPostingId.INSTANCE.execute(withTheText);
		return withTheId.getJsonPiece(VisEntityJobPosting.Fields.postId);
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
