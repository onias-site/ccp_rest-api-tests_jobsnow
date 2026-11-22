package com.vis.business.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentEmailSender;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
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
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityGrouper;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntityResume;
import com.vis.services.VisServicePosition;

/**
 * Proves the endpoint of the filters of the position form: the resumes of the area in the regions asked (every region
 * with no ddd), filtered inside Elasticsearch by a Painless script; the answer is the set of ids, its size and the
 * averages of the claims. Each filter informed leaves out the resume that does not satisfy it, a filter left out (or 0,
 * or unchecked) does not filter, a required skill is satisfied by a skill below it in the hierarchy unless a narrower
 * required skill is asked too, and an invalid input is refused.
 */
public class ResumesOfTheFiltersTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> SilentEmailSender.INSTANCE;

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
		JnExecuteBulkOperation.INSTANCE.executeBulk(VisEntityResume.INSTANCE.getFirstRecordsToInsert(), JnDeleteKeysFromCache.INSTANCE);
	}

	private static final String ENGLISH = "Inglês";

	private final long now = System.currentTimeMillis();

	private final List<CcpJsonRepresentation> savedResumes = new ArrayList<>();

	/**
	 * A resume of information technology in ddds 11 and 21: 10 days to start, up to 6 months of temporary job, 4 years of
	 * experience (pleno), CLT 99,500 and no PJ, English level 1 and Spanish level 2, the skill PHP (child of BACKEND), no flag checked and
	 * the company empresa-proibida.com.br forbidden.
	 */
	private CcpJsonRepresentation resume(String name) {
		CcpJsonRepresentation skill = new CcpJsonRepresentation("{\"skill\":\"PHP\",\"word\":\"PHP\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		CcpJsonRepresentation english = language(ENGLISH, 1);
		CcpJsonRepresentation spanish = language("Espanhol", 2);
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, name + this.now + "@jobsnow.com")
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11, 21))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 10)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/" + name + "-" + this.now)
				.put(VisEntityResume.Fields.clt, 99500)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 6)
				.put(VisEntityResume.Fields.negotiableClaim, false)
				.put(VisEntityResume.Fields.pcd, false)
				.put(VisEntityResume.Fields.travel, false)
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(english, spanish))
				.put(VisEntityResume.Fields.notAllowedCompany, Arrays.asList("empresa-proibida.com.br"))
				.put(VisEntityResume.Fields.skill, Arrays.asList(skill));
	}

	/** Saves the resume and answers its id, calculated from the stored record (the e-mail is transformed on the way in). */
	private String save(CcpJsonRepresentation resume) {
		VisEntityResume.ENTITY.save(resume);
		this.savedResumes.add(resume);
		// the search of the groups is near real time: the group saved now is seen after the refresh of the index
		CcpEntityMetaData grouperDetails = JnEntityGrouper.ENTITY.getEntityMetaData();
		CcpDbRequester requester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		requester.refresh(grouperDetails.entityName);
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(resume);
		String resumeId = VisEntityResume.ENTITY.calculateId(stored);
		return resumeId;
	}

	@After
	public void deleteTheResumes() {
		for (CcpJsonRepresentation resume : this.savedResumes) {
			VisEntityResume.ENTITY.deleteAnyWhere(resume);
		}
	}

	/** The filters of an information technology position in ddd 11. */
	private static CcpJsonRepresentation filters() {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessResumesOfTheFilters.Fields.resumeType, 1)
				.put(VisBusinessResumesOfTheFilters.Fields.ddd, Arrays.asList(11));
	}

	private static CcpJsonRepresentation ask(CcpJsonRepresentation filters) {
		// como numa requisição HTTP: os números chegam do texto JSON
		CcpJsonRepresentation parsed = new CcpJsonRepresentation(filters.asUgglyJson());
		CcpJsonRepresentation response = VisServicePosition.GetResumesOfTheFilters.execute(parsed);
		return response;
	}

	private static boolean finds(CcpJsonRepresentation filters, String resumeId) {
		CcpJsonRepresentation response = ask(filters);
		List<String> ids = response.getAsStringList(VisBusinessResumesOfTheFilters.Fields.ids);
		return ids.contains(resumeId);
	}

	private static CcpJsonRepresentation range(int min, int max) {
		return new CcpJsonRepresentation("{\"min\":" + min + ",\"max\":" + max + "}");
	}

	private static CcpJsonRepresentation language(String name, int level) {
		return new CcpJsonRepresentation("{\"name\":\"" + name + "\",\"level\":" + level + "}");
	}

	@Test
	public void theAreaAndTheRegionsChooseTheGroups() {
		String resumeId = this.save(this.resume("filtersArea"));

		assertTrue("the resume of its area and ddd", finds(filters(), resumeId));
		assertFalse("not in another area", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.resumeType, 2), resumeId));
		assertFalse("not in a ddd it does not list", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.ddd, Arrays.asList(31)), resumeId));
		CcpJsonRepresentation homeOffice = CcpOtherConstants.EMPTY_JSON.put(VisBusinessResumesOfTheFilters.Fields.resumeType, 1);
		assertTrue("with no ddd, every ddd (home office position)", finds(homeOffice, resumeId));

		CcpJsonRepresentation bothDdds = ask(filters().put(VisBusinessResumesOfTheFilters.Fields.ddd, Arrays.asList(11, 21)));
		List<String> ids = bothDdds.getAsStringList(VisBusinessResumesOfTheFilters.Fields.ids);
		assertEquals("a resume of two ddds counts once", 1, ids.stream().filter(resumeId::equals).count());
		assertEquals("the count is the size of the set of ids", new HashSet<>(ids).size(), bothDdds.getAsIntegerNumber(VisBusinessResumesOfTheFilters.Fields.resumes).intValue());
	}

	@Test
	public void eachFilterLeavesOutTheResumeThatDoesNotSatisfyIt() {
		String resumeId = this.save(this.resume("filtersEach"));

		assertFalse("starts in 10 days, the position asks 5", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.disponibility, 5), resumeId));
		assertTrue("the position gives 15 days", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.disponibility, 15), resumeId));
		assertTrue("immediate hiring (0) does not filter", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.disponibility, 0), resumeId));

		assertFalse("accepts 6 months of temporary job, the position has 3", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.temporallyJobTime, 3), resumeId));
		assertTrue("the position has 12 months", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.temporallyJobTime, 12), resumeId));

		assertTrue("4 years of experience is pleno", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.seniority, "pleno"), resumeId));
		assertFalse("and not senior", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.seniority, "senior"), resumeId));

		assertFalse("does not negotiate the claim", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.negotiableClaim, true), resumeId));
		assertFalse("is not pcd", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.pcd, true), resumeId));
		assertFalse("does not travel", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.travel, true), resumeId));
		assertTrue("an unchecked flag does not filter", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.travel, false), resumeId));

		assertTrue("CLT 99,500 inside the range", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.clt, range(99000, 100000)), resumeId));
		assertFalse("outside the range", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.clt, range(5000, 10000)), resumeId));
		assertFalse("a resume with no PJ claim is out of a PJ range", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.pj, range(5000, 100000)), resumeId));

		assertFalse("the candidate forbids the company of the recruiter", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.recruiterDomain, "Empresa-Proibida.com.br"), resumeId));
		assertTrue("another company", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.recruiterDomain, "outra-empresa.com.br"), resumeId));

		assertTrue("English level 1", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.spokenLanguage, Arrays.asList(language(ENGLISH, 1))), resumeId));
		assertFalse("not level 2", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.spokenLanguage, Arrays.asList(language(ENGLISH, 2))), resumeId));
		assertTrue("Spanish level 2, the second language of the list", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.spokenLanguage, Arrays.asList(language("Espanhol", 2))), resumeId));
		assertTrue("both languages", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.spokenLanguage, Arrays.asList(language(ENGLISH, 1), language("Espanhol", 1))), resumeId));
		assertFalse("no French", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.spokenLanguage, Arrays.asList(language("Francês", 1))), resumeId));
	}

	@Test
	public void aRequiredSkillIsFoundBelowItInTheHierarchy() {
		String resumeId = this.save(this.resume("filtersSkills"));

		assertTrue("the skill itself", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.skill, Arrays.asList("PHP")), resumeId));
		assertTrue("php is below backend: a backend position takes it", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.skill, Arrays.asList("backend")), resumeId));
		assertFalse("a skill the resume does not have", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.skill, Arrays.asList("SQL")), resumeId));
		assertFalse("every required skill is needed", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.skill, Arrays.asList("PHP", "SQL")), resumeId));
		assertTrue("backend gives way to php, which the position asks too", finds(filters().put(VisBusinessResumesOfTheFilters.Fields.skill, Arrays.asList("BACKEND", "PHP")), resumeId));
	}

	@Test
	public void theAveragesAreOfTheResumesFound() {
		String firstId = this.save(this.resume("filtersAverageA"));
		CcpJsonRepresentation second = this.resume("filtersAverageB")
				.put(VisEntityResume.Fields.clt, 99900)
				.put(VisEntityResume.Fields.pj, 99000);
		String secondId = this.save(second);

		CcpJsonRepresentation onlyTheseTwo = filters().put(VisBusinessResumesOfTheFilters.Fields.clt, range(99400, 100000));
		CcpJsonRepresentation response = ask(onlyTheseTwo);
		List<String> ids = response.getAsStringList(VisBusinessResumesOfTheFilters.Fields.ids);

		assertEquals("only the two resumes of this test have such claims", new HashSet<>(Arrays.asList(firstId, secondId)), new HashSet<>(ids));
		assertEquals(2, response.getAsIntegerNumber(VisBusinessResumesOfTheFilters.Fields.resumes).intValue());
		assertEquals("the average CLT of both", 99700.0, response.getAsDoubleNumber(VisBusinessResumesOfTheFilters.Fields.cltAverage), 0.001);
		assertEquals("the average PJ of the one that informed it", 99000.0, response.getAsDoubleNumber(VisBusinessResumesOfTheFilters.Fields.pjAverage), 0.001);

		CcpJsonRepresentation nobody = ask(filters().put(VisBusinessResumesOfTheFilters.Fields.clt, range(1000, 1001)));
		assertEquals("no resume found", 0, nobody.getAsIntegerNumber(VisBusinessResumesOfTheFilters.Fields.resumes).intValue());
		assertFalse("no average without resumes", nobody.containsAllFields(VisBusinessResumesOfTheFilters.Fields.cltAverage));
	}

	@Test
	public void anInvalidInputIsRefused() {
		try {
			ask(CcpOtherConstants.EMPTY_JSON.put(VisBusinessResumesOfTheFilters.Fields.ddd, Arrays.asList(11)));
			fail("the area is required");
		} catch (CcpJsonValidationError expected) {
		}
		try {
			ask(filters().put(VisBusinessResumesOfTheFilters.Fields.resumeType, 5));
			fail("area 5 does not exist");
		} catch (CcpJsonValidationError expected) {
		}
		try {
			ask(filters().put(VisBusinessResumesOfTheFilters.Fields.ddd, Arrays.asList(11, 11)));
			fail("a repeated ddd");
		} catch (CcpJsonValidationError expected) {
		}
		try {
			ask(filters().put(VisBusinessResumesOfTheFilters.Fields.seniority, "guru"));
			fail("an unknown seniority");
		} catch (CcpJsonValidationError expected) {
		}
		try {
			ask(filters().put(VisBusinessResumesOfTheFilters.Fields.clt, range(9000, 5000)));
			fail("min above max");
		} catch (CcpJsonValidationError expected) {
		}
	}
}
