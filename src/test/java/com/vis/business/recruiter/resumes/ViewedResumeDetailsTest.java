package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.flow.CcpErrorFlowDisturb;
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
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityGrouper;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityViewedResume;
import com.vis.services.VisServiceRecruiterResumes;

/**
 * Proves the details of a viewed resume ({@code VisBusinessViewedResumeDetails}): from the LinkedIn of a view of the
 * month, the resume comes as a row of the e-mail of resumes, without the date lines (they need the history); a LinkedIn
 * the recruiter did not view in the month is refused, and a resume no longer active has no row.
 */
public class ViewedResumeDetailsTest {

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

	private final long run = System.currentTimeMillis();

	private final String candidate = "candidatodetalhe" + this.run + "@jobsnow.com";

	private final String linkedin = "https://www.linkedin.com/in/candidatodetalhe" + this.run;

	private final String recruiter = "recrutadordetalhe" + this.run + "@empresa.com.br";

	private final String currentMonth = CcpEntityExpurgableOptions.monthly.getFormattedDate();

	private final List<CcpJsonRepresentation> createdViews = new ArrayList<>();

	@After
	public void deleteWhatTheTestCreated() {
		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.candidate);
		VisEntityResume.ENTITY.deleteAnyWhere(resumeKey);
		for (CcpJsonRepresentation view : this.createdViews) {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(view);
		}
		CcpJsonRepresentation monthKey = this.input(this.currentMonth, this.linkedin);
		CcpJsonRepresentation groupKey = VisBusinessViewedResumesOfTheMonth.getGroupKeyOfTheMonth(monthKey);
		JnEntityGrouper.ENTITY.deleteAnyWhere(groupKey);
	}

	private void saveResume() {
		this.saveResume(List.of());
	}

	private void saveResume(List<String> notAllowedCompanies) {
		CcpJsonRepresentation skill = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[]}");
		CcpJsonRepresentation english = new CcpJsonRepresentation("{\"name\":\"Inglês\",\"level\":2,\"id\":0}");
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.candidate)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(21, 11))
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
		boolean forbidsCompanies = false == notAllowedCompanies.isEmpty();
		CcpJsonRepresentation resumeToSave = forbidsCompanies ? resume.put(VisEntityResume.Fields.notAllowedCompany, notAllowedCompanies) : resume;
		VisEntityResume.ENTITY.save(resumeToSave);
	}

	private void saveView() {
		CcpJsonRepresentation view = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedin)
				.put(VisEntityViewedResume.Fields.email, this.recruiter)
				.put(VisEntityViewedResume.Fields.alias, "Dev Java do detalhe");
		VisEntityViewedResume.ENTITY.save(view);
		this.createdViews.add(view);
	}

	private CcpJsonRepresentation input(String month, String linkedin) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessViewedResumeDetails.Fields.email, this.recruiter)
				.put(VisBusinessViewedResumeDetails.Fields.month, month)
				.put(VisBusinessViewedResumeDetails.Fields.linkedinHash, VisBusinessSendResumesToRecruiters.getLinkedinHash(linkedin));
	}

	private CcpJsonRepresentation details(String month) {
		return VisBusinessViewedResumeDetails.INSTANCE.execute(this.input(month, this.linkedin));
	}

	@Test
	public void theDetailsAreTheRowOfTheEmailWithoutTheDates() {
		this.saveResume();
		this.saveView();

		CcpJsonRepresentation details = this.details(this.currentMonth);

		assertEquals(this.linkedin, details.getAsString(VisEntityViewedResume.Fields.linkedin));
		assertEquals(VisBusinessSendResumesToRecruiters.getLinkedinHash(this.linkedin), details.getAsString(VisBusinessViewedResumeDetails.Fields.linkedinHash));
		List<CcpJsonRepresentation> rows = details.getAsJsonList(VisRecruiterResumesFields.resumes);
		assertEquals(1, rows.size());
		CcpJsonRepresentation row = rows.get(0);
		assertEquals(4, row.getAsStringList(VisRecruiterResumesFields.whatMatters).size());
		assertFalse(row.getAsStringList(VisRecruiterResumesFields.regions).isEmpty());
		assertTrue(row.getAsStringList(VisRecruiterResumesFields.languagesAndSkills).toString().contains("JAVA"));
		assertTrue(row.getAsStringList(VisRecruiterResumesFields.job).toString().contains("Desenvolvedor Java"));
		assertFalse(row.getAsStringList(VisRecruiterResumesFields.otherInformation).isEmpty());
		assertFalse("no history read online", row.containsAllFields(VisRecruiterResumesFields.insertion));
		assertFalse("no history read online", row.containsAllFields(VisRecruiterResumesFields.lastChange));
		assertFalse(row.containsAllFields(VisRecruiterResumesFields.sortKeys));
	}

	/** The candidate forbade the company of the recruiter after the view: the details no longer show the resume. */
	@Test
	public void aResumeThatNowForbidsTheCompanyOfTheRecruiterHasNoRow() {
		this.saveResume();
		this.saveView();
		this.saveResume(List.of("empresa.com.br"));

		CcpJsonRepresentation details = this.details(this.currentMonth);

		assertTrue(details.getAsJsonList(VisRecruiterResumesFields.resumes).isEmpty());
		assertFalse("the LinkedIn is not answered", details.containsAllFields(VisEntityViewedResume.Fields.linkedin));
		assertFalse(details.toString().contains(this.linkedin));
	}

	@Test
	public void theServiceAnswersTheDetails() {
		this.saveResume();
		this.saveView();

		CcpJsonRepresentation details = VisServiceRecruiterResumes.GetViewedResumeDetails.execute(this.input(this.currentMonth, this.linkedin));

		assertEquals(1, details.getAsJsonList(VisRecruiterResumesFields.resumes).size());
	}

	@Test
	public void aLinkedinNotViewedInTheMonthIsRefused() {
		this.saveResume();
		try {
			this.details(this.currentMonth);
		} catch (CcpErrorFlowDisturb notViewed) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, notViewed.status);
			return;
		}
		throw new AssertionError("the details of a resume the recruiter did not view were answered");
	}

	@Test
	public void theViewOfAnotherMonthDoesNotOpenTheDetails() {
		this.saveResume();
		this.saveView();
		try {
			this.details("200001");
		} catch (CcpErrorFlowDisturb notViewed) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, notViewed.status);
			return;
		}
		throw new AssertionError("a view of another month opened the details");
	}

	@Test
	public void aResumeNoLongerActiveHasNoRow() {
		this.saveResume();
		this.saveView();
		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.candidate);
		VisEntityResume.ENTITY.delete(resumeKey);

		CcpJsonRepresentation details = this.details(this.currentMonth);

		assertTrue(details.getAsJsonList(VisRecruiterResumesFields.resumes).isEmpty());
		assertFalse("the LinkedIn is not answered", details.containsAllFields(VisEntityViewedResume.Fields.linkedin));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void theDetailsWithoutTheHashOfTheLinkedinAreRefused() {
		CcpJsonRepresentation withoutHash = this.input(this.currentMonth, this.linkedin).removeFields(VisBusinessViewedResumeDetails.Fields.linkedinHash);
		VisBusinessViewedResumeDetails.INSTANCE.execute(withoutHash);
	}
}
