package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailSender;
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
import com.jn.entities.JnEntityGrouper;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnGroupers;
import com.vis.json.transformers.VisResumeHashCombinations;

/**
 * Proves the life cycle of the resume end to end: the save fills the {@code hash} list and puts the resume in the group
 * of {@code JnEntityGrouper} of each hash; saving again moves it to the groups of the new hashes; the deactivation moves
 * it to the twin groups, a save on the twin keeps it there with its new hashes, and the reactivation moves it back; the
 * delete everywhere takes it out of both sides. Save, deactivation, reactivation and delete everywhere e-mail the owner
 * (the save on the twin does not).
 */
public class ResumeLifecycleTest {

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
		// the e-mail templates of the resume, as CcpCreateEntities seeds them
		JnExecuteBulkOperation.INSTANCE.executeBulk(new VisEntityResume().getFirstRecordsToInsert(), JnDeleteKeysFromCache.INSTANCE);
	}

	private final String email = "lifecycle" + System.currentTimeMillis() + "@jobsnow.com";

	private CcpJsonRepresentation resume(int ddd) {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JVM\"]}");
		CcpJsonRepresentation spring = new CcpJsonRepresentation("{\"skill\":\"SPRING\",\"word\":\"SPRING\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JAVA\"]}");
		CcpJsonRepresentation python = new CcpJsonRepresentation("{\"skill\":\"PYTHON\",\"word\":\"PYTHON\",\"type\":\"excludedSkills\",\"category\":\"excludedSkills\"}");
		CcpJsonRepresentation english = new CcpJsonRepresentation("{\"name\":\"English\",\"level\":2,\"id\":0}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(ddd))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/onias")
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(java, spring, python))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(english));
	}

	private CcpJsonRepresentation key() {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.email);
	}

	private static final boolean MAIN_GROUPER = false;

	private static final boolean TWIN_GROUPER = true;

	private static final String PARENT_NAME = VisEntityVirtualHashGrouper.ENTITY.getEntityMetaData().entityName;

	private CcpJsonRepresentation groupKey(String hash) {
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		return JnGroupers.getGroupKey(PARENT_NAME, childName, hash);
	}

	/** Read only through JnGroupers.getGroup: a group that is only on the other side comes empty, it does not redirect. */
	private List<String> childrenOfTheGroup(boolean twinSide, String hash) {
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation group = JnGroupers.getGroup(twinSide, PARENT_NAME, childName, hash);
		if (group.isEmpty()) {
			return Arrays.asList();
		}
		List<String> children = JnGroupers.getChildren(twinSide, PARENT_NAME, childName, hash);
		assertEquals(children.size(), group.getAsIntegerNumber(JnEntityGrouper.Fields.childCount).intValue());
		return children;
	}

	private boolean isInTheGroup(boolean twinSide, String hash, String resumeId) {
		return this.childrenOfTheGroup(twinSide, hash).contains(resumeId);
	}

	/** The resume is in the groups of the hashes on one side of the grouper and in none of them on the other side. */
	private void assertOnlyIn(boolean twinSide, List<String> hashes, String resumeId) {
		for (String hash : hashes) {
			assertTrue(hash, this.isInTheGroup(twinSide, hash, resumeId));
			assertFalse(hash, this.isInTheGroup(false == twinSide, hash, resumeId));
		}
	}

	private void assertInNone(List<String> hashes, String resumeId) {
		for (String hash : hashes) {
			assertFalse(hash, this.isInTheGroup(MAIN_GROUPER, hash, resumeId));
			assertFalse(hash, this.isInTheGroup(TWIN_GROUPER, hash, resumeId));
		}
	}

	private List<String> noticesToTheOwner() {
		return RecordingEmailSender.SENT.stream().filter(sent -> sent.contains(this.email)).collect(Collectors.toList());
	}

	@Test
	public void theWholeLifeCycleKeepsTheGroupsAndTellsTheOwner() {
		RecordingEmailSender.SENT.clear();
		CcpJsonRepresentation resume = this.resume(11);
		// languages: none, English 2, English 1; skills: none, JAVA, JVM, SPRING (PYTHON is excluded); the rest has one value
		assertEquals(12, new VisResumeHashCombinations().countCombinations(resume));

		// save
		VisEntityResume.ENTITY.save(resume);
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(this.key());
		String resumeId = VisEntityResume.ENTITY.calculateId(stored);
		List<String> hashes = stored.getAsStringList(VisEntityResume.Fields.hash);
		assertEquals(12, hashes.size());
		this.assertOnlyIn(MAIN_GROUPER, hashes, resumeId);
		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(this.groupKey(hashes.get(0)));
		assertTrue(group.getInnerJson(JnEntityGrouper.Fields.parentJson).containsAllFields(VisEntityVirtualHashGrouper.Fields.skillName));
		assertEquals(Arrays.asList(resumeId), group.getAsStringList(JnEntityGrouper.Fields.child));
		// the child is the id of the record of the resume: reading by it brings the resume back
		String resumeEntityName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation resumeById = CcpDependencyInjection.getDependency(CcpCrud.class).getOneById(resumeEntityName, resumeId);
		assertEquals(stored.getAsString(VisEntityResume.Fields.desiredJob), resumeById.getAsString(VisEntityResume.Fields.desiredJob));
		assertEquals(1, this.noticesToTheOwner().size());

		// saved again with another ddd: leaves the old groups, enters the new ones
		VisEntityResume.ENTITY.save(this.resume(21));
		List<String> newHashes = VisEntityResume.ENTITY.getOneById(this.key()).getAsStringList(VisEntityResume.Fields.hash);
		this.assertInNone(hashes, resumeId);
		this.assertOnlyIn(MAIN_GROUPER, newHashes, resumeId);
		assertEquals(2, this.noticesToTheOwner().size());

		// deactivation: moves to the twin, and from the main groups to the twin groups
		VisEntityResume.ENTITY.delete(this.key());
		assertFalse(VisEntityResume.ENTITY.exists(this.key()));
		assertTrue(VisEntityResume.ENTITY.getTwinEntity().exists(this.key()));
		this.assertOnlyIn(TWIN_GROUPER, newHashes, resumeId);
		assertEquals(3, this.noticesToTheOwner().size());
		// the group is only on the twin side: reading it on the main side by getOneById redirects, JnGroupers does not
		CcpJsonRepresentation twinOnlyGroupKey = this.groupKey(newHashes.get(0));
		try {
			JnEntityGrouper.ENTITY.getOneById(twinOnlyGroupKey);
			fail("the group exists only on the twin side, the main getOneById should have redirected");
		} catch (CcpErrorFlowDisturb redirect) {
			assertEquals(CcpProcessStatusDefault.REDIRECT, redirect.status);
		}
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		assertTrue(JnGroupers.getGroup(MAIN_GROUPER, PARENT_NAME, childName, newHashes.get(0)).isEmpty());
		assertEquals(Arrays.asList(resumeId), JnGroupers.getChildren(TWIN_GROUPER, PARENT_NAME, childName, newHashes.get(0)));

		// saved on the twin (still inactive) with another ddd: the twin groups follow the new hashes; no notice
		VisEntityResume.ENTITY.getTwinEntity().save(this.resume(31));
		assertFalse(VisEntityResume.ENTITY.exists(this.key()));
		List<String> inactiveHashes = VisEntityResume.ENTITY.getTwinEntity().getOneById(this.key()).getAsStringList(VisEntityResume.Fields.hash);
		this.assertInNone(newHashes, resumeId);
		this.assertOnlyIn(TWIN_GROUPER, inactiveHashes, resumeId);
		assertEquals(3, this.noticesToTheOwner().size());

		// reactivation: back from the twin, and from the twin groups to the main groups
		VisEntityResume.ENTITY.getTwinEntity().delete(this.key());
		assertTrue(VisEntityResume.ENTITY.exists(this.key()));
		this.assertOnlyIn(MAIN_GROUPER, inactiveHashes, resumeId);
		assertEquals(4, this.noticesToTheOwner().size());

		// delete everywhere: leaves the groups of both sides for good
		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
		this.assertInNone(inactiveHashes, resumeId);
		assertEquals(5, this.noticesToTheOwner().size());
	}
}
