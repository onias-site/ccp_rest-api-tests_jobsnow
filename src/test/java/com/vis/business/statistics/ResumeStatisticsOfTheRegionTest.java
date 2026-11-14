package com.vis.business.statistics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
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
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnGroupers;
import com.vis.entities.RecordingEmailSender;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityViewedResume;
import com.vis.entities.VisEntityVirtualDddGrouper;

/**
 * Proves the statistics of the resumes of a region (screen Supply X Demand): two resumes saved in a region change each
 * pie chart by their own slices, and the averages of the salary expectations are the ones of the children of the group of
 * the ddd (the region already has resumes of the database, so the slices are compared before and after).
 */
public class ResumeStatisticsOfTheRegionTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
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
		JnExecuteBulkOperation.INSTANCE.executeBulk(VisEntityResume.INSTANCE.getFirstRecordsToInsert(), JnDeleteKeysFromCache.INSTANCE);
	}

	private static final int DDD = 11;

	private final long now = System.currentTimeMillis();

	private final String firstEmail = "statistics1" + this.now + "@jobsnow.com";

	private final String secondEmail = "statistics2" + this.now + "@jobsnow.com";

	private final String uniqueSkill = "STATISTICS" + this.now;

	private CcpJsonRepresentation resume(String email, int experience, List<CcpJsonRepresentation> languages, int disponibility, int temporallyJobTime) {
		CcpJsonRepresentation unique = new CcpJsonRepresentation("{\"skill\":\"" + this.uniqueSkill + "\",\"word\":\"" + this.uniqueSkill + "\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		CcpJsonRepresentation excluded = new CcpJsonRepresentation("{\"skill\":\"COBOL\",\"word\":\"COBOL\",\"type\":\"excludedSkills\",\"category\":\"excludedSkills\"}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(DDD))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, disponibility)
				.put(VisEntityResume.Fields.experience, experience)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/statistics" + email.charAt(10))
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, temporallyJobTime)
				.put(VisEntityResume.Fields.skill, Arrays.asList(unique, excluded))
				.put(VisEntityResume.Fields.spokenLanguage, languages);
	}

	private static CcpJsonRepresentation language(String name, int level) {
		return new CcpJsonRepresentation("{\"name\":\"" + name + "\",\"level\":" + level + ",\"id\":0}");
	}

	private static CcpJsonRepresentation statistics() {
		return statistics(VisResumeStatisticsSources.resumes);
	}

	private static CcpJsonRepresentation statistics(VisResumeStatisticsSources source) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisBusinessResumeStatisticsOfTheRegion.Fields.ddd, DDD);
		CcpJsonRepresentation statistics = VisBusinessResumeStatisticsOfTheRegion.INSTANCE.execute(input);
		return statistics.getInnerJson(source);
	}

	private static long slice(CcpJsonRepresentation statistics, VisResumeStatisticsCharts chart, String key) {
		CcpJsonRepresentation charts = statistics.getInnerJson(VisBusinessResumeStatisticsOfTheRegion.Fields.charts);
		CcpJsonRepresentation slices = charts.getInnerJson(chart);
		CcpFieldName keyField = new CcpFieldName(key);
		return slices.containsAllFields(keyField) ? slices.getAsLongNumber(keyField) : 0L;
	}

	private static void assertAdded(String message, long expected, CcpJsonRepresentation before, CcpJsonRepresentation after, VisResumeStatisticsCharts chart, String key) {
		long added = slice(after, chart, key) - slice(before, chart, key);
		assertEquals(message, expected, added);
	}

	private final String recruiterEmail = "statisticsRecruiter" + this.now + "@empresa.com.br";

	private final String viewedLinkedin = "https://www.linkedin.com/in/statistics-view-" + this.now;

	/** A view of a resume of the region, with the fields of the resume handed over to the group of views of the ddd. */
	private CcpJsonRepresentation view() {
		return this.resume(this.firstEmail, 7, Arrays.asList(language("English", 2)), 0, 6)
				.removeFields(VisEntityResume.Fields.email, VisEntityResume.Fields.linkedinAddress, VisEntityResume.Fields.desiredJob)
				.put(VisEntityResume.Fields.pj, 9_000)
				// the view carries the resume as stored: the year the experience started, not the years
				.put(VisEntityResume.Fields.experience, new CcpTimeDecorator().getYear() - 7)
				.put(VisEntityViewedResume.Fields.linkedin, this.viewedLinkedin)
				.put(VisEntityViewedResume.Fields.email, this.recruiterEmail)
				.put(VisEntityViewedResume.Fields.alias, "Statistics view");
	}

	@After
	public void deleteTheResumes() {
		VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.firstEmail));
		VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.secondEmail));
		VisEntityViewedResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.viewedLinkedin)
				.put(VisEntityViewedResume.Fields.email, this.recruiterEmail));
	}

	private static long totalOf(VisResumeTotalsCounters counter, VisResumeStatisticsSources source, Object key) {
		CcpJsonRepresentation totals = VisBusinessResumeTotals.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON).getInnerJson(counter).getInnerJson(source);
		CcpFieldName keyField = new CcpFieldName(String.valueOf(key));
		return totals.containsAllFields(keyField) ? totals.getAsLongNumber(keyField) : 0L;
	}

	private static long totalOf(VisResumeStatisticsSources source, int ddd) {
		return totalOf(VisResumeTotalsCounters.byRegion, source, ddd);
	}

	/**
	 * 2026-11-14: the totals of the folders of the screen come from a counter by ddd in jn_grouper, one per entity: the
	 * insertion adds 1 to each ddd of the record, a save that changes the ddds moves the count, the delete subtracts it.
	 * The same request brings the counters by day and by month of insertion (the line chart), which also count the
	 * active records only.
	 */
	@Test
	public void theTotalsByRegionFollowTheResumesAndTheViews() {
		int otherDdd = 21;
		String today = new CcpTimeDecorator().getFormattedDateTime("yyyyMMdd");
		String thisMonth = today.substring(0, 6);
		long resumesBefore = totalOf(VisResumeStatisticsSources.resumes, DDD);
		long otherResumesBefore = totalOf(VisResumeStatisticsSources.resumes, otherDdd);
		long viewsBefore = totalOf(VisResumeStatisticsSources.viewedResumes, DDD);
		long resumesTodayBefore = totalOf(VisResumeTotalsCounters.byDay, VisResumeStatisticsSources.resumes, today);
		long viewsTodayBefore = totalOf(VisResumeTotalsCounters.byDay, VisResumeStatisticsSources.viewedResumes, today);
		long resumesThisMonthBefore = totalOf(VisResumeTotalsCounters.byMonth, VisResumeStatisticsSources.resumes, thisMonth);
		long viewsThisMonthBefore = totalOf(VisResumeTotalsCounters.byMonth, VisResumeStatisticsSources.viewedResumes, thisMonth);

		CcpJsonRepresentation resume = this.resume(this.firstEmail, 3, Arrays.asList(language("English", 2)), 0, 0).put(VisEntityResume.Fields.clt, 8_500);
		VisEntityResume.ENTITY.save(resume);
		VisEntityViewedResume.ENTITY.save(this.view());
		assertEquals("the resume in its ddd", resumesBefore + 1, totalOf(VisResumeStatisticsSources.resumes, DDD));
		assertEquals("the view in the ddd of the resume", viewsBefore + 1, totalOf(VisResumeStatisticsSources.viewedResumes, DDD));
		assertEquals("the resume inserted today", resumesTodayBefore + 1, totalOf(VisResumeTotalsCounters.byDay, VisResumeStatisticsSources.resumes, today));
		assertEquals("the view made today", viewsTodayBefore + 1, totalOf(VisResumeTotalsCounters.byDay, VisResumeStatisticsSources.viewedResumes, today));
		assertEquals("the resume in this month", resumesThisMonthBefore + 1, totalOf(VisResumeTotalsCounters.byMonth, VisResumeStatisticsSources.resumes, thisMonth));
		assertEquals("the view in this month", viewsThisMonthBefore + 1, totalOf(VisResumeTotalsCounters.byMonth, VisResumeStatisticsSources.viewedResumes, thisMonth));

		VisEntityResume.ENTITY.save(resume.put(VisEntityResume.Fields.ddd, Arrays.asList(otherDdd)));
		assertEquals("the resume left its ddd", resumesBefore, totalOf(VisResumeStatisticsSources.resumes, DDD));
		assertEquals("and went to the new one", otherResumesBefore + 1, totalOf(VisResumeStatisticsSources.resumes, otherDdd));
		assertEquals("a save again is not a new insertion", resumesTodayBefore + 1, totalOf(VisResumeTotalsCounters.byDay, VisResumeStatisticsSources.resumes, today));

		this.deleteTheResumes();
		assertEquals(otherResumesBefore, totalOf(VisResumeStatisticsSources.resumes, otherDdd));
		assertEquals(viewsBefore, totalOf(VisResumeStatisticsSources.viewedResumes, DDD));
		assertEquals(resumesTodayBefore, totalOf(VisResumeTotalsCounters.byDay, VisResumeStatisticsSources.resumes, today));
		assertEquals(viewsTodayBefore, totalOf(VisResumeTotalsCounters.byDay, VisResumeStatisticsSources.viewedResumes, today));
	}

	/**
	 * 2026-11-14: the same region read in the same request gives the statistics of the views of its resumes, from the group
	 * of views of the ddd: a view counts in the slices of the resume as it was at the view, and not among the resumes.
	 */
	@Test
	public void theViewsOfTheRegionHaveTheirOwnStatistics() {
		CcpJsonRepresentation viewsBefore = statistics(VisResumeStatisticsSources.viewedResumes);
		CcpJsonRepresentation resumesBefore = statistics();

		VisEntityViewedResume.ENTITY.save(this.view());

		CcpJsonRepresentation viewsAfter = statistics(VisResumeStatisticsSources.viewedResumes);
		CcpJsonRepresentation resumesAfter = statistics();
		long viewsTotalBefore = viewsBefore.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.total);
		long viewsTotalAfter = viewsAfter.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.total);
		assertEquals("one more view", viewsTotalBefore + 1, viewsTotalAfter);
		long resumesTotalBefore = resumesBefore.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.total);
		long resumesTotalAfter = resumesAfter.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.total);
		assertEquals("a view is not a resume", resumesTotalBefore, resumesTotalAfter);

		assertAdded("the skill of the viewed resume", 1, viewsBefore, viewsAfter, VisResumeStatisticsCharts.skill, this.uniqueSkill);
		assertAdded("fluent English", 1, viewsBefore, viewsAfter, VisResumeStatisticsCharts.spokenLanguage, "English|2");
		assertAdded("7 years", 1, viewsBefore, viewsAfter, VisResumeStatisticsCharts.seniority, "senior");
		assertAdded("6 months", 1, viewsBefore, viewsAfter, VisResumeStatisticsCharts.temporallyJob, "accepts");

		CcpJsonRepresentation averages = viewsAfter.getInnerJson(VisBusinessResumeStatisticsOfTheRegion.Fields.averages);
		CcpJsonRepresentation all = averages.getInnerJson(VisBusinessResumeStatisticsOfTheRegion.Fields.all);
		assertEquals(viewsTotalAfter, all.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.resumes).longValue());
	}

	@Test
	public void theResumesOfTheRegionChangeTheirOwnSlices() {
		CcpJsonRepresentation before = statistics();

		// 3 years (pleno), clt 8.500, pj 9.000, fluent English, immediately available, no temporary job
		VisEntityResume.ENTITY.save(this.resume(this.firstEmail, 3, Arrays.asList(language("English", 2)), 0, 0)
				.put(VisEntityResume.Fields.clt, 8_500)
				.put(VisEntityResume.Fields.pj, 9_000));
		// 7 years (senior), clt 12.000, no pj, intermediate English and fluent Spanish, in 30 days, temporary jobs of 6 months
		VisEntityResume.ENTITY.save(this.resume(this.secondEmail, 7, Arrays.asList(language("English", 1), language("Spanish", 2)), 30, 6)
				.put(VisEntityResume.Fields.clt, 12_000));

		CcpJsonRepresentation after = statistics();
		long totalBefore = before.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.total);
		long totalAfter = after.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.total);
		assertEquals("two more resumes", totalBefore + 2, totalAfter);

		assertAdded("the type of both", 2, before, after, VisResumeStatisticsCharts.resumeType, "1");
		assertAdded("fluent English", 1, before, after, VisResumeStatisticsCharts.spokenLanguage, "English|2");
		assertAdded("intermediate English is another slice", 1, before, after, VisResumeStatisticsCharts.spokenLanguage, "English|1");
		assertAdded("fluent Spanish", 1, before, after, VisResumeStatisticsCharts.spokenLanguage, "Spanish|2");
		assertAdded("the skill both declare", 2, before, after, VisResumeStatisticsCharts.skill, this.uniqueSkill);
		assertAdded("an excluded skill is not a skill of the resume", 0, before, after, VisResumeStatisticsCharts.skill, "COBOL");
		assertAdded("3 years", 1, before, after, VisResumeStatisticsCharts.seniority, "pleno");
		assertAdded("7 years", 1, before, after, VisResumeStatisticsCharts.seniority, "senior");
		assertAdded("0 days", 1, before, after, VisResumeStatisticsCharts.disponibility, "immediate");
		assertAdded("30 days", 1, before, after, VisResumeStatisticsCharts.disponibility, "notImmediate");
		assertAdded("0 months", 1, before, after, VisResumeStatisticsCharts.temporallyJob, "doesNotAccept");
		assertAdded("6 months", 1, before, after, VisResumeStatisticsCharts.temporallyJob, "accepts");
		assertAdded("one skill each (the excluded one does not count)", 2, before, after, VisResumeStatisticsCharts.skillsCount, "upTo5");
		assertAdded("clt and pj", 1, before, after, VisResumeStatisticsCharts.contractType, "both");
		assertAdded("clt only", 1, before, after, VisResumeStatisticsCharts.contractType, "onlyClt");
	}

	@Test
	public void theAveragesAreTheOnesOfTheChildrenOfTheGroup() {
		VisEntityResume.ENTITY.save(this.resume(this.firstEmail, 3, Arrays.asList(language("English", 2)), 0, 0).put(VisEntityResume.Fields.clt, 8_500));
		CcpJsonRepresentation statistics = statistics();

		// the oracle: the children of the group of the ddd, averaged here
		String parentId = VisEntityVirtualDddGrouper.ENTITY.calculateId(CcpOtherConstants.EMPTY_JSON.put(VisEntityVirtualDddGrouper.Fields.ddd, DDD));
		List<String> children = JnGroupers.getChildren(false, VisEntityVirtualDddGrouper.ENTITY.getEntityMetaData().entityName, VisEntityResume.ENTITY.getEntityMetaData().entityName, parentId);
		int currentYear = new CcpTimeDecorator().getYear();
		double cltSum = 0, plenoCltSum = 0, yearsSum = 0, plenoYearsSum = 0;
		long cltCount = 0, plenoCount = 0, plenoCltCount = 0, yearsCount = 0;
		for (String child : children) {
			CcpJsonRepresentation resume = new CcpJsonRepresentation(child);
			double clt = resume.containsAllFields(VisEntityResume.Fields.clt) ? resume.getAsDoubleNumber(VisEntityResume.Fields.clt) : 0;
			int years = Math.max(0, currentYear - resume.getAsIntegerNumber(VisEntityResume.Fields.experience));
			boolean pleno = years >= 3 && years < 5;
			plenoCount += pleno ? 1 : 0;
			yearsSum += years;
			yearsCount++;
			plenoYearsSum += pleno ? years : 0;
			if (clt > 0) {
				cltSum += clt;
				cltCount++;
				plenoCltSum += pleno ? clt : 0;
				plenoCltCount += pleno ? 1 : 0;
			}
		}

		CcpJsonRepresentation averages = statistics.getInnerJson(VisBusinessResumeStatisticsOfTheRegion.Fields.averages);
		CcpJsonRepresentation all = averages.getInnerJson(VisBusinessResumeStatisticsOfTheRegion.Fields.all);
		CcpJsonRepresentation pleno = averages.getInnerJson(new CcpFieldName("pleno"));
		assertEquals((long) children.size(), all.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.resumes).longValue());
		assertEquals(Math.round(cltSum / cltCount), all.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.clt).longValue());
		assertEquals(plenoCount, pleno.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.resumes).longValue());
		assertEquals(Math.round(plenoCltSum / plenoCltCount), pleno.getAsLongNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.clt).longValue());
		// the years of experience, with one decimal place
		assertEquals(Math.round(yearsSum * 10 / yearsCount) / 10d, all.getAsDoubleNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.experience), 0.0001);
		assertEquals(Math.round(plenoYearsSum * 10 / plenoCount) / 10d, pleno.getAsDoubleNumber(VisBusinessResumeStatisticsOfTheRegion.Fields.experience), 0.0001);
		// every seniority is there, even with no resume, and all of them together
		assertEquals(new java.util.HashSet<>(Arrays.asList("estag", "junior", "pleno", "senior", "especialista", "all")), averages.fieldSet());
	}
}
