package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityGrouper;
import com.vis.business.resume.VisResumeGroupers;

/**
 * Proves that resumes changing the same page of the same groups at the same time do not overwrite each other: the page
 * is changed by atomic scripts, applied by Elasticsearch one after the other. Eight resumes with the same data (so the
 * same hashes) and e-mails chosen to fall in the same page are saved at once; then four are deleted while the other four
 * are saved again, also at once.
 */
public class ResumeGroupersConcurrencyTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> new SilentInstantMessenger();
		CcpInstanceProvider<CcpEmailSender> email = () -> new RecordingEmailSender();

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	private static final int RESUMES = 8;

	/** The e-mail as it is stored: the same SHA-1 of the field transformer of the e-mail. */
	private static String storedEmail(String email) {
		return new CcpStringDecorator(email).email().hash().asString(CcpHashAlgorithm.SHA1);
	}

	/** Picks e-mails whose stored form falls in the same page of the groups. */
	private static List<String> emailsInTheSamePage(String prefix) {
		List<String> emails = new ArrayList<>();
		int targetPage = -1;
		for (int k = 0; emails.size() < RESUMES; k++) {
			String email = prefix + k + "@jobsnow.com";
			int page = VisResumeGroupers.getChildPage(storedEmail(email));
			if (targetPage < 0) {
				targetPage = page;
			}
			if (page == targetPage) {
				emails.add(email);
			}
		}
		return emails;
	}

	private static CcpJsonRepresentation resume(String email) {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JVM\"]}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/onias")
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(java));
	}

	private static void runAtOnce(List<Runnable> tasks) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
		List<Future<?>> futures = new ArrayList<>();
		for (Runnable task : tasks) {
			futures.add(pool.submit(task));
		}
		for (Future<?> future : futures) {
			future.get();
		}
		pool.shutdown();
	}

	private static Set<String> childrenOfThePage(String hash, int page) {
		CcpJsonRepresentation groupKey = VisResumeGroupers.getGroupKey(hash, page);
		boolean exists = JnEntityGrouper.ENTITY.exists(groupKey);
		if (false == exists) {
			return new HashSet<>();
		}
		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(groupKey);
		List<CcpJsonRepresentation> children = group.getAsJsonList(JnEntityGrouper.Fields.childJson);
		assertEquals(children.size(), group.getAsIntegerNumber(JnEntityGrouper.Fields.childCount).intValue());
		return children.stream().map(child -> child.getAsString(VisEntityResume.Fields.email)).collect(Collectors.toSet());
	}

	@Test
	public void concurrentWritersOfTheSamePageLoseNothing() throws Exception {
		List<String> emails = emailsInTheSamePage("concurrency" + System.currentTimeMillis() + "_");
		int page = VisResumeGroupers.getChildPage(storedEmail(emails.get(0)));
		Set<String> allStoredEmails = emails.stream().map(email -> storedEmail(email)).collect(Collectors.toSet());

		// eight resumes saved at once, all in the same page of the same groups
		List<Runnable> saves = emails.stream().map(email -> (Runnable) () -> VisEntityResume.ENTITY.save(resume(email))).collect(Collectors.toList());
		runAtOnce(saves);

		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, emails.get(0));
		List<String> hashes = VisEntityResume.ENTITY.getOneById(key).getAsStringList(VisEntityResume.Fields.hash);
		// languages: none; skills: none, JAVA, JVM; the rest has one value
		assertEquals(3, hashes.size());
		for (String hash : hashes) {
			assertEquals(hash, allStoredEmails, childrenOfThePage(hash, page));
		}

		// four deleted while the other four are saved again, at once
		List<String> deleted = emails.subList(0, RESUMES / 2);
		List<String> kept = emails.subList(RESUMES / 2, RESUMES);
		List<Runnable> mixed = new ArrayList<>();
		for (String email : deleted) {
			mixed.add(() -> VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email)));
		}
		for (String email : kept) {
			mixed.add(() -> VisEntityResume.ENTITY.save(resume(email)));
		}
		runAtOnce(mixed);

		Set<String> keptStoredEmails = kept.stream().map(email -> storedEmail(email)).collect(Collectors.toSet());
		for (String hash : hashes) {
			assertEquals(hash, keptStoredEmails, childrenOfThePage(hash, page));
		}

		// cleanup: the last four leave, and the pages are deleted
		List<Runnable> cleanup = kept.stream().map(email -> (Runnable) () -> VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email))).collect(Collectors.toList());
		runAtOnce(cleanup);
		for (String hash : hashes) {
			assertTrue(hash, childrenOfThePage(hash, page).isEmpty());
			assertTrue(hash, false == JnEntityGrouper.ENTITY.exists(VisResumeGroupers.getGroupKey(hash, page)));
		}
	}
}
