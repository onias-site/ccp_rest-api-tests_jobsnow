package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Year;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.ElasticsearchLocal;
import com.ccp.especifications.email.CcpEmailSender;
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
import com.vis.entities.VisEntityRecruiterResumes;
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
		CcpInstanceProvider<CcpEmailSender> email = () -> RecordingResumesEmailSender.INSTANCE;

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
		// the validator runs before the transformers: the domain and the kind of the e-mail go ready
		String domain = email.substring(email.indexOf('@') + 1);
		CcpJsonRepresentation recruiter = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityRecruiter.Fields.email, email)
				.put(VisEntityRecruiter.Fields.source, "test")
				.put(VisEntityRecruiter.Fields.domain, domain)
				.put(VisEntityRecruiter.Fields.professional, false == domain.equals("gmail.com"));
		VisEntityRecruiter.ENTITY.save(recruiter);
		this.createdRecruiters.add(email);
	}

	private static CcpJsonRepresentation sendTo(String... recruiters) {
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
	public void theEmailFoldsTheRegionsBeyondTheThirdOne() {
		String recruiter = this.recruiter("regions", "gmail.com");
		this.saveRecruiter(recruiter);
		String job = "Analista de cinco regioes " + this.run;
		CcpJsonRepresentation fiveRegions = this.resume("regioes", job).put(VisEntityResume.Fields.ddd, Arrays.asList(11, 21, 31, 41, 51));
		this.saveResume(fiveRegions);

		sendTo(recruiter);

		String message = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(recruiter);
		assertTrue("the regions beyond the third one are folded: " + message, message.contains("<details>"));
		CcpJsonRepresentation row = rowOf(rowsOf(recruiter), job);
		assertEquals(5, row.getAsStringList(VisRecruiterResumesFields.regions).size());
	}

	@Test
	public void theEmailShowsTheFirst40ResumesAndLinksToTheScreenWithAllOfThem() {
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
		int linksToResumes = message.split("/recrutador/ver-curriculo\\?email=", -1).length - 1;
		assertEquals(VisBusinessSendResumesToRecruiters.RESUMES_IN_THE_EMAIL, linksToResumes);
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
				.put(VisEntityResume.Fields.notAllowedCompany, Arrays.asList("Empresa Proibida " + this.run));
		this.saveResume(forbidding);

		CcpJsonRepresentation counters = sendTo(professional, personal);

		assertEquals(2, counters.getAsIntegerNumber(VisRecruiterResumesFields.emailsSent).intValue());
		List<CcpJsonRepresentation> professionalRows = rowsOf(professional);
		assertTrue(hasRowOf(professionalRows, allowedJob));
		assertFalse(hasRowOf(professionalRows, forbiddingJob));
		List<CcpJsonRepresentation> personalRows = rowsOf(personal);
		assertTrue("the company forbidden is not the one of a personal e-mail", hasRowOf(personalRows, forbiddingJob));

		String message = RecordingResumesEmailSender.LAST_MESSAGE_BY_RECIPIENT.get(professional);
		assertTrue(message.contains(allowedJob));
		assertFalse(message.contains(forbiddingJob));
		assertTrue(message.contains("/recrutador/descadastrar?email="));
		assertTrue(message.contains("/recrutador/ver-curriculo?email="));
		assertFalse("no placeholder is left unresolved", message.matches("(?s).*\\{[a-zA-Z]+\\}.*"));
		CcpJsonRepresentation allowedRow = rowOf(professionalRows, allowedJob);
		assertEquals("Sua última visualização: nunca", allowedRow.getAsString(VisRecruiterResumesFields.lastView));
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
