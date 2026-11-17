package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
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
import com.jn.business.grouper.JnBusinessGetGrouperCharts;
import com.jn.utils.JnGroupers;
import com.vis.business.recruiter.VisBusinessLoadRecruitersFromHtmlFolder;
import com.vis.business.recruiter.VisBusinessPrepareRecruiter;
import com.vis.business.recruiter.resumes.VisMetropolitanRegions;
import com.vis.json.transformers.VisResumeRegionScopeCounterKeys;

/**
 * 2026-11-17: two charts of the screen Curiosities, both counters of {@code jn_grouper} of the current state: the active
 * recruiters by {@code source} and the active resumes by the reach of their ddds (only home office, one region, several,
 * all of them).
 */
public class CuriositiesRecruiterSourceAndResumeRegionScopeTest {
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
	}

	private final long now = System.currentTimeMillis();

	private final String recruiterEmail = "sourcecounter" + this.now + "@empresa" + this.now + ".com.br";

	private final String resumeEmail = "regionscope" + this.now + "@jobsnow.com";

	private CcpJsonRepresentation recruiterKey() {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, this.recruiterEmail);
	}

	private CcpJsonRepresentation resume(List<Integer> ddds) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.resumeEmail)
				.put(VisEntityResume.Fields.ddd, ddds)
				.put(VisEntityResume.Fields.clt, 5000)
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 10)
				.put(VisEntityResume.Fields.experience, 5)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/regionscope" + this.now)
				.put(VisEntityResume.Fields.resumeType, 2)
				.put(VisEntityResume.Fields.temporallyJobTime, 0);
	}

	private CcpJsonRepresentation resumeKey() {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.resumeEmail);
	}

	@After
	public void tearDown() {
		VisEntityRecruiter.ENTITY.deleteAnyWhere(this.recruiterKey());
		VisEntityResume.ENTITY.deleteAnyWhere(this.resumeKey());
	}

	private static long countOf(CcpEntity parentEntity, CcpEntity childEntity, CcpJsonRepresentation combination, String key) {
		CcpEntityMetaData parentDetails = parentEntity.getEntityMetaData();
		CcpEntityMetaData childDetails = childEntity.getEntityMetaData();
		String parentId = parentEntity.calculateId(combination);
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(parentDetails.entityName, childDetails.entityName, parentId);
		List<List<String>> childrenOfEachGroup = JnGroupers.getChildrenOfEachGroup(Arrays.asList(groupKey));
		List<String> children = childrenOfEachGroup.get(0);
		if (children.isEmpty()) {
			return 0L;
		}
		CcpJsonRepresentation counters = new CcpJsonRepresentation(children.get(0));
		CcpFieldName keyField = new CcpFieldName(key);
		return counters.containsAllFields(keyField) ? counters.getAsLongNumber(keyField) : 0L;
	}

	private static long sources(String source) {
		return countOf(VisEntityVirtualRecruiterSourceCounter.ENTITY, VisEntityRecruiter.ENTITY, VisEntityVirtualRecruiterSourceCounter.getCombination(), source);
	}

	private static long scopes(VisResumeRegionScopeCounterKeys.Scopes scope) {
		return countOf(VisEntityVirtualResumeRegionScopeCounter.ENTITY, VisEntityResume.ENTITY, VisEntityVirtualResumeRegionScopeCounter.getCombination(), scope.name());
	}

	private static List<String> keysOf(List<?> ddds) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.ddd, ddds);
		return VisResumeRegionScopeCounterKeys.INSTANCE.apply(json);
	}

	private static List<Integer> everyRegion() {
		List<Integer> ddds = new ArrayList<>();
		for (VisMetropolitanRegions region : VisMetropolitanRegions.values()) {
			ddds.add(Integer.valueOf(region.name().substring(4)));
		}
		return ddds;
	}

	@Test
	public void eachResumeHasOneReachOfItsDdds() {
		assertEquals(List.of("homeOfficeOnly"), keysOf(List.of(10)));
		assertEquals(List.of("oneRegion"), keysOf(List.of(11)));
		assertEquals("the same ddd twice (11 and 11.0) is one region", List.of("oneRegion"), keysOf(List.of(11, 11.0)));
		assertEquals(List.of("severalRegions"), keysOf(List.of(11, 21, 31)));
		assertEquals(List.of("anyRegion"), keysOf(everyRegion()));
		assertTrue("no ddd, no key", VisResumeRegionScopeCounterKeys.INSTANCE.apply(CcpOtherConstants.EMPTY_JSON).isEmpty());
	}

	@Test
	public void theActiveRecruitersAreCountedBySource() {
		long supportBefore = sources("support");
		long loginBefore = sources("login");
		CcpJsonRepresentation input = this.recruiterKey().put(VisEntityRecruiter.Fields.source, "support");
		VisEntityRecruiter.ENTITY.save(VisBusinessPrepareRecruiter.INSTANCE.execute(input));
		assertEquals("the insertion adds 1 to its source", supportBefore + 1, sources("support"));

		CcpJsonRepresentation changed = this.recruiterKey().put(VisEntityRecruiter.Fields.source, "login");
		VisEntityRecruiter.ENTITY.save(VisBusinessPrepareRecruiter.INSTANCE.execute(changed));
		assertEquals("a save that changes the source moves the count", supportBefore, sources("support"));
		assertEquals(loginBefore + 1, sources("login"));

		VisEntityRecruiter.ENTITY.delete(this.recruiterKey());
		assertEquals("the deactivation subtracts", loginBefore, sources("login"));

		VisEntityRecruiter.ENTITY.getTwinEntity().delete(this.recruiterKey());
		assertEquals("the reactivation adds it back", loginBefore + 1, sources("login"));
	}

	@Test
	public void theFolderLoadCountsItsNewRecruitersAsTelegram() throws Exception {
		long telegramBefore = sources("telegram");
		Path folder = Files.createTempDirectory("recruiterSources");
		String html = "<html><body><p>" + this.recruiterEmail + "</p></body></html>";
		Files.write(folder.resolve("messages.html"), html.getBytes(StandardCharsets.UTF_8));
		CcpJsonRepresentation folderInput = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("folderPath"), folder.toString());

		VisBusinessLoadRecruitersFromHtmlFolder.INSTANCE.execute(folderInput);
		assertEquals("the bulk goes around the decorator: the load counts its new recruiter", telegramBefore + 1, sources("telegram"));

		VisBusinessLoadRecruitersFromHtmlFolder.INSTANCE.execute(folderInput);
		assertEquals("a recruiter already there is not counted again", telegramBefore + 1, sources("telegram"));

		VisEntityRecruiter.ENTITY.delete(this.recruiterKey());
		assertEquals("the deactivation subtracts", telegramBefore, sources("telegram"));
	}

	@Test
	public void theActiveResumesAreCountedByTheReachOfTheirDdds() {
		long oneBefore = scopes(VisResumeRegionScopeCounterKeys.Scopes.oneRegion);
		long severalBefore = scopes(VisResumeRegionScopeCounterKeys.Scopes.severalRegions);
		long homeOfficeBefore = scopes(VisResumeRegionScopeCounterKeys.Scopes.homeOfficeOnly);

		VisEntityResume.ENTITY.save(this.resume(List.of(11)));
		assertEquals(oneBefore + 1, scopes(VisResumeRegionScopeCounterKeys.Scopes.oneRegion));

		VisEntityResume.ENTITY.save(this.resume(List.of(11, 21)));
		assertEquals("a save that changes the ddds moves the count", oneBefore, scopes(VisResumeRegionScopeCounterKeys.Scopes.oneRegion));
		assertEquals(severalBefore + 1, scopes(VisResumeRegionScopeCounterKeys.Scopes.severalRegions));

		VisEntityResume.ENTITY.save(this.resume(List.of(10)));
		assertEquals(severalBefore, scopes(VisResumeRegionScopeCounterKeys.Scopes.severalRegions));
		assertEquals(homeOfficeBefore + 1, scopes(VisResumeRegionScopeCounterKeys.Scopes.homeOfficeOnly));

		VisEntityResume.ENTITY.delete(this.resumeKey());
		assertEquals("the deactivation subtracts", homeOfficeBefore, scopes(VisResumeRegionScopeCounterKeys.Scopes.homeOfficeOnly));
	}

	@Test
	public void theScreenCuriositiesReadsBothCharts() {
		CcpJsonRepresentation input = this.recruiterKey().put(VisEntityRecruiter.Fields.source, "support");
		VisEntityRecruiter.ENTITY.save(VisBusinessPrepareRecruiter.INSTANCE.execute(input));
		VisEntityResume.ENTITY.save(this.resume(everyRegion()));

		CcpJsonRepresentation screen = CcpOtherConstants.EMPTY_JSON.put(JnBusinessGetGrouperCharts.Fields.screen, "curiosities");
		CcpJsonRepresentation charts = JnBusinessGetGrouperCharts.INSTANCE.execute(screen);
		CcpJsonRepresentation recruiterSources = charts.getInnerJson(new CcpFieldName("recruiterSources"));
		CcpJsonRepresentation regionScopes = charts.getInnerJson(new CcpFieldName("resumeRegionScopes"));
		assertTrue(recruiterSources.getAsLongNumber(new CcpFieldName("support")) >= 1);
		assertTrue(regionScopes.getAsLongNumber(new CcpFieldName("anyRegion")) >= 1);
	}
}
