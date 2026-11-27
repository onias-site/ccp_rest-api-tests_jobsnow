package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
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
import com.vis.business.job.posting.VisBusinessFindTheSkillsOfTheJobPosting;
import com.vis.entities.VisEntityJobPosting;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityResumeFilterDescription;
import com.vis.entities.VisEntityVirtualJobPostingHour;
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

		// the child of the group of the day and of the millisecond modulo 24 of the saving: the text and the id
		Long timestamp = saved.getAsLongNumber(JnJsonCommonsFields.timestamp);
		String day = new CcpTimeDecorator(timestamp).getFormattedDateTime("yyyyMMdd");
		CcpJsonRepresentation groupOfTheHour = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityVirtualJobPostingHour.Fields.day, day)
				.put(VisEntityVirtualJobPostingHour.Fields.hour, (int) (timestamp % 24));
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
