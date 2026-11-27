package com.vis.business.job.posting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
import com.ccp.especifications.db.utils.entity.decorators.ElasticsearchLocal;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.business.bots.engine.JbBusinessReceiveBotUpdate;
import com.jb.entities.JbEntityBotUpdateId;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.entities.VisEntityInstantMessagePositionReply;
import com.vis.entities.VisEntityResumeFilterDescription;

/**
 * {@code vis_instant_message_position_reply} (since 2026-11-27): a job posting written in a group of Telegram runs, before
 * its save, the six businesses of {@code vis_job_posting}; once saved, the support bot answers the message of the job
 * posting in the group with the resumes found. A refused job posting is not saved, and nothing is answered. Every text
 * message of the groups of the support bot comes here, through the queue of its groups.
 */
public class InstantMessagePositionReplyTest {

	private static final ReplyRecordingInstantMessenger TELEGRAM = new ReplyRecordingInstantMessenger();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram
		);
	}

	private static final String SHARING_EMAIL = "curriculos@onias.site";

	private static final Long GROUP_CHAT = -1001234567890L;

	private static final Long MESSAGE_OF_THE_JOB_POSTING = 4321L;

	private final long run = System.currentTimeMillis();

	private final List<CcpJsonRepresentation> savedRecords = new ArrayList<>();

	@BeforeClass
	public static void saveTheTextOfTheAnswer() {
		List<CcpBulkItem> records = VisEntityInstantMessagePositionReply.INSTANCE.getFirstRecordsToInsert();
		for (CcpBulkItem record : records) {
			boolean scripted = record.operation != CcpBulkEntityOperationType.create;
			if (scripted) {
				continue;
			}
			record.entity.save(record.json);
		}
	}

	@Before
	public void clearTheDescriptionsOfTheJobPostings() {
		TELEGRAM.sentMessages.clear();
		// a job posting goes on only when its text is a new description of its filters, and the descriptions never expire:
		// the ones shared under the e-mail of the job postings by earlier tests and runs would refuse the job postings
		CcpJsonRepresentation sharingKey = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeFilterDescription.Fields.filterId, "0")
				.put(VisEntityResumeFilterDescription.Fields.email, SHARING_EMAIL);
		CcpJsonRepresentation handledSharingKey = VisEntityResumeFilterDescription.ENTITY.getJsonWithHandledPrimaryKey(sharingKey);
		String sharingEmailHash = handledSharingKey.getAsString(VisEntityResumeFilterDescription.Fields.email);
		String descriptionsIndex = VisEntityResumeFilterDescription.ENTITY.getEntityMetaData().entityName;
		// the description written by the previous test is searchable only after the refresh
		ElasticsearchLocal.refresh(descriptionsIndex);
		ElasticsearchLocal.deleteWhere(descriptionsIndex, VisEntityResumeFilterDescription.Fields.email.name(), sharingEmailHash);
	}

	@After
	public void deleteWhatTheTestCreated() {
		for (CcpJsonRepresentation record : this.savedRecords) {
			VisEntityInstantMessagePositionReply.ENTITY.delete(record);
		}
		JbEntityBotUpdateId.ENTITY.delete(CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.botName, "support"));
	}

	/**
	 * The whole way of a job posting written in a group where the support bot is (since 2026-11-27): the update of
	 * Telegram goes from the queue of the bot to the queue of its groups ({@code jn_bot_group_message_handler}, seeded
	 * here), never to the bot engine, and the job posting is saved and answered in the group.
	 */
	@Test
	public void aJobPostingWrittenInAGroupOfTheSupportBotIsAnswered() {
		String text = this.textWithSkills();
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateOfTheGroup(8000L, text));

		List<CcpJsonRepresentation> answers = TELEGRAM.sentTo(GROUP_CHAT);
		assertEquals("only the answer, no list of commands", 1, TELEGRAM.sentMessages.size());
		assertEquals(1, answers.size());
		CcpJsonRepresentation answer = answers.get(0);
		String message = answer.getAsString(JnJsonCommonsFields.message);
		String link = this.linkOf(message);
		assertTrue(message, message.startsWith("Foram encontrados "));
		assertEquals("support", answer.getAsString(JnJsonInstantMessengerFields.botName));
		assertEquals(MESSAGE_OF_THE_JOB_POSTING, answer.getAsLongNumber(CcpJsonCommonsFields.replyTo));
		CcpJsonRepresentation saved = VisEntityInstantMessagePositionReply.ENTITY.getOneById(this.keyOf(link));
		assertEquals(text, saved.getAsString(VisEntityInstantMessagePositionReply.Fields.description));
	}

	/** A conversation of the group (a text the entity refuses) and a text without skills: nothing answered, no error. */
	@Test
	public void aConversationOfTheGroupIsIgnored() {
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateOfTheGroup(8100L, "bom dia, pessoal"));
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateOfTheGroup(8101L, "/showAllCommands"));
		String lorem = "Lorem ipsum " + this.run + " dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
		JbBusinessReceiveBotUpdate.INSTANCE.execute(this.updateOfTheGroup(8102L, lorem));

		assertEquals(0, TELEGRAM.sentMessages.size());
	}

	/** An update of Telegram with a message of the group, for the support bot. */
	private CcpJsonRepresentation updateOfTheGroup(long updateId, String text) {
		CcpJsonRepresentation update = new CcpJsonRepresentation("{"
				+ "\"botName\": \"support\","
				+ "\"update_id\": " + updateId + ","
				+ "\"message\": {"
				+ "\"message_id\": " + MESSAGE_OF_THE_JOB_POSTING + ","
				+ "\"date\": 1790121741,"
				+ "\"from\": {\"id\": 751717896, \"username\": \"recrutadora\"},"
				+ "\"chat\": {\"id\": " + GROUP_CHAT + ", \"type\": \"supergroup\"},"
				+ "\"text\": \"" + text + "\""
				+ "}}");
		return update;
	}

	/**
	 * A job posting with resumes is saved with the count, the averages and the link, without what the businesses passed on
	 * to one another, and the support bot answers its message in the group with the resumes found.
	 */
	@Test
	public void aJobPostingWithResumesIsSavedAndAnsweredInTheGroup() {
		String text = this.textWithSkills();
		this.saveThePosting(text);

		List<CcpJsonRepresentation> answers = TELEGRAM.sentTo(GROUP_CHAT);
		assertEquals("one answer in the group", 1, answers.size());
		CcpJsonRepresentation answer = answers.get(0);
		String message = answer.getAsString(JnJsonCommonsFields.message);
		System.out.println("ANSWER:\n" + message);
		// the primary key is the link, written before the save: the test learns it from the answer
		String link = this.linkOf(message);
		CcpJsonRepresentation key = this.keyOf(link);
		assertTrue("the job posting was not saved under its link", VisEntityInstantMessagePositionReply.ENTITY.exists(key));
		CcpJsonRepresentation saved = VisEntityInstantMessagePositionReply.ENTITY.getOneById(key);
		System.out.println("POSITION REPLY: " + saved);
		assertTrue(link, link.contains("/curriculos/buscar?filtro="));
		assertEquals(link, saved.getAsString(VisEntityInstantMessagePositionReply.Fields.linkToShowResumes));
		assertEquals(GROUP_CHAT, saved.getAsLongNumber(VisEntityInstantMessagePositionReply.Fields.groupChatId));
		assertEquals(MESSAGE_OF_THE_JOB_POSTING, saved.getAsLongNumber(VisEntityInstantMessagePositionReply.Fields.replyTo));
		assertEquals(text, saved.getAsString(VisEntityInstantMessagePositionReply.Fields.description));
		int resumesCount = saved.getAsIntegerNumber(VisEntityInstantMessagePositionReply.Fields.resumesCount);
		assertTrue("the text finds no resume in the local database", resumesCount > 0);
		assertFalse(saved.containsField(VisBusinessFindTheSkillsOfTheJobPosting.Fields.requestedSkills));
		assertFalse(saved.containsField(VisBusinessSaveTheResumeFilterOfTheJobPosting.Fields.filterId));

		assertEquals("support", answer.getAsString(JnJsonInstantMessengerFields.botName));
		assertEquals("the answer answers the message of the job posting", MESSAGE_OF_THE_JOB_POSTING, answer.getAsLongNumber(CcpJsonCommonsFields.replyTo));
		assertTrue(message, message.startsWith("Foram encontrados " + resumesCount + " currículos (média CLT "));
		assertTrue(message, message.contains(", média PJ "));
		assertTrue(message, message.contains(", média de experiência "));
		assertTrue(message, message.endsWith(" anos)\n<a href=\"" + link + "\">Ver quais são estes currículos</a>"));
	}

	/** A text without skills is refused before the save: nothing is saved and nothing is answered. */
	@Test
	public void aTextWithoutSkillsIsNotSavedNorAnswered() {
		// lorem ipsum: no word of the dictionary of skills
		String text = "Lorem ipsum " + this.run + " dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
		this.saveThePosting(text);

		// refused before the search: there is no link, so no record could exist
		assertEquals(0, TELEGRAM.sentTo(GROUP_CHAT).size());
	}

	/**
	 * A second job posting whose resumes have the filters of an earlier one has the same link: it is refused, the record
	 * of the link keeps the first text, and there is no second answer.
	 */
	@Test
	public void aJobPostingWhoseFiltersAlreadyHaveATextIsNotSavedNorAnswered() {
		String firstText = this.textWithSkills();
		this.saveThePosting(firstText);
		List<CcpJsonRepresentation> answers = TELEGRAM.sentTo(GROUP_CHAT);
		assertEquals(1, answers.size());
		String firstMessage = answers.get(0).getAsString(JnJsonCommonsFields.message);
		CcpJsonRepresentation key = this.keyOf(this.linkOf(firstMessage));
		assertTrue(VisEntityInstantMessagePositionReply.ENTITY.exists(key));

		this.saveThePosting("Outra " + firstText);
		assertEquals("no answer to the refused job posting", 1, TELEGRAM.sentTo(GROUP_CHAT).size());
		CcpJsonRepresentation saved = VisEntityInstantMessagePositionReply.ENTITY.getOneById(key);
		assertEquals("the record of the link keeps the first text", firstText, saved.getAsString(VisEntityInstantMessagePositionReply.Fields.description));
	}

	/** A search that found no resume answers nothing: no link, or a count of zero. */
	@Test
	public void noResumeFoundNoAnswer() {
		CcpJsonRepresentation withoutLink = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityInstantMessagePositionReply.Fields.groupChatId, GROUP_CHAT)
				.put(VisEntityInstantMessagePositionReply.Fields.replyTo, MESSAGE_OF_THE_JOB_POSTING)
				.put(VisEntityInstantMessagePositionReply.Fields.resumesCount, 0);
		VisBusinessReplyTheJobPostingWithItsResumes.INSTANCE.execute(withoutLink);
		CcpJsonRepresentation withZeroResumes = withoutLink.put(VisEntityInstantMessagePositionReply.Fields.linkToShowResumes, "https://onias.site/curriculos/buscar?filtro=abc");
		VisBusinessReplyTheJobPostingWithItsResumes.INSTANCE.execute(withZeroResumes);
		assertEquals(0, TELEGRAM.sentMessages.size());
	}

	/**
	 * The averages in Brazilian format, as money for the claims (an average no resume informed as "não informada"), and the
	 * answer answers the message of the job posting.
	 */
	@Test
	public void theAnswerHasTheCountTheAveragesAndTheLink() {
		CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityInstantMessagePositionReply.Fields.groupChatId, GROUP_CHAT)
				.put(VisEntityInstantMessagePositionReply.Fields.replyTo, MESSAGE_OF_THE_JOB_POSTING)
				.put(VisEntityInstantMessagePositionReply.Fields.resumesCount, 12)
				.put(VisEntityInstantMessagePositionReply.Fields.avgClt, 9500.0)
				.put(VisEntityInstantMessagePositionReply.Fields.avgExperience, 4.46)
				.put(VisEntityInstantMessagePositionReply.Fields.linkToShowResumes, "https://onias.site/curriculos/buscar?filtro=abc&email=" + SHARING_EMAIL);
		VisBusinessReplyTheJobPostingWithItsResumes.INSTANCE.execute(record);
		List<CcpJsonRepresentation> answers = TELEGRAM.sentTo(GROUP_CHAT);
		assertEquals(1, answers.size());
		assertEquals(MESSAGE_OF_THE_JOB_POSTING, answers.get(0).getAsLongNumber(CcpJsonCommonsFields.replyTo));
		assertEquals("Foram encontrados 12 currículos (média CLT R$ 9.500,00, média PJ não informada, média de experiência 4,5 anos)\n"
				+ "<a href=\"https://onias.site/curriculos/buscar?filtro=abc&email=" + SHARING_EMAIL + "\">Ver quais são estes currículos</a>",
				answers.get(0).getAsString(JnJsonCommonsFields.message));
	}

	private void saveThePosting(String text) {
		CcpJsonRepresentation posting = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityInstantMessagePositionReply.Fields.groupChatId, GROUP_CHAT)
				.put(VisEntityInstantMessagePositionReply.Fields.replyTo, MESSAGE_OF_THE_JOB_POSTING)
				.put(VisEntityInstantMessagePositionReply.Fields.description, text);
		VisEntityInstantMessagePositionReply.ENTITY.save(posting);
	}

	/** The link of the resumes in the answer, kept to delete its record after the test. */
	private String linkOf(String message) {
		String start = "<a href=\"";
		int linkStart = message.indexOf(start) + start.length();
		int linkEnd = message.indexOf("\"", linkStart);
		String link = message.substring(linkStart, linkEnd);
		this.savedRecords.add(this.keyOf(link));
		return link;
	}

	private CcpJsonRepresentation keyOf(String link) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityInstantMessagePositionReply.Fields.linkToShowResumes, link);
	}

	/** A text of this run with skills of the dictionary, from 100 to 5,000 characters. */
	private String textWithSkills() {
		return "Vaga " + this.run + " para pessoa desenvolvedora backend com Java, Spring Boot, SQL e Git, "
				+ "trabalhando com APIs REST, testes automatizados e metodologias ageis como Scrum.";
	}
}
