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
import com.jn.utils.JnGroupers;

/**
 * Proves that resumes changing the same groups at the same time do not overwrite each other: the group is changed by
 * atomic scripts, applied by Elasticsearch one after the other. Eight resumes with the same data (so the same hashes)
 * are saved at once; then four are deleted while the other four are saved again, also at once.
 */
public class ResumeGroupersConcurrencyTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailSender> email = () -> RecordingEmailSender.INSTANCE;

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

	private static final int RESUMES = 8;

	/** The id of the record of the resume, as the groups keep it: calculated from the e-mail as it is stored. */
	private static String resumeId(String email) {
		String storedEmail = new CcpStringDecorator(email).email().hash().asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation storedKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, storedEmail);
		return VisEntityResume.ENTITY.calculateId(storedKey);
	}

	private static List<String> emails(String prefix) {
		List<String> emails = new ArrayList<>();
		for (int k = 0; k < RESUMES; k++) {
			emails.add(prefix + k + "@jobsnow.com");
		}
		return emails;
	}

	/**
	 * Without the ddd in the hash, the groups of common values are shared with the resumes already stored; a skill of
	 * this execution alone gives the test groups no other resume reaches.
	 */
	private static final String UNIQUE_SKILL = "CONCURRENCY" + System.currentTimeMillis();

	private static CcpJsonRepresentation resume(String email) {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"" + UNIQUE_SKILL + "\",\"word\":\"" + UNIQUE_SKILL + "\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JVM\"]}");
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

	private static CcpJsonRepresentation groupKey(String hash) {
		String parentName = VisEntityVirtualHashGrouper.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		return JnGroupers.getGroupKey(parentName, childName, hash);
	}

	private static Set<String> childrenOfTheGroup(String hash) {
		CcpJsonRepresentation groupKey = groupKey(hash);
		boolean exists = JnEntityGrouper.ENTITY.exists(groupKey);
		if (false == exists) {
			return new HashSet<>();
		}
		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(groupKey);
		List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
		assertEquals(children.size(), group.getAsIntegerNumber(JnEntityGrouper.Fields.childCount).intValue());
		return new HashSet<>(children);
	}

	/** The resumes of this test inside the group: the groups of common values also hold the resumes already stored. */
	private static Set<String> resumesOfTheTestInTheGroup(String hash, Set<String> resumesOfTheTest) {
		Set<String> children = childrenOfTheGroup(hash);
		children.retainAll(resumesOfTheTest);
		return children;
	}

	@Test
	public void concurrentWritersOfTheSameGroupLoseNothing() throws Exception {
		List<String> emails = emails("concurrency" + System.currentTimeMillis() + "_");
		Set<String> allResumeIds = emails.stream().map(email -> resumeId(email)).collect(Collectors.toSet());

		// eight resumes saved at once, all in the same groups
		List<Runnable> saves = emails.stream().map(email -> (Runnable) () -> VisEntityResume.ENTITY.save(resume(email))).collect(Collectors.toList());
		runAtOnce(saves);

		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, emails.get(0));
		List<String> hashes = VisEntityResume.ENTITY.getOneById(key).getAsStringList(VisEntityResume.Fields.hash);
		// languages: none; skills: none, the unique one, JVM; the rest has one value
		assertEquals(3, hashes.size());
		List<String> exclusiveHashes = new ArrayList<>();
		for (String hash : hashes) {
			assertEquals(hash, allResumeIds, resumesOfTheTestInTheGroup(hash, allResumeIds));
			boolean exclusive = allResumeIds.equals(childrenOfTheGroup(hash));
			if (exclusive) {
				exclusiveHashes.add(hash);
			}
		}
		// at least the group of the unique skill holds the resumes of this test alone
		assertTrue(false == exclusiveHashes.isEmpty());

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

		Set<String> keptResumeIds = kept.stream().map(email -> resumeId(email)).collect(Collectors.toSet());
		for (String hash : hashes) {
			assertEquals(hash, keptResumeIds, resumesOfTheTestInTheGroup(hash, allResumeIds));
		}

		// cleanup: the last four leave, and the groups that only they held are deleted
		List<Runnable> cleanup = kept.stream().map(email -> (Runnable) () -> VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email))).collect(Collectors.toList());
		runAtOnce(cleanup);
		for (String hash : hashes) {
			assertTrue(hash, resumesOfTheTestInTheGroup(hash, allResumeIds).isEmpty());
		}
		for (String hash : exclusiveHashes) {
			assertTrue(hash, false == JnEntityGrouper.ENTITY.exists(groupKey(hash)));
		}
	}
}
