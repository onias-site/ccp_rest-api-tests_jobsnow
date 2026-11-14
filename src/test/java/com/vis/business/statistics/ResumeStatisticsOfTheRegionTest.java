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
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisBusinessResumeStatisticsOfTheRegion.Fields.ddd, DDD);
		return VisBusinessResumeStatisticsOfTheRegion.INSTANCE.execute(input);
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

	@After
	public void deleteTheResumes() {
		VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.firstEmail));
		VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.secondEmail));
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
		assertAdded("clt 8.500 in the range of 8.000", 1, before, after, VisResumeStatisticsCharts.clt, "8000");
		assertAdded("clt 12.000 in the range of 12.000", 1, before, after, VisResumeStatisticsCharts.clt, "12000");
		assertAdded("pj 9.000", 1, before, after, VisResumeStatisticsCharts.pj, "9000");
		assertAdded("pj not informed", 1, before, after, VisResumeStatisticsCharts.pj, "notInformed");
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
