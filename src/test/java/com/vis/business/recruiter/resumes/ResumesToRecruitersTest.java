package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Year;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Assume;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.ElasticsearchLocal;
import com.ccp.especifications.email.CcpEmailProvider;
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
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;
import com.vis.entities.VisEntityRecruiter;
import com.vis.entities.VisEntityRecruiterFirstEmail;
import com.vis.entities.VisEntityRecruiterQueueCursor;
import com.vis.entities.VisEntityRecruiterResumes;
import com.vis.entities.VisEntityRecruiterSignUpReturn;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityViewedResume;
import com.vis.services.VisServiceRecruiterResumes;

/**
 * The daily e-mail of resumes to the recruiters ({@link VisBusinessSendResumesToRecruiters}): the row of each resume,
 * the suggested alias, the companies a resume forbids, the signed links, and the whole flow (the e-mail, the list of the
 * screen "see all", opening a resume, giving it an alias and unsubscribing). Each test uses e-mails and addresses of its
 * own run, and the run of the process is restricted to the recruiters of the test.
 *
 * <p>Needs {@code recruiterLinksSecret} in {@code application_properties}.
 */
public class ResumesToRecruitersTest {

	static {
		CcpInstanceProvider<CcpEmailProvider> email = () -> RecordingResumesEmailSender.INSTANCE;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.instantMessenger,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				email
		);
		List<CcpBulkItem> seeds = VisEntityRecruiterResumes.INSTANCE.getFirstRecordsToInsert();
		for (CcpBulkItem seed : seeds) {
			seed.entity.save(seed.json);
		}
	}

	private final long run = System.currentTimeMillis();

	private final int currentYear = Year.now().getValue();

	private String candidate(String who) {
		return "candidato" + who + this.run + "@jobsnow.com";
	}

	private String linkedin(String who) {
		return "https://www.linkedin.com/in/candidato" + who + this.run;
	}

	private String recruiter(String who, String domain) {
		return "recrutador" + who + this.run + "@" + domain;
	}

	private final List<String> createdResumes = new java.util.ArrayList<>();

	private final List<String> createdRecruiters = new java.util.ArrayList<>();

	@After
	public void deleteWhatTheTestCreated() {
		for (String email : this.createdResumes) {
			CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email);
			VisEntityResume.ENTITY.deleteAnyWhere(key);
		}
		for (String email : this.createdRecruiters) {
			CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, email);
			VisEntityRecruiter.ENTITY.deleteAnyWhere(key);
			VisEntityRecruiterResumes.ENTITY.delete(key);
			VisEntityRecruiterFirstEmail.ENTITY.delete(key);
			VisEntityRecruiterSignUpReturn.ENTITY.delete(key);
		}
	}

	private static CcpJsonRepresentation skill(String name) {
		return new CcpJsonRepresentation("{\"skill\":\"" + name + "\",\"word\":\"" + name + "\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[]}");
	}

	private static CcpJsonRepresentation spokenLanguage(String name, int level) {
		return new CcpJsonRepresentation("{\"name\":\"" + name + "\",\"level\":" + level + ",\"id\":0}");
	}

	/** A resume as the candidate sends it (the experience in years). */
	private CcpJsonRepresentation resume(String who, String desiredJob) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.candidate(who))
				.put(VisEntityResume.Fields.ddd, Arrays.asList(21, 11))
				.put(VisEntityResume.Fields.desiredJob, desiredJob)
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 6)
				.put(VisEntityResume.Fields.linkedinAddress, this.linkedin(who))
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.clt, 8_000)
				.put(VisEntityResume.Fields.pj, 12_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(skill("JAVA")))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(spokenLanguage("Inglês", 2)));
	}

	private void saveResume(CcpJsonRepresentation resume) {
		VisEntityResume.ENTITY.save(resume);
		String email = resume.getAsString(VisEntityResume.Fields.email);
		this.createdResumes.add(email);
	}

	private void saveRecruiter(String email) {
		this.saveRecruiter(email, "test");
	}

	private void saveRecruiter(String email, String source) {
		// the validator runs before the transformers: the domain and the kind of the e-mail go ready
		String domain = email.substring(email.indexOf('@') + 1);
		CcpJsonRepresentation recruiter = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityRecruiter.Fields.email, email)
				.put(VisEntityRecruiter.Fields.source, source)
				.put(VisEntityRecruiter.Fields.domain, domain)
				.put(VisEntityRecruiter.Fields.professional, false == domain.equals("gmail.com"));
		VisEntityRecruiter.ENTITY.save(recruiter);
		this.createdRecruiters.add(email);
	}

	private static CcpJsonRepresentation sendTo(String... recruiters) {
		// the e-mails go out only from Monday to Friday, and the process takes the day from the clock
		CcpTimeDecorator now = new CcpTimeDecorator();
		Assume.assumeFalse(now.isWeekend());
		// the process queries what the test has just written
		ElasticsearchLocal.refresh("jn_versionable", "vis_resume", "vis_recruiter", "vis_viewed_resume");
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisRecruiterResumesFields.recruiters, Arrays.asList(recruiters));
		CcpJsonRepresentation counters = VisBusinessSendResumesToRecruiters.INSTANCE.execute(input);
		return counters;
	}

	/** The rows the process wrote for the screen "see all" of the recruiter. */
	private static List<CcpJsonRepresentation> rowsOf(String recruiter) {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterResumes.Fields.email, recruiter);
		CcpJsonRepresentation recruiterResumes = VisEntityRecruiterResumes.ENTITY.getOneById(key);
		List<CcpJsonRepresentation> rows = recruiterResumes.getAsJsonList(VisEntityRecruiterResumes.Fields.resumes);
		return rows;
	}

	private static CcpJsonRepresentation rowOf(List<CcpJsonRepresentation> rows, String desiredJob) {
		for (CcpJsonRepresentation row : rows) {
			String job = row.getAsStringList(VisRecruiterResumesFields.job).toString();
			if(job.contains(desiredJob)) {
				return row;
			}
		}
		throw new AssertionError("No row of the resume " + desiredJob + " in " + rows);
	}

	private static boolean hasRowOf(List<CcpJsonRepresentation> rows, String desiredJob) {
		return rows.stream().anyMatch(row -> row.getAsStringList(VisRecruiterResumesFields.job).toString().contains(desiredJob));
	}

	/** The input of the services of the links of the e-mail, as the front end sends it. */
	private static CcpJsonRepresentation linkOf(String recruiter, CcpJsonRepresentation row) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, recruiter)
				.put(VisRecruiterResumesFields.resumeId, row.getAsString(VisRecruiterResumesFields.resumeId))
				.put(VisRecruiterResumesFields.token, row.getAsString(VisRecruiterResumesFields.token));
	}

	@Test
	public void theEmailEndsAskingToReportTroublesOnOpeningTheLinkedinOfTheCandidateThenTheFooter() {
		List<String> endings = new java.util.ArrayList<>();
		for (CcpBulkItem seed : VisEntityRecruiterResumes.INSTANCE.getFirstRecordsToInsert()) {
			boolean templateSeed = seed.json.containsAllFields(JnJsonCommonsFields.templateId, JnJsonCommonsFields.message);
			if(false == templateSeed) {
				continue;
			}
			String templateId = seed.json.getAsString(JnJsonCommonsFields.templateId);
			boolean recruiterTemplate = templateId.endsWith("VisNotifyRecruiterAboutResumes");
			if(false == recruiterTemplate) {
				continue;
			}
			String message = seed.json.getAsString(JnJsonCommonsFields.message);
			endings.add(message);
		}
		assertEquals(2, endings.size());
		assertTrue(endings.stream().anyMatch(message -> message.endsWith("<p>Caso haja problemas em abrir o LinkedIn do candidato, nos avise.</p>" + com.jn.business.messages.JnEmailFooter.portuguese.getValue() + "</body></html>")));
		assertTrue(endings.stream().anyMatch(message -> message.endsWith("<p>If you have any trouble opening the candidate's LinkedIn, please let us know.</p>" + com.jn.business.messages.JnEmailFooter.english.getValue() + "</body></html>")));
	}

	@Test
	public void aCompanyIsForbiddenByAPartOfTheDomainOrByAName() {
		assertTrue(VisBusinessSendResumesToRecruiters.isForbiddenCompany("itau.com.br", List.of("Itaú")));
		assertTrue(VisBusinessSendResumesToRecruiters.isForbiddenCompany("itau-unibanco.com.br", List.of("Itaú Unibanco")));
		assertTrue(VisBusinessSendResumesToRecruiters.isForbiddenCompany("rh.ibm.com", List.of("IBM")));
		assertFalse("a short name is compared only with whole parts of the domain", VisBusinessSendResumesToRecruiters.isForbiddenCompany("bimbo.com", List.of("IBM")));
		assertFalse(VisBusinessSendResumesToRecruiters.isForbiddenCompany("empresa.com.br", List.of("Outra Empresa S/A")));
		assertFalse(VisBusinessSendResumesToRecruiters.isForbiddenCompany("empresa.com.br", List.of()));
	}

	@Test
	public void aCompanyNameMadeOnlyOfSymbolsForbidsNothingButTheNextNamesStillCount() {
		assertFalse(VisBusinessSendResumesToRecruiters.isForbiddenCompany("empresa.com.br", List.of("***")));
		assertTrue(VisBusinessSendResumesToRecruiters.isForbiddenCompany("itau.com.br", List.of("***", "Itaú")));
	}

	private static CcpJsonRepresentation withSortKeys(Object... keys) {
		return CcpOtherConstants.EMPTY_JSON.put(VisRecruiterResumesFields.sortKeys, Arrays.asList(keys));
	}

	@Test
	public void theResumesAreSortedByTheFirstKeyThatDiffers() {
		CcpJsonRepresentation lowerClt = withSortKeys(1, 8_000d, 12_000d, 2020);
		CcpJsonRepresentation higherClt = withSortKeys(1, 9_000d, 0d, 2010);

		assertTrue(VisBusinessSendResumesToRecruiters.BY_SORT_KEYS.compare(lowerClt, higherClt) < 0);
		assertTrue(VisBusinessSendResumesToRecruiters.BY_SORT_KEYS.compare(higherClt, lowerClt) > 0);
		assertEquals(0, VisBusinessSendResumesToRecruiters.BY_SORT_KEYS.compare(lowerClt, withSortKeys(1, 8_000d, 12_000d, 2020)));
	}

	@Test
	public void theEmailLinksTheRegionsBeyondTheThirdOne() {
		String recruiter = this.recruiter("regions", "gmail.com");
		this.saveRecruiter(recruiter);
		String job = "Analista de cinco regioes " + this.run;
		CcpJsonRepresentation fiveRegions = this.resume("regioes", job).put(VisEntityResume.Fields.ddd, Arrays.asList(11, 21, 31, 41, 51));
		this.saveResume(fiveRegions);

		sendTo(recruiter);

		List<CcpJsonRepresentation> rows = rowsOf(recruiter);
		CcpJsonRepresentation row = rowOf(rows, job);
		assertEquals(5, row.getAsStringList(VisRecruiterResumesFields.regions).size());

		// the e-mail of the process shows only the first 30 resumes of the week, and the local database may hold more
		// than 30 ahead of this one: the folding is checked in an e-mail with this row alone, sent to another recruiter
		// (jn_email_message_sent lets one e-mail of the kind go to each recipient per day)
		String readerOfTheRow = this.recruiter("regions-row", "gmail.com");
		this.saveRecruiter(readerOfTheRow);
		List<CcpJsonRepresentation> onlyThisRow = Arrays.asList(row);
		VisBusinessSendResumesToRecruiters.sendTheEmail(readerOfTheRow, onlyThisRow, "http://localhost:3000", "https://api.jobsnow.com");
		String message = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(readerOfTheRow);
		assertTrue("the regions beyond the third one are counted by a link: " + message, message.contains("+2 regiões</a>"));
		assertFalse("the regions beyond the third one are not lines of the e-mail: " + message, message.contains("<p>Porto Alegre – RS</p>"));
		assertTrue("the regions beyond the third one are in the title of the link: " + message, message.contains("title=\"Curitiba – PR, Porto Alegre – RS\""));
	}

	/**
	 * Bug of 2026-11-12: with 40 rows the e-mail reached 107 KB and Gmail, which clips a message beyond 102 KB, hid its
	 * end. The e-mail shows 30.
	 */
	@Test
	public void theEmailShowsAtMost30ResumesSoGmailDoesNotClipIt() {
		assertEquals(30, VisBusinessSendResumesToRecruiters.RESUMES_IN_THE_EMAIL);
	}

	/**
	 * Bugs of 2026-11-12: the total of the subject and of the text did not match what the recruiter could see, and the
	 * screen "see all" opened empty. The subject, the opening text and the paragraph "see all" count every resume left
	 * after the filters, the table shows the first {@code RESUMES_IN_THE_EMAIL}, and the screen "see all" (the service
	 * the front end calls) answers all of them, in the order of the e-mail, with the same signed links.
	 */
	@Test
	public void theSubjectTheTextAndTheScreenSeeAllCountTheSameResumes() {
		int beyondTheTable = 5;
		int resumesOfTheList = VisBusinessSendResumesToRecruiters.RESUMES_IN_THE_EMAIL + beyondTheTable;
		String source = this.recruiter("count-source", "gmail.com");
		this.saveRecruiter(source);
		for (int index = 0; index < resumesOfTheList; index++) {
			this.saveResume(this.resume("contagem" + index, "Analista contado " + index + " " + this.run));
		}

		sendTo(source);

		// the whole process: the subject counts the list the screen "see all" shows
		List<CcpJsonRepresentation> rowsOfTheProcess = rowsOf(source);
		String messageOfTheProcess = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(source);
		assertTrue("the subject counts the list of the screen \"see all\" (" + rowsOfTheProcess.size() + "): " + messageOfTheProcess.substring(0, 80),
				messageOfTheProcess.startsWith(rowsOfTheProcess.size() + " currículos de "));
		assertTrue(messageOfTheProcess.contains("(" + rowsOfTheProcess.size() + " no total)"));

		// a list of a known size, as the e-mail to a recruiter whose resumes passed every filter
		List<CcpJsonRepresentation> list = rowsOfTheProcess.subList(0, resumesOfTheList);
		String reader = this.recruiter("count-reader", "gmail.com");
		this.saveRecruiter(reader);
		VisBusinessSendResumesToRecruiters.sendTheEmail(reader, list, "http://localhost:3000", "https://api.jobsnow.com");
		String message = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(reader);

		assertTrue("the subject counts the whole list: " + message.substring(0, 80), message.startsWith(resumesOfTheList + " currículos de "));
		assertTrue("the opening text counts the whole list", message.contains("(" + resumesOfTheList + " no total)"));
		int shown = VisBusinessSendResumesToRecruiters.RESUMES_IN_THE_EMAIL;
		assertTrue("the paragraph \"see all\" counts the table and the list", message.contains("Estes são os " + shown + " primeiros de " + resumesOfTheList + " currículos."));
		// the link carries the recipient: with another account logged in, the screen asks for the login of the recipient
		// instead of reading the list of the wrong account (bug of 2026-11-12)
		String encodedReader = java.net.URLEncoder.encode(reader, java.nio.charset.StandardCharsets.UTF_8);
		assertTrue("the link \"see all\" carries the recipient", message.contains("/meu-recrutamento/curriculos-da-semana?email=" + encodedReader + "\""));
		List<String> linkedResumes = new java.util.ArrayList<>();
		Matcher resumeLink = Pattern.compile("/recrutador/ver-curriculo\\?email=[^\"]*?resume=([^&\"]+)").matcher(message);
		while (resumeLink.find()) {
			String resumeId = resumeLink.group(1);
			if(false == linkedResumes.contains(resumeId)) {
				linkedResumes.add(resumeId);
			}
		}
		assertEquals("the table shows the first ones", shown, linkedResumes.size());

		// the screen "see all": every resume of the list, in the order of the e-mail
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, reader);
		CcpJsonRepresentation screen = VisServiceRecruiterResumes.GetResumesOfTheWeek.execute(request);
		List<CcpJsonRepresentation> screenRows = screen.getAsJsonList(VisEntityRecruiterResumes.Fields.resumes);
		assertEquals("the screen \"see all\" has the whole list", resumesOfTheList, screenRows.size());
		for (int index = 0; index < resumesOfTheList; index++) {
			String expectedId = list.get(index).getAsString(VisRecruiterResumesFields.resumeId);
			String screenId = screenRows.get(index).getAsString(VisRecruiterResumesFields.resumeId);
			assertEquals("the order of the e-mail, row " + index, expectedId, screenId);
			String expectedToken = list.get(index).getAsString(VisRecruiterResumesFields.token);
			String screenToken = screenRows.get(index).getAsString(VisRecruiterResumesFields.token);
			assertEquals("the same signed link, row " + index, expectedToken, screenToken);
		}
		for (int index = 0; index < shown; index++) {
			String screenId = screenRows.get(index).getAsString(VisRecruiterResumesFields.resumeId);
			assertEquals("the table of the e-mail is the beginning of the screen, row " + index, screenId, linkedResumes.get(index));
		}
	}

	@Test
	public void theEmailShowsOnlyTheFirstResumesAndLinksToTheScreenWithAllOfThem() {
		String recruiter = this.recruiter("many", "gmail.com");
		this.saveRecruiter(recruiter);
		int resumesOfTheTest = VisBusinessSendResumesToRecruiters.RESUMES_IN_THE_EMAIL + 1;
		for (int index = 0; index < resumesOfTheTest; index++) {
			this.saveResume(this.resume("muitos" + index, "Analista numero " + index + " " + this.run));
		}

		sendTo(recruiter);

		List<CcpJsonRepresentation> rows = rowsOf(recruiter);
		assertTrue("the screen \"see all\" has every resume: " + rows.size(), rows.size() >= resumesOfTheTest);
		String message = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(recruiter);
		assertTrue(message.contains("/meu-recrutamento/curriculos-da-semana"));
		// distinct resumes, not links: "+N regiões" and "ver as outras N habilidades" also link to the resume, so a resume
		// of the local base with many regions or skills has more than one link (until 2026-11-10 the test counted links
		// and failed with 50 once the roteiros left such resumes among the first 40)
		Set<String> linkedResumes = new HashSet<>();
		Matcher resumeLink = Pattern.compile("/recrutador/ver-curriculo\\?email=[^\"]*?resume=([^&\"]+)").matcher(message);
		while (resumeLink.find()) {
			linkedResumes.add(resumeLink.group(1));
		}
		assertEquals(VisBusinessSendResumesToRecruiters.RESUMES_IN_THE_EMAIL, linkedResumes.size());
	}

	@Test
	public void theLinksAreSignedForTheirPurposeAndValues() {
		String recruiter = this.recruiter("links", "empresa.com.br");
		String token = VisRecruiterLinks.view.sign(recruiter, "resume1");
		assertTrue(VisRecruiterLinks.view.matches(token, recruiter, "resume1"));
		assertFalse(VisRecruiterLinks.view.matches(token, recruiter, "resume2"));
		assertFalse(VisRecruiterLinks.view.matches(token, "outro" + recruiter, "resume1"));
		String unsubscribeToken = VisRecruiterLinks.unsubscribe.sign(recruiter);
		assertNotEquals(unsubscribeToken, VisRecruiterLinks.view.sign(recruiter));
		assertTrue(VisRecruiterLinks.unsubscribe.matches(unsubscribeToken, recruiter));
	}

	@Test
	public void theRowShowsTheResumeInTheColumnsOfTheEmail() {
		CcpJsonRepresentation rankings = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("JAVA"), 5).put(new CcpFieldName("SQL"), 1);
		// as the resume is stored: the numbers come back as Double and the experience is the year it began
		CcpJsonRepresentation stored = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, "hashdoemail")
				.put(VisEntityResume.Fields.ddd, Arrays.asList(31d, 11d, 21d, 19d))
				.put(VisEntityResume.Fields.desiredJob, "Arquiteto de software")
				.put(VisEntityResume.Fields.lastJob, "Desenvolvedor")
				.put(VisEntityResume.Fields.disponibility, 15d)
				.put(VisEntityResume.Fields.experience, (double) (this.currentYear - 6))
				.put(VisEntityResume.Fields.resumeType, 1d)
				.put(VisEntityResume.Fields.temporallyJobTime, 6d)
				.put(VisEntityResume.Fields.clt, 9_000d)
				.put(VisEntityResume.Fields.negotiableClaim, true)
				.put(VisEntityResume.Fields.travel, true)
				.put(VisEntityResume.Fields.skill, Arrays.asList(skill("KOTLIN"), skill("JAVA"), skill("SQL")))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(
						spokenLanguage("Francês", 1), spokenLanguage("Espanhol", 2), spokenLanguage("Inglês", 1), spokenLanguage("Alemão", 2), spokenLanguage("English", 2)))
				.put(VisRecruiterResumesFields.rankings, rankings)
				.put(VisRecruiterResumesFields.insertion, 0L)
				.put(VisRecruiterResumesFields.lastChange, 0L)
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese);

		CcpJsonRepresentation row = VisBusinessBuildResumeRow.INSTANCE.execute(stored);

		assertEquals(List.of(
				"Pretensão Salarial CLT: R$ 9.000,00",
				"Pretensão Salarial PJ: Não Informada",
				"Pretensões salariais negociáveis: Sim",
				"Experiência em anos: 6 (Senior)"), row.getAsStringList(VisRecruiterResumesFields.whatMatters));
		assertEquals(List.of("São Paulo – SP", "Campinas – SP", "Rio de Janeiro – RJ", "Belo Horizonte – MG"), row.getAsStringList(VisRecruiterResumesFields.regions));
		assertEquals(List.of(
				"English fluente", "Espanhol fluente",
				"SQL", "JAVA", "KOTLIN",
				"Francês intermediário", "Inglês intermediário", "Alemão fluente"), row.getAsStringList(VisRecruiterResumesFields.languagesAndSkills));
		assertEquals(List.of(
				"Tipo do currículo: Tecnologia da Informação",
				"Última função / papel / emprego / ocupação exercida: Desenvolvedor",
				"Função / papel / emprego / ocupação desejada: Arquiteto de software"), row.getAsStringList(VisRecruiterResumesFields.job));
		assertEquals(List.of(
				"Disponibilidade para novas oportunidades em 15 dias",
				"Mínimo de tempo de projeto aceito por este perfil: 6 meses",
				"Disponibilidade para viagens: Sim",
				"Pessoa com deficiência (PCD): Não"), row.getAsStringList(VisRecruiterResumesFields.otherInformation));
		assertEquals("English fluente_Espanhol fluente_SQL_JAVA_KOTLIN_R$ 9.000,00_São Paulo – SP_Campinas – SP_Rio de Janeiro – RJ_6 Anos",
				row.getAsString(VisRecruiterResumesFields.defaultAlias));
	}

	/**
	 * Decision of 2026-11-12: with every other key tied (skills, views, rankings, type, CLT and PJ), the most experienced
	 * resume comes first. Until then the least experienced came first.
	 */
	@Test
	public void withEveryOtherKeyTiedTheMostExperiencedComeFirst() {
		CcpJsonRepresentation rankings = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("JAVA"), 5);
		CcpJsonRepresentation junior = this.storedResumeWithExperience("junior", 2);
		CcpJsonRepresentation senior = this.storedResumeWithExperience("senior", 15);
		CcpJsonRepresentation middle = this.storedResumeWithExperience("pleno", 6);
		List<CcpJsonRepresentation> resumes = Arrays.asList(junior, senior, middle);

		List<CcpJsonRepresentation> rows = VisBusinessSendResumesToRecruiters.getResumesOfTheEmail(resumes, rankings, new java.util.HashMap<>(), new java.util.HashMap<>());

		List<String> expectedOrder = List.of("senior", "pleno", "junior");
		List<String> jobsInOrder = new java.util.ArrayList<>();
		for (CcpJsonRepresentation row : rows) {
			String job = row.getAsStringList(VisRecruiterResumesFields.job).toString();
			for (String who : expectedOrder) {
				boolean thisOne = job.contains("Desenvolvedor " + who);
				if(thisOne) {
					jobsInOrder.add(who);
					break;
				}
			}
		}
		assertEquals(expectedOrder, jobsInOrder);
	}

	/** A resume as it is stored (the experience is the year it began), equal to the others but in the experience. */
	private CcpJsonRepresentation storedResumeWithExperience(String who, int years) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, "hash" + who + this.run)
				.put(VisEntityResume.Fields.linkedinAddress, this.linkedin(who))
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11d))
				.put(VisEntityResume.Fields.desiredJob, "Desenvolvedor " + who)
				.put(VisEntityResume.Fields.disponibility, 5d)
				.put(VisEntityResume.Fields.experience, (double) (this.currentYear - years))
				.put(VisEntityResume.Fields.resumeType, 1d)
				.put(VisEntityResume.Fields.temporallyJobTime, 0d)
				.put(VisEntityResume.Fields.clt, 9_000d)
				.put(VisEntityResume.Fields.skill, Arrays.asList(skill("JAVA")));
	}

	@Test
	public void theFluentEnglishAndSpanishComeFirstInTheSubjectWithoutCountingAndTheOtherLanguagesStayOut() {
		CcpJsonRepresentation rankings = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("JAVA"), 5).put(new CcpFieldName("SQL"), 1);
		CcpJsonRepresentation firstInput = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(spokenLanguage("Francês", 2), spokenLanguage("Inglês", 2)));
		CcpJsonRepresentation secondInput = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(spokenLanguage("Espanhol", 1)));
		List<String> firstSkills = List.of("SQL", "JAVA", "S03", "S04", "S05", "S06", "S07", "S08", "S09", "S10", "S11");
		List<CcpJsonRepresentation> firstItems = VisBusinessSendResumesToRecruiters.getSubjectItems(firstInput, firstSkills, rankings);
		List<CcpJsonRepresentation> secondItems = VisBusinessSendResumesToRecruiters.getSubjectItems(secondInput, List.of("JAVA"), rankings);
		CcpJsonRepresentation firstRow = CcpOtherConstants.EMPTY_JSON.put(VisRecruiterResumesFields.subjectItems, firstItems);
		CcpJsonRepresentation secondRow = CcpOtherConstants.EMPTY_JSON.put(VisRecruiterResumesFields.subjectItems, secondItems);

		CcpJsonRepresentation subjectValues = VisBusinessSendResumesToRecruiters.getSubjectValues(List.of(firstRow, secondRow));

		String topSkills = subjectValues.getAsString(VisRecruiterResumesFields.topSkills);
		List<String> named = Arrays.asList(topSkills.split(", "));
		assertEquals("the fluent English first, then the best ranked skills", List.of("Inglês fluente", "SQL", "JAVA"), named.subList(0, 3));
		assertEquals("the fluent English plus the 10 skills", 11, named.size());
		assertFalse("another language is not in the subject", topSkills.contains("Francês"));
		assertFalse("a language that is not fluent is not in the subject", topSkills.contains("Espanhol"));
		assertEquals("only the eleventh skill is left; the language does not count", 1, subjectValues.getAsIntegerNumber(VisRecruiterResumesFields.otherSkills).intValue());
	}

	@Test
	public void aResumeOnlyOfTheDdd10AcceptsOnlyHomeOffice() {
		CcpJsonRepresentation stored = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.ddd, Arrays.asList(10d));
		List<String> regions = VisBusinessBuildResumeRow.getRegions(stored, JnLanguage.portuguese);
		assertEquals(List.of("Aceita somente homeoffice"), regions);
	}

	@Test
	public void theRecruiterReceivesTheResumesExceptTheOnesThatForbidTheirCompany() {
		String professional = this.recruiter("pro", "empresaproibida" + this.run + ".com.br");
		String personal = this.recruiter("gmail", "gmail.com");
		this.saveRecruiter(professional);
		this.saveRecruiter(personal);
		String allowedJob = "Engenheiro liberado " + this.run;
		String forbiddingJob = "Engenheiro restrito " + this.run;
		this.saveResume(this.resume("liberado", allowedJob));
		CcpJsonRepresentation forbidding = this.resume("restrito", forbiddingJob)
				.put(VisEntityResume.Fields.notAllowedCompany, Arrays.asList("empresaproibida" + this.run + ".com.br"));
		this.saveResume(forbidding);

		CcpJsonRepresentation counters = sendTo(professional, personal);

		assertEquals(2, counters.getAsIntegerNumber(VisRecruiterResumesFields.emailsSent).intValue());
		List<CcpJsonRepresentation> professionalRows = rowsOf(professional);
		assertTrue(hasRowOf(professionalRows, allowedJob));
		assertFalse(hasRowOf(professionalRows, forbiddingJob));
		List<CcpJsonRepresentation> personalRows = rowsOf(personal);
		assertTrue("the company forbidden is not the one of a personal e-mail", hasRowOf(personalRows, forbiddingJob));

		String messageOfTheProcess = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(professional);
		assertFalse(messageOfTheProcess.contains(forbiddingJob));

		// the e-mail of the process shows only the first 30 resumes of the week, and the local database may hold more
		// than 30 ahead of the allowed one: its row is checked in an e-mail with the rows of this test alone, sent to
		// another recruiter of the same company (jn_email_message_sent lets one e-mail of the kind go to each recipient per day)
		String colleague = this.recruiter("pro-row", "empresaproibida" + this.run + ".com.br");
		this.saveRecruiter(colleague);
		CcpJsonRepresentation rowOfTheAllowedJob = rowOf(professionalRows, allowedJob);
		List<CcpJsonRepresentation> rowsOfThisTest = Arrays.asList(rowOfTheAllowedJob);
		VisBusinessSendResumesToRecruiters.sendTheEmail(colleague, rowsOfThisTest, "http://localhost:3000", "https://api.jobsnow.com");
		String message = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(colleague);
		assertTrue(message.contains(allowedJob));
		assertTrue(message.contains("/recrutador/descadastrar?email="));
		assertTrue(message.contains("/recrutador/ver-curriculo?email="));
		assertFalse("no placeholder is left unresolved", message.matches("(?s).*\\{[a-zA-Z]+\\}.*"));
		CcpJsonRepresentation allowedRow = rowOf(professionalRows, allowedJob);
		assertEquals("Você nunca visualizou este currículo", allowedRow.getAsString(VisRecruiterResumesFields.lastView));
		assertFalse("no red background on the line", message.contains("background-color: red"));
		assertEquals("Ver currículo", allowedRow.getAsString(VisRecruiterResumesFields.viewLabel));
		assertFalse(allowedRow.getAsBoolean(VisRecruiterResumesFields.viewed));
	}

	@Test
	public void openingAResumeSavesTheViewAndTheNextListShowsItsAlias() {
		String recruiter = this.recruiter("viewer", "gmail.com");
		this.saveRecruiter(recruiter);
		String job = "Analista visto " + this.run;
		this.saveResume(this.resume("visto", job));
		sendTo(recruiter);
		CcpJsonRepresentation row = rowOf(rowsOf(recruiter), job);
		CcpJsonRepresentation link = linkOf(recruiter, row);

		CcpJsonRepresentation opened = VisServiceRecruiterResumes.ViewResumeFromEmail.execute(link);

		assertEquals(this.linkedin("visto"), opened.getAsString(VisEntityResume.Fields.linkedinAddress));
		assertEquals(row.getAsString(VisRecruiterResumesFields.defaultAlias), opened.getAsString(VisEntityViewedResume.Fields.alias));
		CcpJsonRepresentation renamed = link.put(VisEntityViewedResume.Fields.alias, "Meu analista preferido");
		VisServiceRecruiterResumes.SaveResumeAliasFromEmail.execute(renamed);
		CcpJsonRepresentation reopened = VisServiceRecruiterResumes.ViewResumeFromEmail.execute(link);
		assertEquals("opening again keeps the alias", "Meu analista preferido", reopened.getAsString(VisEntityViewedResume.Fields.alias));
		// the view is in the group of views of each ddd of the resume, with the fields of the resume and the id of the view
		CcpJsonRepresentation storedView = VisEntityViewedResume.ENTITY.getOneById(CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedin("visto"))
				.put(VisEntityViewedResume.Fields.email, recruiter));
		String viewId = VisEntityViewedResume.ENTITY.calculateId(storedView);
		List<Object> viewDdds = storedView.getAsObjectList(VisEntityViewedResume.Fields.ddd);
		assertFalse(viewDdds.isEmpty());
		for (Object viewDdd : viewDdds) {
			int ddd = ((Number) viewDdd).intValue();
			String parentId = com.vis.entities.VisEntityVirtualViewedResumeDddGrouper.ENTITY.calculateId(CcpOtherConstants.EMPTY_JSON.put(com.vis.entities.VisEntityVirtualViewedResumeDddGrouper.Fields.ddd, ddd));
			List<String> children = com.jn.utils.JnGroupers.getChildren(false, "vis_virtual_viewed_resume_ddd_grouper", "vis_viewed_resume", parentId);
			long childrenOfTheView = children.stream().filter(child -> child.contains("\"id\":\"" + viewId + "\"")).count();
			assertEquals("ddd " + ddd, 1, childrenOfTheView);
			String childOfTheView = children.stream().filter(child -> child.contains("\"id\":\"" + viewId + "\"")).findFirst().get();
			assertTrue(childOfTheView, childOfTheView.contains("\"experience\""));
		}

		sendTo(recruiter);
		CcpJsonRepresentation viewedRow = rowOf(rowsOf(recruiter), job);
		assertEquals("Meu analista preferido", viewedRow.getAsString(VisRecruiterResumesFields.viewLabel));
		assertTrue(viewedRow.getAsBoolean(VisRecruiterResumesFields.viewed));
		assertFalse(viewedRow.getAsString(VisRecruiterResumesFields.lastView).contains("nunca"));

		CcpJsonRepresentation weekResumes = VisServiceRecruiterResumes.GetResumesOfTheWeek.execute(CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, recruiter));
		assertTrue(hasRowOf(weekResumes.getAsJsonList(VisEntityRecruiterResumes.Fields.resumes), job));

		CcpJsonRepresentation viewKey = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedin("visto"))
				.put(VisEntityViewedResume.Fields.email, recruiter);
		VisEntityViewedResume.ENTITY.deleteAnyWhere(viewKey);
	}

	@Test
	public void aLinkSignedForAnotherRecruiterIsRefused() {
		String recruiter = this.recruiter("signed", "gmail.com");
		this.saveRecruiter(recruiter);
		String job = "Analista assinado " + this.run;
		this.saveResume(this.resume("assinado", job));
		sendTo(recruiter);
		CcpJsonRepresentation row = rowOf(rowsOf(recruiter), job);
		CcpJsonRepresentation forged = linkOf(recruiter, row).put(JnJsonCommonsFields.email, this.recruiter("intruso", "gmail.com"));
		try {
			VisServiceRecruiterResumes.ViewResumeFromEmail.execute(forged);
			fail("a link of another recruiter was accepted");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.UNHAUTHORIZED, expected.status);
		}
	}

	/** The company is taken from the e-mail of the recruiter; a personal e-mail has no company to forbid. */
	@Test
	public void theCompanyOfTheRecruiterIsTakenFromTheirEmail() {
		List<String> forbidding = List.of("Consultoria Exemplo");
		assertTrue(VisBusinessSendResumesToRecruiters.isTheCompanyOfTheRecruiterForbidden("ana@consultoriaexemplo.com.br", forbidding));
		assertTrue(VisBusinessSendResumesToRecruiters.isTheCompanyOfTheRecruiterForbidden("ana@rh.consultoria-exemplo.com.br", forbidding));
		assertFalse(VisBusinessSendResumesToRecruiters.isTheCompanyOfTheRecruiterForbidden("ana@outraempresa.com.br", forbidding));
		assertFalse("a personal e-mail has no company", VisBusinessSendResumesToRecruiters.isTheCompanyOfTheRecruiterForbidden("consultoriaexemplo@gmail.com", List.of("Gmail")));
		assertFalse(VisBusinessSendResumesToRecruiters.isTheCompanyOfTheRecruiterForbidden("ana@consultoriaexemplo.com.br", List.of()));
	}

	private void assertTheLinkIsClosed(CcpJsonRepresentation link) {
		try {
			VisServiceRecruiterResumes.ViewResumeFromEmail.execute(link);
			fail("a company forbidden after the e-mail opened the resume");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals("the same answer of a resume no longer active", CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
		try {
			CcpJsonRepresentation renamed = link.put(VisEntityViewedResume.Fields.alias, "Apelido proibido");
			VisServiceRecruiterResumes.SaveResumeAliasFromEmail.execute(renamed);
			fail("a company forbidden after the e-mail gave an alias to the resume");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
	}

	/**
	 * The candidate allowed the company, the e-mail went to its recruiters and then the candidate forbade the company:
	 * the links already sent stop opening the resume (no LinkedIn, no view saved), while a recruiter of another company
	 * and one of a personal e-mail still open it.
	 */
	@Test
	public void aCompanyForbiddenAfterTheEmailCanNoLongerOpenTheResume() {
		String forbiddenRecruiter = this.recruiter("depois", "consultoria" + this.run + ".com.br");
		String colleague = this.recruiter("depois-colega", "consultoria" + this.run + ".com.br");
		String otherCompany = this.recruiter("depois-outra", "outraempresa" + this.run + ".com.br");
		String personal = this.recruiter("depois-pessoal", "gmail.com");
		this.saveRecruiter(forbiddenRecruiter);
		this.saveRecruiter(colleague);
		this.saveRecruiter(otherCompany);
		this.saveRecruiter(personal);
		String job = "Analista proibido depois " + this.run;
		CcpJsonRepresentation resume = this.resume("proibidodepois", job);
		this.saveResume(resume);

		sendTo(forbiddenRecruiter, colleague, otherCompany, personal);
		CcpJsonRepresentation linkOfTheForbidden = linkOf(forbiddenRecruiter, rowOf(rowsOf(forbiddenRecruiter), job));
		CcpJsonRepresentation linkOfTheColleague = linkOf(colleague, rowOf(rowsOf(colleague), job));
		CcpJsonRepresentation linkOfTheOtherCompany = linkOf(otherCompany, rowOf(rowsOf(otherCompany), job));
		CcpJsonRepresentation linkOfThePersonal = linkOf(personal, rowOf(rowsOf(personal), job));

		CcpJsonRepresentation forbidding = resume.put(VisEntityResume.Fields.notAllowedCompany, Arrays.asList("consultoria" + this.run + ".com.br"));
		VisEntityResume.ENTITY.save(forbidding);

		this.assertTheLinkIsClosed(linkOfTheForbidden);
		this.assertTheLinkIsClosed(linkOfTheColleague);
		CcpJsonRepresentation viewOfTheForbidden = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedin("proibidodepois"))
				.put(VisEntityViewedResume.Fields.email, forbiddenRecruiter);
		assertFalse("no view is saved", VisEntityViewedResume.ENTITY.exists(viewOfTheForbidden));

		CcpJsonRepresentation openedByTheOtherCompany = VisServiceRecruiterResumes.ViewResumeFromEmail.execute(linkOfTheOtherCompany);
		CcpJsonRepresentation openedByThePersonal = VisServiceRecruiterResumes.ViewResumeFromEmail.execute(linkOfThePersonal);
		assertEquals(this.linkedin("proibidodepois"), openedByTheOtherCompany.getAsString(VisEntityResume.Fields.linkedinAddress));
		assertEquals(this.linkedin("proibidodepois"), openedByThePersonal.getAsString(VisEntityResume.Fields.linkedinAddress));
		for (String recruiter : List.of(otherCompany, personal)) {
			CcpJsonRepresentation viewKey = CcpOtherConstants.EMPTY_JSON
					.put(VisEntityViewedResume.Fields.linkedin, this.linkedin("proibidodepois"))
					.put(VisEntityViewedResume.Fields.email, recruiter);
			VisEntityViewedResume.ENTITY.deleteAnyWhere(viewKey);
		}
	}

	private final List<String> forgedViews = new java.util.ArrayList<>();

	/**
	 * Writes straight to the history (without the decorators, that would date it now) a view of the recruiter made the
	 * given days ago.
	 */
	private void viewedDaysAgo(String recruiter, String linkedinAddress, int days) {
		String emailHash = VisBusinessSendResumesToRecruiters.getEmailHash(recruiter);
		String linkedinHash = VisBusinessSendResumesToRecruiters.getLinkedinHash(linkedinAddress);
		CcpJsonRepresentation view = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.email, emailHash)
				.put(VisEntityViewedResume.Fields.linkedin, linkedinHash)
				.put(VisEntityViewedResume.Fields.alias, "Visto faz tempo");
		long timestamp = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000;
		CcpJsonRepresentation historyRow = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, VisEntityViewedResume.ENTITY.getEntityMetaData().entityName)
				.put(JnJsonCommonsFields.id, view.removeFields(VisEntityViewedResume.Fields.alias).asUgglyJson())
				.put(JnJsonCommonsFields.json, view.asPrettyJson())
				.put(JnJsonCommonsFields.operation, "create")
				.put(JnJsonCommonsFields.date, "forjado pelo teste")
				.put(JnJsonCommonsFields.timestamp, timestamp);
		String historyId = "forged-view-" + days + "-" + emailHash;
		ElasticsearchLocal.save("jn_versionable", historyId, historyRow);
		this.forgedViews.add(historyId);
	}

	@After
	public void deleteTheForgedViews() {
		for (String historyId : this.forgedViews) {
			ElasticsearchLocal.delete("jn_versionable", historyId);
		}
	}

	@Test
	public void theRecruiterWhoseLastViewIsOlderThan35DaysStopsReceivingTheEmail() {
		String stoppedViewing = this.recruiter("parou", "gmail.com");
		String stillViewing = this.recruiter("segue", "gmail.com");
		String viewedLongAgoAndRecently = this.recruiter("voltou", "gmail.com");
		String neverViewed = this.recruiter("nunca", "gmail.com");
		for (String recruiter : List.of(stoppedViewing, stillViewing, viewedLongAgoAndRecently, neverViewed)) {
			this.saveRecruiter(recruiter);
		}
		this.saveResume(this.resume("dias", "Analista dos 35 dias " + this.run));
		int limit = VisBusinessSendResumesToRecruiters.DAYS_WITHOUT_VIEWS;
		this.viewedDaysAgo(stoppedViewing, this.linkedin("antigo"), limit + 1);
		this.viewedDaysAgo(stillViewing, this.linkedin("recente"), limit - 1);
		// what counts is the most recent view of the recruiter, of any resume
		this.viewedDaysAgo(viewedLongAgoAndRecently, this.linkedin("antigo"), limit + 10);
		this.viewedDaysAgo(viewedLongAgoAndRecently, this.linkedin("recente"), 1);

		CcpJsonRepresentation counters = sendTo(stoppedViewing, stillViewing, viewedLongAgoAndRecently, neverViewed);

		assertEquals(3, counters.getAsIntegerNumber(VisRecruiterResumesFields.emailsSent).intValue());
		assertFalse(RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.containsKey(stoppedViewing));
		assertTrue(RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.containsKey(stillViewing));
		assertTrue(RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.containsKey(viewedLongAgoAndRecently));
		assertTrue(RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.containsKey(neverViewed));
		CcpJsonRepresentation stoppedKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterResumes.Fields.email, stoppedViewing);
		assertFalse("no list of \"see all\" either", VisEntityRecruiterResumes.ENTITY.exists(stoppedKey));

		assertEquals(1, counters.getAsIntegerNumber(VisRecruiterResumesFields.recruitersInactivated).intValue());
		CcpJsonRepresentation stoppedRecruiter = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, stoppedViewing);
		assertFalse("inactivated", VisEntityRecruiter.ENTITY.exists(stoppedRecruiter));
		assertTrue(VisEntityRecruiter.ENTITY.getTwinEntity().exists(stoppedRecruiter));
		assertEquals("test", VisEntityRecruiter.ENTITY.getTwinEntity().getOneById(stoppedRecruiter).getAsString(VisEntityRecruiter.Fields.source));
		for (String active : List.of(stillViewing, viewedLongAgoAndRecently, neverViewed)) {
			CcpJsonRepresentation activeRecruiter = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, active);
			assertTrue(VisEntityRecruiter.ENTITY.exists(activeRecruiter));
		}
	}

	private static List<CcpJsonRepresentation> skillsNamed(String prefix, int howMany) {
		List<CcpJsonRepresentation> skills = new java.util.ArrayList<>();
		for (int index = 0; index < howMany; index++) {
			skills.add(skill(prefix + index));
		}
		return skills;
	}

	private static CcpJsonRepresentation withRankings(CcpJsonRepresentation rankings, String prefix, int howMany, double ranking) {
		CcpJsonRepresentation result = rankings;
		for (int index = 0; index < howMany; index++) {
			result = result.put(new CcpFieldName(prefix + index), ranking);
		}
		return result;
	}

	/** A resume as stored, with the given skills: what the process reads from {@code vis_resume}. */
	private CcpJsonRepresentation storedResume(String who, String job, String skillPrefix, int skills) {
		CcpJsonRepresentation resume = this.resume(who, job);
		this.saveResume(resume);
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(resume);
		return stored.put(VisEntityResume.Fields.skill, skillsNamed(skillPrefix, skills));
	}

	/** The rows of the e-mail, not the sending: the order is proved on any day of the week. */
	@Test
	public void theResumesGoByEnoughSkillsThenByViewsThenByTheAverageRanking() {
		CcpJsonRepresentation fewSkills = this.storedResume("few", "Poucas " + this.run, "FEW", 9);
		CcpJsonRepresentation noViews = this.storedResume("noviews", "Sem views " + this.run, "BEST", 10);
		CcpJsonRepresentation worseRanking = this.storedResume("worse", "Pior ranking " + this.run, "WORSE", 10);
		CcpJsonRepresentation betterRanking = this.storedResume("better", "Melhor ranking " + this.run, "BETTER", 10);
		CcpJsonRepresentation mostViewed = this.storedResume("most", "Mais visto " + this.run, "WORST", 10);
		CcpJsonRepresentation rankings = CcpOtherConstants.EMPTY_JSON;
		rankings = withRankings(rankings, "FEW", 9, 1d);
		rankings = withRankings(rankings, "BEST", 10, 1d);
		rankings = withRankings(rankings, "BETTER", 10, 2d);
		rankings = withRankings(rankings, "WORSE", 10, 5d);
		rankings = withRankings(rankings, "WORST", 10, 900d);
		java.util.Map<String, Long> viewsByResume = new java.util.HashMap<>();
		viewsByResume.put(VisBusinessSendResumesToRecruiters.getLinkedinHash(this.linkedin("few")), 50L);
		viewsByResume.put(VisBusinessSendResumesToRecruiters.getLinkedinHash(this.linkedin("worse")), 1L);
		viewsByResume.put(VisBusinessSendResumesToRecruiters.getLinkedinHash(this.linkedin("better")), 1L);
		viewsByResume.put(VisBusinessSendResumesToRecruiters.getLinkedinHash(this.linkedin("most")), 3L);
		List<CcpJsonRepresentation> resumes = Arrays.asList(fewSkills, noViews, worseRanking, betterRanking, mostViewed);

		List<CcpJsonRepresentation> rows = VisBusinessSendResumesToRecruiters.getResumesOfTheEmail(resumes, rankings, new java.util.HashMap<>(), viewsByResume);

		assertEquals(5, rows.size());
		assertTrue(hasRowOf(rows.subList(0, 1), "Mais visto " + this.run));
		assertTrue(hasRowOf(rows.subList(1, 2), "Melhor ranking " + this.run));
		assertTrue(hasRowOf(rows.subList(2, 3), "Pior ranking " + this.run));
		assertTrue(hasRowOf(rows.subList(3, 4), "Sem views " + this.run));
		assertTrue(hasRowOf(rows.subList(4, 5), "Poucas " + this.run));
	}

	@Test
	public void aRecruiterCountsOnceInTheViewsOfAResume() {
		java.util.Map<String, Long> lastViewByPair = new java.util.HashMap<>();
		lastViewByPair.put("recruiter1|resumeA", 10L);
		lastViewByPair.put("recruiter2|resumeA", 20L);
		lastViewByPair.put("recruiter1|resumeB", 30L);

		java.util.Map<String, Long> viewsByResume = VisBusinessSendResumesToRecruiters.getViewsByResume(lastViewByPair);

		assertEquals(Long.valueOf(2), viewsByResume.get("resumeA"));
		assertEquals(Long.valueOf(1), viewsByResume.get("resumeB"));
	}

	/** An unknown skill has no number: it stays out of the sum and of the division. */
	@Test
	public void theAverageRankingCountsOnlyTheSkillsThatHaveARanking() {
		CcpJsonRepresentation rankings = CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("JAVA"), 2d)
				.put(new CcpFieldName("SQL"), 4d);

		double average = VisBusinessSendResumesToRecruiters.getAverageRanking(List.of("JAVA", "SQL", "UNKNOWN"), rankings);
		double nothingRanked = VisBusinessSendResumesToRecruiters.getAverageRanking(List.of("UNKNOWN"), rankings);

		assertEquals(3d, average, 0d);
		assertEquals(Double.MAX_VALUE, nothingRanked, 0d);
	}

	/** A document of the legacy index, as the scroll hands it: numbers as texts, and most of the fields absent. */
	private static CcpJsonRepresentation legacyCandidate(String email) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.id, email)
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.tipoVaga, "2")
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.ddd, "11")
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.pretensaoClt, "8000")
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.ultimaProfissao, "Analista de Recrutamento");
	}

	private String legacyTable(CcpJsonRepresentation candidate) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisRecruiterResumesFields.legacyCandidate, candidate)
				.put(JnJsonCommonsFields.email, candidate.getAsString(VisBusinessBuildLegacyCandidateTable.Legacy.id))
				.put(VisRecruiterResumesFields.frontendUrl, "http://localhost:3000");
		CcpJsonRepresentation withTable = VisBusinessBuildLegacyCandidateTable.INSTANCE.execute(input);
		return withTable.getAsString(VisRecruiterResumesFields.resumesTable);
	}

	/** The candidate never informed these: the row of a stored resume would read the absence as "no". */
	@Test
	public void theInvitationShowsNothingTheCandidateDidNotInform() {
		String html = this.legacyTable(legacyCandidate(this.candidate("legacy")));

		assertEquals("one row besides the header", 3, html.split("<tr>").length);
		assertTrue(html, html.contains("Pretensão Salarial CLT"));
		assertTrue(html, html.contains("Analista de Recrutamento"));
		assertFalse(html, html.contains("Experiência em anos"));
		assertFalse(html, html.contains("negociáveis"));
		assertFalse(html, html.contains("viagens"));
		assertFalse(html, html.contains("temporárias"));
		assertFalse(html, html.contains("imediata"));
		assertFalse("the preview has no link", html.contains("recrutador/ver-curriculo"));
		assertTrue(html, html.contains("<p>Ver currículo</p>"));
	}

	@Test
	public void theInvitationShowsWhatTheCandidateInformed() {
		CcpJsonRepresentation candidate = legacyCandidate(this.candidate("informed"))
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.experiencia, 2020d)
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.pretensaoNegociavel, true)
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.disponibilidade, "15");

		String html = this.legacyTable(candidate);

		assertTrue(html, html.contains("Experiência em anos"));
		assertTrue(html, html.contains("Pretensões salariais negociáveis: Sim"));
		assertTrue(html, html.contains("15 dias"));
	}

	/** One day of availability in the singular (until 2026-11-10 it came out as "em 1 dias", defect R11-1). */
	@Test
	public void oneDayOfAvailabilityIsSingular() {
		CcpJsonRepresentation candidate = legacyCandidate(this.candidate("oneDay"))
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.disponibilidade, "1");

		String html = this.legacyTable(candidate);

		assertTrue(html, html.contains("em 1 dia<"));
		assertFalse(html, html.contains("1 dias"));
	}

	/** Only the home office DDD when that is all the candidate accepts. */
	@Test
	public void aCandidateWhoAcceptsOnlyHomeOfficeHasNoRegion() {
		CcpJsonRepresentation candidate = legacyCandidate(this.candidate("remote"))
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.ddd, "0")
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.homeoffice, true);

		String html = this.legacyTable(candidate);

		assertTrue(html, html.contains("Aceita somente homeoffice"));
	}

	/** Goes through the real template, and leaves the e-mail in target for a person to read. */
	@Test
	public void theInvitationGoesWithTheLinkOfTheFormAndTheTable() throws java.io.IOException {
		List<CcpBulkItem> seeds = VisEntityRecruiterResumes.INSTANCE.getInvitationSeeds();
		for (CcpBulkItem seed : seeds) {
			seed.entity.save(seed.json);
		}
		String email = this.candidate("invited");
		CcpJsonRepresentation candidate = legacyCandidate(email)
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.experiencia, 2019d)
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.pretensaoPj, "12000")
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.pretensaoNegociavel, true)
				.put(VisBusinessBuildLegacyCandidateTable.Legacy.profissaoDesejada, "Tech Recruiter");
		String table = this.legacyTable(candidate);

		VisBusinessInviteLegacyCandidates.sendTheInvitation(email, table, "https://www.jobsnow.com.br/meus-dados/sobre-mim");

		String message = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(email);
		assertTrue(message.contains("https://www.jobsnow.com.br/meus-dados/sobre-mim"));
		assertTrue(message.contains("Como o recrutador verá o seu currículo"));
		assertTrue(message.contains("Tech Recruiter"));
		assertFalse("no placeholder is left unresolved", message.matches("(?s).*\\{[a-zA-Z]+\\}.*"));
		java.nio.file.Files.writeString(java.nio.file.Path.of("target", "convite-curriculo-exemplo.html"), message);
	}

	/** Reads the real index, so the read that precedes the sending is proved on any day of the week. */
	@Test
	public void everyRecruiterIsReadAndOrderedByTheQueuePosition() {
		ElasticsearchLocal.refresh("vis_recruiter");

		List<CcpJsonRepresentation> recruiters = VisBusinessSendResumesToRecruiters.getRecruiters();
		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(recruiters, new java.util.HashMap<>(), 0L, "", java.util.Set.of(), 0L, "");

		assertTrue(recruiters.size() > 1000);
		assertEquals(recruiters.size(), inOrder.size());
		List<String> positions = new java.util.ArrayList<>();
		// the recent viewers, the first e-mails, Telegram and the legacy views go before the queue and have no position in it
		inOrder.stream().filter(recruiter -> recruiter.containsAllFields(VisRecruiterResumesFields.queuePosition)).forEach(recruiter -> positions.add(recruiter.getAsString(VisRecruiterResumesFields.queuePosition)));
		List<String> sortedPositions = new java.util.ArrayList<>(positions);
		java.util.Collections.sort(sortedPositions);
		assertEquals(sortedPositions, positions);
	}

	/** A return by the sign-up gives another 35 days; an old return does not, and the recruiter is inactivated again. */
	@Test
	public void aRecentReturnByTheSignUpKeepsTheRecruiterAndAnOldOneDoesNot() {
		String recentReturn = this.recruiter("voltou", "gmail.com");
		String oldReturn = this.recruiter("voltouhatempos", "gmail.com");
		this.saveRecruiter(recentReturn);
		this.saveRecruiter(oldReturn);
		this.saveResume(this.resume("retorno", "Analista do retorno " + this.run));
		int limit = VisBusinessSendResumesToRecruiters.DAYS_WITHOUT_VIEWS;
		this.viewedDaysAgo(recentReturn, this.linkedin("antigo"), limit + 10);
		this.viewedDaysAgo(oldReturn, this.linkedin("antigo"), limit + 10);
		long now = System.currentTimeMillis();
		long oneDay = 24L * 60 * 60 * 1000;
		CcpJsonRepresentation recentKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterSignUpReturn.Fields.email, recentReturn);
		CcpJsonRepresentation oldKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterSignUpReturn.Fields.email, oldReturn);
		VisEntityRecruiterSignUpReturn.ENTITY.save(recentKey.put(VisEntityRecruiterSignUpReturn.Fields.returnedAt, now - oneDay));
		VisEntityRecruiterSignUpReturn.ENTITY.save(oldKey.put(VisEntityRecruiterSignUpReturn.Fields.returnedAt, now - (limit + 1) * oneDay));
		ElasticsearchLocal.refresh("vis_recruiter_sign_up_return");

		CcpJsonRepresentation counters = sendTo(recentReturn, oldReturn);

		assertEquals(1, counters.getAsIntegerNumber(VisRecruiterResumesFields.emailsSent).intValue());
		assertTrue(RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.containsKey(recentReturn));
		assertFalse(RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.containsKey(oldReturn));
		CcpJsonRepresentation stillActive = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, recentReturn);
		CcpJsonRepresentation inactivated = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, oldReturn);
		assertTrue(VisEntityRecruiter.ENTITY.exists(stillActive));
		assertFalse(VisEntityRecruiter.ENTITY.exists(inactivated));
	}

	/** A recruiter of the sign-up leaves the waiting for the first e-mail only when it goes out. */
	@Test
	public void theFirstEmailOfARecruiterOfTheSignUpIsRecorded() {
		String signedUp = this.recruiter("cadastro", "gmail.com");
		String other = this.recruiter("legado", "gmail.com");
		this.saveRecruiter(signedUp, VisEntityRecruiter.Sources.login.getValue());
		this.saveRecruiter(other);
		this.saveResume(this.resume("primeiro", "Analista do primeiro e-mail " + this.run));

		CcpJsonRepresentation counters = sendTo(signedUp, other);

		assertEquals(2, counters.getAsIntegerNumber(VisRecruiterResumesFields.emailsSent).intValue());
		CcpJsonRepresentation signedUpKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterFirstEmail.Fields.email, signedUp);
		CcpJsonRepresentation otherKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterFirstEmail.Fields.email, other);
		assertTrue(VisEntityRecruiterFirstEmail.ENTITY.exists(signedUpKey));
		assertFalse(VisEntityRecruiterFirstEmail.ENTITY.exists(otherKey));
	}

	/** Writes on the records the real run uses, one per queue, and puts back what it found there. */
	@Test
	public void aRunRestrictedToSomeRecruitersDoesNotMoveTheQueueButAFullOneDoes() {
		this.restrictedRunDoesNotMoveTheQueue(VisEntityRecruiterQueueCursor.CursorNames.resumesToRecruiters, "1" + "a".repeat(40));
		this.restrictedRunDoesNotMoveTheQueue(VisEntityRecruiterQueueCursor.CursorNames.legacyViewersOfResumesToRecruiters, "8".repeat(13) + "a".repeat(40));
	}

	private void restrictedRunDoesNotMoveTheQueue(VisEntityRecruiterQueueCursor.CursorNames queue, String newPosition) {
		String positionBefore = VisBusinessSendResumesToRecruiters.getLastQueuePosition(queue);
		try {
			VisBusinessSendResumesToRecruiters.saveLastQueuePosition(queue, List.of("someone@jobsnow.com"), positionBefore, newPosition);
			assertEquals(positionBefore, VisBusinessSendResumesToRecruiters.getLastQueuePosition(queue));

			VisBusinessSendResumesToRecruiters.saveLastQueuePosition(queue, List.of(), positionBefore, newPosition);
			assertEquals(newPosition, VisBusinessSendResumesToRecruiters.getLastQueuePosition(queue));
		} finally {
			String cursorName = queue.getValue();
			CcpJsonRepresentation cursorKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiterQueueCursor.Fields.cursorName, cursorName);
			if(positionBefore.isEmpty()) {
				VisEntityRecruiterQueueCursor.ENTITY.delete(cursorKey);
			} else {
				VisEntityRecruiterQueueCursor.ENTITY.save(cursorKey.put(VisEntityRecruiterQueueCursor.Fields.lastPosition, positionBefore));
			}
		}
	}

	/** The view reaches the same record the unsubscribe moved to the twin, with the source it had. */
	@Test
	public void aViewMakesAnInactiveRecruiterActiveAgain() {
		String recruiter = this.recruiter("inativo", "gmail.com");
		this.saveRecruiter(recruiter);
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, recruiter);
		VisEntityRecruiter.ENTITY.delete(key);
		assertFalse(VisEntityRecruiter.ENTITY.exists(key));
		assertTrue(VisEntityRecruiter.ENTITY.getTwinEntity().exists(key));

		CcpJsonRepresentation view = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedin("reativa"))
				.put(VisEntityViewedResume.Fields.email, recruiter)
				.put(VisEntityViewedResume.Fields.alias, "Candidato que reativou");
		VisEntityViewedResume.ENTITY.save(view);

		assertTrue(VisEntityRecruiter.ENTITY.exists(key));
		assertFalse(VisEntityRecruiter.ENTITY.getTwinEntity().exists(key));
		assertEquals("test", VisEntityRecruiter.ENTITY.getOneById(key).getAsString(VisEntityRecruiter.Fields.source));
		VisEntityViewedResume.ENTITY.deleteAnyWhere(view);
	}

	/** A view of someone who is no recruiter creates no recruiter. */
	@Test
	public void aViewOfSomeoneWhoIsNotARecruiterChangesNoRecruiter() {
		String someone = this.recruiter("ninguem", "gmail.com");
		CcpJsonRepresentation view = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedin("ninguem"))
				.put(VisEntityViewedResume.Fields.email, someone)
				.put(VisEntityViewedResume.Fields.alias, "Candidato sem recrutador");

		VisEntityViewedResume.ENTITY.save(view);

		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, someone);
		assertFalse(VisEntityRecruiter.ENTITY.exists(key));
		VisEntityViewedResume.ENTITY.deleteAnyWhere(view);
	}

	/** The table does not depend on the day: a resume never viewed has its line without any highlight. */
	@Test
	public void aResumeNeverViewedHasAPlainLine() {
		String recruiter = this.recruiter("plain", "gmail.com");
		CcpJsonRepresentation row = rowToSend("Você nunca visualizou este currículo");

		CcpJsonRepresentation table = VisBusinessRenderResumesTable.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON
				.put(VisRecruiterResumesFields.resumes, Arrays.asList(row))
				.put(JnJsonCommonsFields.email, recruiter)
				.put(VisRecruiterResumesFields.frontendUrl, "http://localhost:3000")
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese));

		String html = table.getAsString(VisRecruiterResumesFields.resumesTable);
		assertTrue(html, html.contains("<p>Você nunca visualizou este currículo</p>"));
		assertFalse(html, html.contains("background-color"));
	}

	/** The column of languages and skills shows its first 15 lines, in the order of the row; the others go to the title of a link to the resume. */
	@Test
	public void theEmailLinksTheLanguagesAndSkillsAfterTheFirst15() {
		String recruiter = this.recruiter("fifteenskills", "gmail.com");
		java.util.List<String> languagesAndSkills = new java.util.ArrayList<>();
		for (int position = 1; position <= 17; position++) {
			languagesAndSkills.add("SKILL" + position + "X");
		}
		CcpJsonRepresentation row = rowToSend("Você nunca visualizou este currículo")
				.put(VisRecruiterResumesFields.languagesAndSkills, languagesAndSkills);

		CcpJsonRepresentation table = VisBusinessRenderResumesTable.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON
				.put(VisRecruiterResumesFields.resumes, Arrays.asList(row))
				.put(JnJsonCommonsFields.email, recruiter)
				.put(VisRecruiterResumesFields.frontendUrl, "http://localhost:3000")
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese));

		String html = table.getAsString(VisRecruiterResumesFields.resumesTable);
		assertTrue(html, html.contains("<p>SKILL1X</p>"));
		assertTrue(html, html.contains("<p>SKILL15X</p>"));
		assertFalse(html, html.contains("<p>SKILL16X</p>"));
		assertFalse(html, html.contains("<p>SKILL17X</p>"));
		assertTrue("the skills left out are in the title of the link: " + html, html.contains("title=\"SKILL16X, SKILL17X\""));
		assertFalse("Gmail drops <details>, the list would show open: " + html, html.contains("<details>"));
		String resumeLink = "http://localhost:3000/recrutador/ver-curriculo?email=";
		int moreLinkStart = html.indexOf("ver as outras 2 habilidades</a>");
		assertTrue(html, moreLinkStart > 0);
		String beforeTheMoreLink = html.substring(0, moreLinkStart);
		int hrefStart = beforeTheMoreLink.lastIndexOf("href=\"");
		assertTrue(html, beforeTheMoreLink.startsWith(resumeLink, hrefStart + 6));
	}

	private static CcpJsonRepresentation rowToSend(String lastView) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisRecruiterResumesFields.resumeId, "resume")
				.put(VisRecruiterResumesFields.whatMatters, Arrays.asList("CLT: R$ 8.000,00"))
				.put(VisRecruiterResumesFields.regions, Arrays.asList("São Paulo – SP"))
				.put(VisRecruiterResumesFields.languagesAndSkills, Arrays.asList("JAVA"))
				.put(VisRecruiterResumesFields.job, Arrays.asList("Desenvolvedor"))
				.put(VisRecruiterResumesFields.otherInformation, Arrays.asList("PCD: não"))
				.put(VisRecruiterResumesFields.lastView, lastView)
				.put(VisRecruiterResumesFields.viewed, false)
				.put(VisRecruiterResumesFields.viewLabel, "Ver currículo")
				.put(VisRecruiterResumesFields.token, "token")
				.put(VisRecruiterResumesFields.insertion, "hoje")
				.put(VisRecruiterResumesFields.lastChange, "hoje");
	}

	/** Calls the e-mail directly: the process runs only from Monday to Friday, and this rule does not depend on the day. */
	@Test
	public void theEmailGoesWithTheOneClickUnsubscribeHeaders() {
		String recruiter = this.recruiter("one+click", "gmail.com");
		this.saveRecruiter(recruiter);
		CcpJsonRepresentation row = rowToSend("Você nunca visualizou este currículo");

		VisBusinessSendResumesToRecruiters.sendTheEmail(recruiter, Arrays.asList(row), "http://localhost:3000", "https://api.jobsnow.com");

		CcpJsonRepresentation emailHeaders = RecordingResumesEmailSender.LAST_HEADERS_BY_RECIPIENT.get(recruiter);
		String token = VisRecruiterLinks.unsubscribe.sign(recruiter);
		String encodedRecruiter = recruiter.replace("+", "%2B").replace("@", "%40");
		String expected = "<https://api.jobsnow.com/recruiter-email-link/one-click-unsubscribe?email=" + encodedRecruiter + "&token=" + token + ">";
		assertEquals(expected, emailHeaders.getAsString(VisBusinessSendResumesToRecruiters.Constants.LIST_UNSUBSCRIBE));
		assertEquals("List-Unsubscribe=One-Click", emailHeaders.getAsString(VisBusinessSendResumesToRecruiters.Constants.LIST_UNSUBSCRIBE_POST));
	}

	@Test
	public void unsubscribingMovesTheRecruiterToTheInactiveOnesAndStopsTheEmails() {
		String recruiter = this.recruiter("leaving", "gmail.com");
		this.saveRecruiter(recruiter);
		this.saveResume(this.resume("saindo", "Analista saindo " + this.run));
		CcpJsonRepresentation unsubscribe = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, recruiter)
				.put(VisRecruiterResumesFields.token, VisRecruiterLinks.unsubscribe.sign(recruiter));

		VisServiceRecruiterResumes.UnsubscribeFromResumesEmail.execute(unsubscribe);
		VisServiceRecruiterResumes.UnsubscribeFromResumesEmail.execute(unsubscribe);

		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, recruiter);
		assertFalse(VisEntityRecruiter.ENTITY.exists(key));
		CcpJsonRepresentation counters = sendTo(recruiter);
		assertEquals(0, counters.getAsIntegerNumber(VisRecruiterResumesFields.emailsSent).intValue());
	}
}
