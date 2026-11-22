package com.vis.business.resume;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityGrouper;
import com.jn.utils.JnGroupers;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityResumeViewsOfTheMonth;
import com.vis.entities.VisEntityViewedResume;
import com.vis.entities.VisEntityVirtualResumeViewsCounter;
import com.vis.json.transformers.VisResumeViewsCounterKeys;
import com.vis.json.fields.validation.VisJsonCommonsFields;
import com.vis.services.VisServiceResume;

/**
 * Proves the views of the month of a resume (the screen "Quem me viu"): every save of {@code vis_viewed_resume} counts
 * the domain of the recruiter in the group of counters of the resume in the month ({@code jn_grouper}, parent
 * {@code vis_resume_views_of_the_month}, {@code @CcpEntityGrouperChildCounter}); each recruiter counts once per month;
 * recruiters of the same domain viewing at the same moment all count; a view brought back from the twin does not count
 * again in the month; the delete everywhere of the resume deletes the groups of every month, and its deactivation keeps
 * them; and the screen reads the month by id ({@code VisBusinessResumeViewersOfTheMonth}).
 */
public class ResumeViewersOfTheMonthTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	/** How many recruiters of the same domain view the resume at the same moment. */
	private static final int SIMULTANEOUS_RECRUITERS = 10;

	/** The first month of the counting by domain. */
	private static final String FIRST_MONTH = "202610";

	private final long run = System.currentTimeMillis();

	private final String candidate = "candidatoquemmeviu" + this.run + "@jobsnow.com";

	private final String linkedin = "https://www.linkedin.com/in/candidatoquemmeviu" + this.run;

	private final String currentMonth = CcpEntityExpurgableOptions.monthly.getFormattedDate();

	private final List<CcpJsonRepresentation> createdViews = new ArrayList<>();

	@After
	public void deleteWhatTheTestCreated() {
		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.candidate);
		VisEntityResume.ENTITY.deleteAnyWhere(resumeKey);
		for (CcpJsonRepresentation view : this.createdViews) {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(view);
		}
		CcpJsonRepresentation groupKey = this.groupKeyOfTheMonth();
		JnEntityGrouper.ENTITY.deleteAnyWhere(groupKey);
		CcpJsonRepresentation firstMonthGroupKey = this.groupKeyOf(FIRST_MONTH);
		JnEntityGrouper.ENTITY.deleteAnyWhere(firstMonthGroupKey);
		CcpJsonRepresentation totalGroupKey = this.groupKeyOfTheTotalOfViews();
		JnEntityGrouper.ENTITY.deleteAnyWhere(totalGroupKey);
	}

	private String parentIdOfTheTotalOfViews() {
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(VisEntityVirtualResumeViewsCounter.Fields.email, sha1(this.candidate));
		return VisEntityVirtualResumeViewsCounter.ENTITY.calculateId(parentJson);
	}

	private CcpJsonRepresentation groupKeyOfTheTotalOfViews() {
		String parentId = this.parentIdOfTheTotalOfViews();
		String parentName = VisEntityVirtualResumeViewsCounter.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		return JnGroupers.getGroupKey(parentName, childName, parentId);
	}

	/** The total of views of the resume, read by id from its group; 0 when the group does not exist. */
	private int totalOfViews() {
		String parentId = this.parentIdOfTheTotalOfViews();
		String parentName = VisEntityVirtualResumeViewsCounter.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation counters = JnGroupers.getCounters(parentName, childName, parentId);
		boolean noViews = false == counters.containsAllFields(VisResumeViewsCounterKeys.Keys.views);
		if (noViews) {
			return 0;
		}
		return counters.getAsIntegerNumber(VisResumeViewsCounterKeys.Keys.views);
	}

	private void saveResume() {
		CcpJsonRepresentation skill = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[]}");
		CcpJsonRepresentation english = new CcpJsonRepresentation("{\"name\":\"Inglês\",\"level\":2,\"id\":0}");
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.candidate)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "Desenvolvedor Java")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 6)
				.put(VisEntityResume.Fields.linkedinAddress, this.linkedin)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.clt, 8_000)
				.put(VisEntityResume.Fields.pj, 12_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(skill))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(english));
		VisEntityResume.ENTITY.save(resume);
	}

	private String recruiter(String name, String domain) {
		return name + this.run + "@" + domain;
	}

	private CcpJsonRepresentation view(String recruiter, String linkedin, String alias) {
		CcpJsonRepresentation view = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, linkedin)
				.put(VisEntityViewedResume.Fields.email, recruiter)
				.put(VisEntityViewedResume.Fields.alias, alias);
		synchronized (this.createdViews) {
			this.createdViews.add(view);
		}
		return view;
	}

	private CcpJsonRepresentation saveView(String recruiter) {
		CcpJsonRepresentation view = this.view(recruiter, this.linkedin, "Dev Java visto");
		VisEntityViewedResume.ENTITY.save(view);
		return view;
	}

	private static String sha1(String text) {
		return new CcpStringDecorator(text).hash().asString(CcpHashAlgorithm.SHA1);
	}

	private CcpJsonRepresentation groupKeyOfTheMonth() {
		return this.groupKeyOf(this.currentMonth);
	}

	private CcpJsonRepresentation parentJsonOf(String month) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeViewsOfTheMonth.Fields.email, sha1(this.candidate))
				.put(VisEntityResumeViewsOfTheMonth.Fields.monthYear, month);
	}

	private CcpJsonRepresentation groupKeyOf(String month) {
		CcpJsonRepresentation parentJson = this.parentJsonOf(month);
		String parentId = VisEntityResumeViewsOfTheMonth.ENTITY.calculateId(parentJson);
		String parentName = VisEntityResumeViewsOfTheMonth.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		return JnGroupers.getGroupKey(parentName, childName, parentId);
	}

	/** Counts a view of the domain in the group of the candidate in the month, as the decorator does. */
	private void countAViewIn(String month, String domain) {
		CcpJsonRepresentation parentJson = this.parentJsonOf(month);
		String parentId = VisEntityResumeViewsOfTheMonth.ENTITY.calculateId(parentJson);
		Map<String, CcpJsonRepresentation> parents = new LinkedHashMap<>();
		parents.put(parentId, parentJson);
		String parentName = VisEntityResumeViewsOfTheMonth.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		JnGroupers.incrementCounter(JnGroupers.getGrouper(false), parentName, childName, domain, parents);
	}

	private CcpJsonRepresentation viewersOf(String month) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessResumeViewersOfTheMonth.Fields.email, this.candidate)
				.put(VisBusinessResumeViewersOfTheMonth.Fields.month, month);
		CcpJsonRepresentation viewers = VisBusinessResumeViewersOfTheMonth.INSTANCE.execute(input);
		return viewers;
	}

	private List<CcpJsonRepresentation> viewersOfTheCurrentMonth() {
		CcpJsonRepresentation viewers = this.viewersOf(this.currentMonth);
		List<CcpJsonRepresentation> viewersList = viewers.getAsJsonList(VisBusinessResumeViewersOfTheMonth.ResponseFields.viewers);
		return viewersList;
	}

	private static void assertViewer(CcpJsonRepresentation viewer, String domain, int views) {
		assertEquals(domain, viewer.getAsString(VisJsonCommonsFields.domain));
		assertEquals(Integer.valueOf(views), viewer.getAsIntegerNumber(VisBusinessResumeViewersOfTheMonth.ResponseFields.views));
	}

	@Test
	public void theFirstViewCountsTheDomainOfTheRecruiter() {
		this.saveResume();
		this.saveView(this.recruiter("rh", "Empresa.com.br"));

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();

		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "empresa.com.br", 1);
	}

	@Test
	public void theLaterViewsOfTheSameRecruiterInTheMonthDoNotCount() {
		this.saveResume();
		String recruiter = this.recruiter("rh", "empresa.com.br");
		this.saveView(recruiter);
		this.saveView(recruiter);
		CcpJsonRepresentation renamed = this.view(recruiter, this.linkedin, "Outro apelido do Dev");
		VisEntityViewedResume.ENTITY.save(renamed);

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();

		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "empresa.com.br", 1);
	}

	@Test
	public void theFirstViewOfAnotherMonthCountsAgain() {
		this.saveResume();
		CcpJsonRepresentation view = this.saveView(this.recruiter("rh", "empresa.com.br"));
		// the stored view goes back to january of 2000, as if its last save had been then
		CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(view);
		CcpJsonRepresentation storedInAnotherMonth = stored.put(VisEntityViewedResume.Fields.date, "15/01/2000 10:00:00");
		CcpEntityMetaData viewDetails = VisEntityViewedResume.ENTITY.getEntityMetaData();
		CcpJsonRepresentation viewWithHashedKey = VisEntityViewedResume.ENTITY.getJsonWithHandledPrimaryKey(view);
		CcpJsonRepresentation viewKey = viewDetails.getOnlyExistingFields(viewWithHashedKey);
		String viewId = VisEntityViewedResume.ENTITY.calculateId(viewKey);
		CcpDependencyInjection.getDependency(CcpCrud.class).save(viewDetails.entityName, storedInAnotherMonth, viewId);

		this.saveView(this.recruiter("rh", "empresa.com.br"));

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();
		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "empresa.com.br", 2);
	}

	@Test
	public void theDomainsComeFromTheMostViewedToTheLeast() {
		this.saveResume();
		this.saveView(this.recruiter("alfa", "consultoria.com"));
		this.saveView(this.recruiter("rh", "empresa.com.br"));
		this.saveView(this.recruiter("tech", "empresa.com.br"));

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();

		assertEquals(2, viewers.size());
		assertViewer(viewers.get(0), "empresa.com.br", 2);
		assertViewer(viewers.get(1), "consultoria.com", 1);
	}

	@Test
	public void theNonProfessionalDomainsCountAsASingleFreelancer() {
		this.saveResume();
		this.saveView(this.recruiter("autonomo", "gmail.com"));
		this.saveView(this.recruiter("consultor", "Hotmail.com"));
		this.saveView(this.recruiter("avulso", "yahoo.com.br"));
		this.saveView(this.recruiter("rh", "empresa.com.br"));

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();

		assertEquals(2, viewers.size());
		assertViewer(viewers.get(0), "freelancer", 3);
		assertViewer(viewers.get(1), "empresa.com.br", 1);
	}

	@Test
	public void theLaterViewsOfTheSameFreelancerInTheMonthDoNotCount() {
		this.saveResume();
		String recruiter = this.recruiter("autonomo", "gmail.com");
		this.saveView(recruiter);
		this.saveView(recruiter);

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();

		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "freelancer", 1);
	}

	@Test
	public void recruitersOfTheSameDomainViewingAtTheSameMomentAllCount() throws Exception {
		this.saveResume();
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(SIMULTANEOUS_RECRUITERS);
		List<Future<Boolean>> saves = new ArrayList<>();
		for (int index = 0; index < SIMULTANEOUS_RECRUITERS; index++) {
			String recruiter = this.recruiter("rh" + index + "x", "mesmaempresa.com");
			CcpJsonRepresentation view = this.view(recruiter, this.linkedin, "Dev Java ao mesmo tempo");
			Callable<Boolean> save = new SaveAfterTheStart(start, VisEntityViewedResume.ENTITY, view);
			Future<Boolean> saving = executor.submit(save);
			saves.add(saving);
		}
		start.countDown();
		for (Future<Boolean> saving : saves) {
			assertTrue("each recruiter views the resume for the first time", saving.get());
		}
		executor.shutdown();

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();

		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "mesmaempresa.com", SIMULTANEOUS_RECRUITERS);
	}

	@Test
	public void theGroupOfTheMonthKeepsASingleJsonOfCountersByDomain() {
		this.saveResume();
		this.saveView(this.recruiter("alfa", "consultoria.com"));
		this.saveView(this.recruiter("rh", "empresa.com.br"));
		this.saveView(this.recruiter("tech", "empresa.com.br"));

		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(this.groupKeyOfTheMonth());

		// the only child is a JSON text, and childCount is the number of its keys
		List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
		assertEquals(1, children.size());
		assertEquals("{\"consultoria.com\":1,\"empresa.com.br\":2}", children.get(0));
		CcpJsonRepresentation counters = new CcpJsonRepresentation(children.get(0));
		assertEquals(2, counters.fieldSet().size());
		assertEquals(Integer.valueOf(2), counters.getAsIntegerNumber(new CcpFieldName("empresa.com.br")));
		assertEquals(Integer.valueOf(1), counters.getAsIntegerNumber(new CcpFieldName("consultoria.com")));
		assertEquals(Integer.valueOf(2), group.getAsIntegerNumber(JnEntityGrouper.Fields.childCount));
		CcpJsonRepresentation parentJson = group.getInnerJson(JnEntityGrouper.Fields.parentJson);
		assertEquals(sha1(this.candidate), parentJson.getAsString(VisEntityResumeViewsOfTheMonth.Fields.email));
		assertEquals(this.currentMonth, parentJson.getAsString(VisEntityResumeViewsOfTheMonth.Fields.monthYear));
		assertEquals(2, parentJson.fieldSet().size());
		assertFalse(group.toString().contains(this.candidate));
	}

	@Test
	public void theStoredViewKeepsNoParentIds() {
		this.saveResume();
		CcpJsonRepresentation view = this.saveView(this.recruiter("rh", "empresa.com.br"));

		CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(view);

		assertFalse(stored.fieldSet().contains("monthGroup"));
	}

	@Test
	public void deletingTheViewKeepsTheCounters() {
		this.saveResume();
		CcpJsonRepresentation view = this.saveView(this.recruiter("rh", "empresa.com.br"));

		VisEntityViewedResume.ENTITY.deleteAnyWhere(view);

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();
		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "empresa.com.br", 1);
	}

	@Test
	public void viewingAgainAViewThatWentToTheTwinDoesNotCountAgainInTheMonth() {
		this.saveResume();
		String recruiter = this.recruiter("rh", "consultoria.com");
		CcpJsonRepresentation view = this.saveView(recruiter);
		// the delete moves the view to vis_inactive_viewed_resume
		VisEntityViewedResume.ENTITY.delete(view);
		assertFalse(VisEntityViewedResume.ENTITY.exists(VisEntityViewedResume.ENTITY.getJsonWithHandledPrimaryKey(view)));

		this.saveView(recruiter);

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();
		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "consultoria.com", 1);
	}

	@Test
	public void aViewThatWentToTheTwinInAnotherMonthCountsAgain() {
		this.saveResume();
		String recruiter = this.recruiter("rh", "consultoria.com");
		CcpJsonRepresentation view = this.saveView(recruiter);
		VisEntityViewedResume.ENTITY.delete(view);
		// the view in the twin goes back to january of 2000, as if it had been deleted then
		CcpEntityMetaData twinDetails = VisEntityViewedResume.ENTITY.getTwinEntity().getEntityMetaData();
		CcpJsonRepresentation viewWithHashedKey = VisEntityViewedResume.ENTITY.getJsonWithHandledPrimaryKey(view);
		CcpJsonRepresentation viewKey = twinDetails.getOnlyExistingFields(viewWithHashedKey);
		String viewId = VisEntityViewedResume.ENTITY.calculateId(viewKey);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpJsonRepresentation storedInTheTwin = crud.getOneById(twinDetails.entityName, viewId);
		CcpJsonRepresentation storedInAnotherMonth = storedInTheTwin.put(VisEntityViewedResume.Fields.date, "15/01/2000 10:00:00");
		crud.save(twinDetails.entityName, storedInAnotherMonth, viewId);

		this.saveView(recruiter);

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();
		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "consultoria.com", 2);
	}

	@Test
	public void deletingTheResumeEverywhereDeletesTheGroupsOfEveryMonth() {
		this.saveResume();
		this.saveView(this.recruiter("rh", "empresa.com.br"));
		this.countAViewIn(FIRST_MONTH, "consultoria.com");
		assertTrue(JnEntityGrouper.ENTITY.exists(this.groupKeyOfTheMonth()));
		assertTrue(JnEntityGrouper.ENTITY.exists(this.groupKeyOf(FIRST_MONTH)));

		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.candidate);
		VisEntityResume.ENTITY.deleteAnyWhere(resumeKey);

		assertFalse(JnEntityGrouper.ENTITY.exists(this.groupKeyOfTheMonth()));
		assertFalse(JnEntityGrouper.ENTITY.exists(this.groupKeyOf(FIRST_MONTH)));
		assertTrue(this.viewersOfTheCurrentMonth().isEmpty());
		assertTrue(this.viewersOf(FIRST_MONTH).getAsJsonList(VisBusinessResumeViewersOfTheMonth.ResponseFields.viewers).isEmpty());
	}

	@Test
	public void deactivatingTheResumeKeepsTheCounters() {
		this.saveResume();
		this.saveView(this.recruiter("rh", "empresa.com.br"));

		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.candidate);
		VisEntityResume.ENTITY.delete(resumeKey);

		List<CcpJsonRepresentation> viewers = this.viewersOfTheCurrentMonth();
		assertEquals(1, viewers.size());
		assertViewer(viewers.get(0), "empresa.com.br", 1);
	}

	@Test
	public void eachRecruiterCountsOnceInTheTotalOfViewsOfTheResume() {
		this.saveResume();
		String recruiter = this.recruiter("rh", "empresa.com.br");
		this.saveView(recruiter);
		this.saveView(recruiter);
		this.saveView(this.recruiter("ti", "empresa.com.br"));
		this.saveView(this.recruiter("rh", "consultoria.com"));

		assertEquals(3, this.totalOfViews());
	}

	@Test
	public void theIdOfTheGroupOfTheTotalOfViewsIsTheIdOfTheResume() {
		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.candidate);
		CcpJsonRepresentation resumeWithHashedEmail = VisEntityResume.ENTITY.getJsonWithHandledPrimaryKey(resumeKey);
		CcpJsonRepresentation resumePrimaryKey = VisEntityResume.ENTITY.getEntityMetaData().getOnlyExistingFields(resumeWithHashedEmail);
		String resumeId = VisEntityResume.ENTITY.calculateId(resumePrimaryKey);

		assertEquals(resumeId, this.parentIdOfTheTotalOfViews());
	}

	@Test
	public void theTotalOfViewsIsAHistory() {
		this.saveResume();
		String recruiter = this.recruiter("rh", "consultoria.com");
		CcpJsonRepresentation view = this.saveView(recruiter);
		VisEntityViewedResume.ENTITY.delete(view);
		this.saveView(recruiter);
		CcpJsonRepresentation otherView = this.saveView(this.recruiter("ti", "empresa.com.br"));
		VisEntityViewedResume.ENTITY.deleteAnyWhere(otherView);
		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.candidate);
		VisEntityResume.ENTITY.delete(resumeKey);

		assertEquals(2, this.totalOfViews());
	}

	@Test
	public void deletingTheResumeEverywhereDeletesTheTotalOfViews() {
		this.saveResume();
		this.saveView(this.recruiter("rh", "empresa.com.br"));
		assertTrue(JnEntityGrouper.ENTITY.exists(this.groupKeyOfTheTotalOfViews()));

		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.candidate);
		VisEntityResume.ENTITY.deleteAnyWhere(resumeKey);

		assertFalse(JnEntityGrouper.ENTITY.exists(this.groupKeyOfTheTotalOfViews()));
		assertEquals(0, this.totalOfViews());
	}

	@Test
	public void theMonthsOfTheCountingGoFromTheFirstToTheCurrentOne() {
		List<String> months = VisBusinessDeleteTheViewersOfTheDeletedResume.getMonths();

		assertEquals(FIRST_MONTH, months.get(0));
		assertEquals(this.currentMonth, months.get(months.size() - 1));
		assertEquals("202611", months.get(1));
		for (String month : months) {
			int monthOfTheYear = Integer.parseInt(month.substring(4));
			assertTrue(month, monthOfTheYear >= 1 && monthOfTheYear <= 12);
		}
		boolean crossesTheYear = months.size() > 3;
		if (crossesTheYear) {
			assertEquals("202612", months.get(2));
			assertEquals("202701", months.get(3));
		}
	}

	@Test
	public void aLinkedinOfNoResumeCountsForNobody() {
		this.saveResume();
		CcpJsonRepresentation view = this.view(this.recruiter("rh", "empresa.com.br"), this.linkedin + "outro", "Perfil sem curriculo");
		VisEntityViewedResume.ENTITY.save(view);

		assertTrue(this.viewersOfTheCurrentMonth().isEmpty());
	}

	@Test
	public void aMonthWithNoViewIsEmpty() {
		this.saveResume();
		this.saveView(this.recruiter("rh", "empresa.com.br"));

		CcpJsonRepresentation viewers = this.viewersOf("200001");

		assertEquals("200001", viewers.getAsString(VisBusinessResumeViewersOfTheMonth.Fields.month));
		assertTrue(viewers.getAsJsonList(VisBusinessResumeViewersOfTheMonth.ResponseFields.viewers).isEmpty());
	}

	@Test
	public void theServiceAnswersTheViewers() {
		this.saveResume();
		this.saveView(this.recruiter("rh", "empresa.com.br"));
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessResumeViewersOfTheMonth.Fields.email, this.candidate)
				.put(VisBusinessResumeViewersOfTheMonth.Fields.month, this.currentMonth);

		CcpJsonRepresentation viewers = VisServiceResume.GetViewersOfTheMonth.execute(input);

		assertEquals(1, viewers.getAsJsonList(VisBusinessResumeViewersOfTheMonth.ResponseFields.viewers).size());
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aMonthOutOfTheFormatIsRefused() {
		this.viewersOf("2026-10");
	}
}
