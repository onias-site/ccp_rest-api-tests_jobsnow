package com.vis.business.recruiter.resumes;

import com.vis.business.position.VisPositionOfTheRecruiter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.CcpDbRequester;
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
import com.vis.business.position.VisBusinessPublishPositionsInTheGroups;
import com.vis.entities.RecordingEmailSender;
import com.vis.entities.VisEntityPosition;
import com.vis.entities.VisEntityPositionResumes;
import com.vis.entities.VisEntityResume;

/**
 * 2026-11-24, against the local Elasticsearch (with the templates and texts of the script 116): the resume just saved goes
 * once to the position that receives the resumes instantly; the daily process sends the position of the frequency daily
 * the resumes of the last 24 hours that match it, keeping the whole list for "see all"; the recruiter of such a position
 * leaves the daily e-mail of every resume; the process of 10:00 publishes the position of the day in its groups, in two
 * messages when it does not fit in one, the second answering the first. The position asks a combination no stored resume
 * has (ddd 69 with the three flags), so only the resume of the test matches it.
 */
public class ResumesOfThePositionsSendingTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> RecordingHtmlMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> RecordingEmailSender.INSTANCE;
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
	}

	private final long now = System.currentTimeMillis();

	private final String resumesEmail = "vaga" + this.now + "@empresa-teste.com.br";

	private final List<CcpJsonRepresentation> savedPositions = new ArrayList<>();

	private final List<CcpJsonRepresentation> savedResumes = new ArrayList<>();

	@Before
	public void clearTheRecords() {
		RecordingEmailSender.SENT.clear();
		RecordingEmailSender.MESSAGES.clear();
		RecordingHtmlMessenger.SENT.clear();
	}

	@After
	public void deleteWhatWasSaved() {
		this.savedPositions.forEach(position -> VisEntityPosition.ENTITY.deleteAnyWhere(position));
		this.savedResumes.forEach(resume -> VisEntityResume.ENTITY.deleteAnyWhere(resume));
	}

	private static String daysAhead(long now, int days) {
		return String.valueOf(now + days * 86_400_000L);
	}

	private CcpJsonRepresentation position(String title, String frequencyType, boolean mustSendInGroups, List<CcpJsonRepresentation> contacts) {
		CcpJsonRepresentation expiration = new CcpJsonRepresentation("{\"min\":\"" + this.now + "\",\"max\":\"" + daysAhead(this.now, 2) + "\"}");
		CcpJsonRepresentation sort = new CcpJsonRepresentation("{\"type\":\"asc\",\"field\":[\"clt\"]}");
		String description = "position used to test the sending of its resumes, with a description long enough to pass the minimum of one hundred characters";
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.recruiter, this.resumesEmail)
				.put(VisEntityPosition.Fields.yearMonth, VisPositionOfTheRecruiter.getCurrentYearMonth())
				.put(VisEntityPosition.Fields.title, title)
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.clt, new CcpJsonRepresentation("{\"min\":99000,\"max\":100000}"))
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(69))
				.put(VisEntityPosition.Fields.description, description)
				.put(VisEntityPosition.Fields.disponibility, 30)
				.put(VisEntityPosition.Fields.expiration, expiration)
				.put(VisEntityPosition.Fields.seniority, List.of("pleno"))
				.put(VisEntityPosition.Fields.pcd, true)
				.put(VisEntityPosition.Fields.travel, true)
				.put(VisEntityPosition.Fields.negotiableClaim, true)
				.put(VisEntityPosition.Fields.sort, Arrays.asList(sort))
				.put(VisEntityPosition.Fields.contact, contacts)
				.put(VisEntityPosition.Fields.temporallyJobTime, 0)
				.put(VisEntityPosition.Fields.frequencyType, frequencyType)
				.put(VisEntityPosition.Fields.mustSendInGroups, mustSendInGroups)
				.put(VisEntityPosition.Fields.resumesEmail, this.resumesEmail);
		VisEntityPosition.ENTITY.save(position);
		this.savedPositions.add(position);
		return position;
	}

	private static List<CcpJsonRepresentation> anEmailContact() {
		return List.of(new CcpJsonRepresentation("{\"type\":\"email\",\"value\":\"selecao@empresa-teste.com.br\"}"));
	}

	private CcpJsonRepresentation resume(String name) {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, name + this.now + "@jobsnow.com")
				.put(VisEntityResume.Fields.ddd, Arrays.asList(69))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 10)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/" + name + "-" + this.now)
				.put(VisEntityResume.Fields.clt, 99500)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 6)
				.put(VisEntityResume.Fields.negotiableClaim, true)
				.put(VisEntityResume.Fields.pcd, true)
				.put(VisEntityResume.Fields.travel, true)
				.put(VisEntityResume.Fields.spokenLanguage, List.of())
				.put(VisEntityResume.Fields.notAllowedCompany, List.of())
				.put(VisEntityResume.Fields.skill, List.of(new CcpJsonRepresentation("{\"skill\":\"PHP\",\"word\":\"PHP\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}")));
		VisEntityResume.ENTITY.save(resume);
		this.savedResumes.add(resume);
		return resume;
	}

	private long sentWithSubject(String subject) {
		String expected = "[" + this.resumesEmail + "] | " + subject;
		return RecordingEmailSender.SENT.stream().filter(sent -> sent.equalsIgnoreCase(expected)).count();
	}

	private static void refresh(String... indexes) {
		CcpDbRequester requester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		for (String index : indexes) {
			requester.refresh(index);
		}
	}

	@Test
	public void theResumeJustSavedGoesOnceToThePositionOfTheInstantFrequency() {
		String title = "Vaga instantanea " + this.now;
		this.position(title, "instantly", false, anEmailContact());
		refresh("vis_position");
		CcpJsonRepresentation resume = this.resume("instantaneo");
		assertEquals(RecordingEmailSender.SENT.toString(), 1, this.sentWithSubject("Cv recebido para a vaga " + title));
		VisEntityResume.ENTITY.save(resume);
		assertEquals("the next save of the same resume sends nothing", 1, this.sentWithSubject("Cv recebido para a vaga " + title));
	}

	@Test
	public void theDailyProcessSendsTheResumesOfTheLastDayAndKeepsTheWholeList() {
		String title = "Vaga diaria " + this.now;
		this.position(title, "daily", false, anEmailContact());
		this.resume("diario");
		refresh("vis_position", "vis_resume", "jn_grouper");
		assertTrue("the recruiter of the position leaves the daily e-mail of every resume",
				VisBusinessSendResumesToRecruiters.getRecruitersWithPositions().contains(this.resumesEmail.toLowerCase()));
		CcpJsonRepresentation counters = VisBusinessSendDailyResumesOfThePositions.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
		// a resume of an earlier run of this test, saved in the last 24 hours, may match the position too
		String expected = "[" + this.resumesEmail + "] | ";
		List<String> ofThePosition = RecordingEmailSender.SENT.stream().filter(sent -> sent.startsWith(expected) && sent.endsWith(" Cvs recebidos para a vaga " + title)).toList();
		assertEquals(counters.toString() + RecordingEmailSender.SENT, 1, ofThePosition.size());
		CcpJsonRepresentation listKey = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPositionResumes.Fields.email, this.resumesEmail)
				.put(VisEntityPositionResumes.Fields.yearMonth, VisPositionOfTheRecruiter.getCurrentYearMonth())
				.put(VisEntityPositionResumes.Fields.title, title);
		CcpJsonRepresentation list = VisEntityPositionResumes.ENTITY.getOneById(listKey);
		int total = list.getAsJsonList(VisEntityPositionResumes.Fields.resumes).size();
		assertEquals("the subject counts the whole list", expected + total + " Cvs recebidos para a vaga " + title, ofThePosition.get(0));
		String message = RecordingEmailSender.MESSAGES.stream().filter(sent -> sent.contains(title)).findFirst().orElse("");
		assertTrue("the opening links the list of the position", message.contains("/meu-recrutamento/curriculos-da-vaga?email="));
	}

	@Test
	public void thePositionOfTheDayIsPublishedInItsGroupsInTwoMessagesWhenItIsLong() {
		String title = "Vaga dos grupos " + this.now;
		List<CcpJsonRepresentation> contacts = new ArrayList<>();
		for (int index = 0; index < 9; index++) {
			contacts.add(new CcpJsonRepresentation("{\"type\":\"link\",\"value\":\"https://empresa-teste.com.br/vagas/" + "x".repeat(450) + index + "\"}"));
		}
		this.position(title, "none", true, contacts);
		refresh("jn_grouper");
		VisBusinessPublishPositionsInTheGroups.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
		List<String> ofThePosition = new ArrayList<>();
		for (int index = 0; index < RecordingHtmlMessenger.SENT.size(); index++) {
			String sent = RecordingHtmlMessenger.SENT.get(index);
			boolean first = sent.contains(title);
			if (false == first) {
				continue;
			}
			ofThePosition.add(sent);
			ofThePosition.add(RecordingHtmlMessenger.SENT.get(index + 1));
			break;
		}
		assertEquals(RecordingHtmlMessenger.SENT.toString(), 2, ofThePosition.size());
		String firstPiece = ofThePosition.get(0);
		String secondPiece = ofThePosition.get(1);
		assertTrue(firstPiece, firstPiece.contains("<b>ENVIO SEU CURRICULO PARA 2.000 RHS") && firstPiece.contains("/meus-dados/sobre-mim\">BASTA CLICAR AQUI</a></b>"));
		assertTrue("no salary in the message", false == firstPiece.contains("99000") && false == firstPiece.contains("99.000"));
		assertTrue(firstPiece.split(" \\| ", 3)[2].length() <= 4096);
		String firstChat = firstPiece.split(" \\| ")[0];
		assertTrue("the first piece answers no message", firstPiece.startsWith(firstChat + " | 0 | "));
		assertTrue(secondPiece, secondPiece.startsWith(firstChat + " | ") && false == secondPiece.startsWith(firstChat + " | 0 | "));
		assertTrue(secondPiece.contains("<b>RECRUTADOR, GRATUITAMENTE") && secondPiece.contains("/meu-recrutamento/vagas\">BASTA CLICAR AQUI</a></b>"));
	}
}
