package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.decorators.CcpTimeDecorator;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.ccp.decorators.CcpFieldName;


import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
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
import com.jn.entities.JnEntityGrouper;
import com.jn.entities.JnEntityRecordsCount;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnGroupers;
import com.vis.json.fields.validation.VisJsonCommonsFields;
import com.vis.json.transformers.VisResumeExcludedSkillCounterKeys;
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
		// the e-mail templates of the resume, as CcpCreateEntities seeds them
		JnExecuteBulkOperation.INSTANCE.executeBulk(VisEntityResume.INSTANCE.getFirstRecordsToInsert(), JnDeleteKeysFromCache.INSTANCE);
	}

	private final String email = "lifecycle" + System.currentTimeMillis() + "@jobsnow.com";

	private final String uniqueSkill = "LIFECYCLE" + System.currentTimeMillis();

	private CcpJsonRepresentation resume(int experience) {
		// a skill of this run only: without the ddd in the hash, the groups of common skills are shared with the resumes already
		// in the database
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"" + this.uniqueSkill + "\",\"word\":\"" + this.uniqueSkill + "\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JVM\"]}");
		CcpJsonRepresentation spring = new CcpJsonRepresentation("{\"skill\":\"SPRING\",\"word\":\"SPRING\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"" + this.uniqueSkill + "\"]}");
		CcpJsonRepresentation python = new CcpJsonRepresentation("{\"skill\":\"PYTHON\",\"word\":\"PYTHON\",\"type\":\"excludedSkills\",\"category\":\"excludedSkills\"}");
		CcpJsonRepresentation english = new CcpJsonRepresentation("{\"name\":\"English\",\"level\":2,\"id\":0}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, experience)
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

	/** A hash whose group has the skill of this run: only this resume is in it. */
	private String hashOfTheUniqueSkill(boolean twinSide, List<String> hashes) {
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		for (String hash : hashes) {
			CcpJsonRepresentation group = JnGroupers.getGroup(twinSide, PARENT_NAME, childName, hash);
			CcpJsonRepresentation parentJson = group.getInnerJson(JnEntityGrouper.Fields.parentJson);
			boolean ofTheUniqueSkill = this.uniqueSkill.equals(parentJson.getAsString(VisEntityVirtualHashGrouper.Fields.skillName));
			if (ofTheUniqueSkill) {
				return hash;
			}
		}
		throw new AssertionError("no group of the skill " + this.uniqueSkill);
	}

	private List<String> noticesToTheOwner() {
		return RecordingEmailSender.SENT.stream().filter(sent -> sent.contains(this.email)).collect(Collectors.toList());
	}

	/**
	 * R09-1 (2026-11-10): the resume is written whole ({@code @CcpEntityReplacedOnSave}). Saved again without {@code clt}
	 * and {@code lastJob}, the stored record must lose them; until then the update merged and kept the old values.
	 */
	@Test
	public void savingAgainWithoutAnOptionalFieldRemovesIt() {
		CcpJsonRepresentation withCltAndLastJob = this.resume(4)
				.put(VisEntityResume.Fields.clt, 5_000)
				.put(VisEntityResume.Fields.lastJob, "Analista de sistemas");
		VisEntityResume.ENTITY.save(withCltAndLastJob);
		String resumeEntityName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		// the id comes from the stored record: the e-mail is transformed on the way in
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(this.key());
		String resumeId = VisEntityResume.ENTITY.calculateId(stored);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpJsonRepresentation first = crud.getOneById(resumeEntityName, resumeId);
		assertTrue(first.containsAllFields(VisEntityResume.Fields.clt, VisEntityResume.Fields.lastJob));

		VisEntityResume.ENTITY.save(this.resume(4));
		CcpJsonRepresentation second = crud.getOneById(resumeEntityName, resumeId);
		assertFalse(second.toString(), second.containsField(VisEntityResume.Fields.clt));
		assertFalse(second.toString(), second.containsField(VisEntityResume.Fields.lastJob));
		assertEquals(9_000, second.getAsIntegerNumber(VisEntityResume.Fields.pj).intValue());
		// the fields the decorators write (the hashes of the groups) are still there
		assertFalse(second.getAsStringList(VisEntityResume.Fields.hash).isEmpty());
		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
	}

	@Test
	public void theWholeLifeCycleKeepsTheGroupsAndTellsTheOwner() {
		RecordingEmailSender.SENT.clear();
		RecordingEmailSender.MESSAGES.clear();
		CcpJsonRepresentation resume = this.resume(4);
		// languages: none, English 2, English 1; skills: none, the unique one, JVM, SPRING (PYTHON is excluded); the rest has one value
		assertEquals(12, VisResumeHashCombinations.INSTANCE.countCombinations(resume));

		// save
		VisEntityResume.ENTITY.save(resume);
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(this.key());
		String resumeId = VisEntityResume.ENTITY.calculateId(stored);
		List<String> hashes = stored.getAsStringList(VisEntityResume.Fields.hash);
		assertEquals(12, hashes.size());
		this.assertOnlyIn(MAIN_GROUPER, hashes, resumeId);
		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(this.groupKey(this.hashOfTheUniqueSkill(MAIN_GROUPER, hashes)));
		assertTrue(group.getInnerJson(JnEntityGrouper.Fields.parentJson).containsAllFields(VisEntityVirtualHashGrouper.Fields.skillName));
		assertEquals(Arrays.asList(resumeId), group.getAsStringList(JnEntityGrouper.Fields.child));
		// the child is the id of the record of the resume: reading by it brings the resume back
		String resumeEntityName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation resumeById = CcpDependencyInjection.getDependency(CcpCrud.class).getOneById(resumeEntityName, resumeId);
		assertEquals(stored.getAsString(VisEntityResume.Fields.desiredJob), resumeById.getAsString(VisEntityResume.Fields.desiredJob));
		assertEquals(1, this.noticesToTheOwner().size());

		// saved again with another seniority (the ddd is out of the hash): leaves the old groups, enters the new ones
		VisEntityResume.ENTITY.save(this.resume(7));
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
		// the deactivation e-mail shows the resume as the recruiters saw it, as the e-mails of the save and of the reactivation do
		List<String> messagesAfterDeactivation = RecordingEmailSender.MESSAGES.stream().filter(sent -> sent.contains(this.email)).collect(Collectors.toList());
		String deactivationMessage = messagesAfterDeactivation.get(2);
		assertTrue(deactivationMessage, deactivationMessage.contains("inativou seus dados") || deactivationMessage.contains("deactivated your data"));
		assertTrue(deactivationMessage, deactivationMessage.contains(this.uniqueSkill));
		assertFalse(deactivationMessage, deactivationMessage.contains("{resumesTable}"));
		// the group is only on the twin side: reading it on the main side by getOneById redirects, JnGroupers does not
		String twinOnlyHash = this.hashOfTheUniqueSkill(TWIN_GROUPER, newHashes);
		CcpJsonRepresentation twinOnlyGroupKey = this.groupKey(twinOnlyHash);
		try {
			JnEntityGrouper.ENTITY.getOneById(twinOnlyGroupKey);
			fail("the group exists only on the twin side, the main getOneById should have redirected");
		} catch (CcpErrorFlowDisturb redirect) {
			assertEquals(CcpProcessStatusDefault.REDIRECT, redirect.status);
		}
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		assertTrue(JnGroupers.getGroup(MAIN_GROUPER, PARENT_NAME, childName, twinOnlyHash).isEmpty());
		assertEquals(Arrays.asList(resumeId), JnGroupers.getChildren(TWIN_GROUPER, PARENT_NAME, childName, twinOnlyHash));

		// saved on the twin (still inactive) with another seniority: the twin groups follow the new hashes; no notice
		VisEntityResume.ENTITY.getTwinEntity().save(this.resume(12));
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
		// the reactivation e-mail shows the resume as the recruiter will see it, as the e-mail of the save does
		List<String> messagesToTheOwner = RecordingEmailSender.MESSAGES.stream().filter(sent -> sent.contains(this.email)).collect(Collectors.toList());
		String reactivationMessage = messagesToTheOwner.get(3);
		assertTrue(reactivationMessage, reactivationMessage.contains("reativou seus dados") || reactivationMessage.contains("reactivated your data"));
		assertTrue(reactivationMessage, reactivationMessage.contains(this.uniqueSkill));
		assertFalse(reactivationMessage, reactivationMessage.contains("{resumesTable}"));

		// delete everywhere: leaves the groups of both sides for good
		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
		this.assertInNone(inactiveHashes, resumeId);
		assertEquals(5, this.noticesToTheOwner().size());
	}

	private static final String DDD_PARENT_NAME = VisEntityVirtualDddGrouper.ENTITY.getEntityMetaData().entityName;

	/** The children of this resume in the group of a ddd, on one side: the JSON texts that carry its id. */
	private List<CcpJsonRepresentation> childrenInTheDddGroup(boolean twinSide, int ddd, String resumeId) {
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation combination = CcpOtherConstants.EMPTY_JSON.put(VisEntityVirtualDddGrouper.Fields.ddd, ddd);
		String parentId = VisEntityVirtualDddGrouper.ENTITY.calculateId(combination);
		CcpJsonRepresentation group = JnGroupers.getGroup(twinSide, DDD_PARENT_NAME, childName, parentId);
		if (group.isEmpty()) {
			return Arrays.asList();
		}
		return group.getAsStringList(JnEntityGrouper.Fields.child).stream()
				.map(CcpJsonRepresentation::new)
				.filter(child -> resumeId.equals(child.getAsString(VisJsonCommonsFields.id)))
				.collect(Collectors.toList());
	}

	/** The resume is once in the group of each ddd on one side, in none of them on the other side, and returns that child. */
	private CcpJsonRepresentation assertOnlyInTheDddGroups(boolean twinSide, List<Integer> ddds, String resumeId) {
		CcpJsonRepresentation lastChild = CcpOtherConstants.EMPTY_JSON;
		for (Integer ddd : ddds) {
			List<CcpJsonRepresentation> children = this.childrenInTheDddGroup(twinSide, ddd, resumeId);
			assertEquals("ddd " + ddd, 1, children.size());
			assertTrue("ddd " + ddd, this.childrenInTheDddGroup(false == twinSide, ddd, resumeId).isEmpty());
			lastChild = children.get(0);
		}
		return lastChild;
	}

	private void assertInNoDddGroup(List<Integer> ddds, String resumeId) {
		for (Integer ddd : ddds) {
			assertTrue("ddd " + ddd, this.childrenInTheDddGroup(MAIN_GROUPER, ddd, resumeId).isEmpty());
			assertTrue("ddd " + ddd, this.childrenInTheDddGroup(TWIN_GROUPER, ddd, resumeId).isEmpty());
		}
	}

	/**
	 * 2026-11-12: the resume is a child of the group of each of its ddds, as a JSON text with btc, clt, disponibility,
	 * experience, spokenLanguage, pcd, pj, resumeType, temporallyJobTime, travel and its id; the child follows the save,
	 * the deactivation, the reactivation and the delete everywhere.
	 */
	@Test
	public void theResumeIsAChildOfTheGroupOfEachDdd() {
		VisEntityResume.ENTITY.save(this.resume(4).put(VisEntityResume.Fields.ddd, Arrays.asList(11, 21)).put(VisEntityResume.Fields.clt, 5_000));
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(this.key());
		String resumeId = VisEntityResume.ENTITY.calculateId(stored);
		CcpJsonRepresentation child = this.assertOnlyInTheDddGroups(MAIN_GROUPER, Arrays.asList(11, 21), resumeId);
		List<String> expectedFields = Arrays.asList("btc", "clt", "disponibility", "experience", "spokenLanguage", "pcd", "pj", "resumeType", "temporallyJobTime", "travel", "skill", "id");
		assertTrue(child.toString(), expectedFields.containsAll(child.fieldSet()));
		assertEquals(stored.getAsIntegerNumber(VisEntityResume.Fields.experience), child.getAsIntegerNumber(VisEntityResume.Fields.experience));
		assertEquals(5_000, child.getAsIntegerNumber(VisEntityResume.Fields.clt).intValue());
		assertEquals(9_000, child.getAsIntegerNumber(VisEntityResume.Fields.pj).intValue());
		// spokenLanguage is kept whole, a list with every item as stored (the id the front end sends is dropped on the way in)
		assertEquals(stored.getAsJsonList(VisEntityResume.Fields.spokenLanguage), child.getAsJsonList(VisEntityResume.Fields.spokenLanguage));
		assertEquals("English", child.getAsJsonList(VisEntityResume.Fields.spokenLanguage).get(0).getAsString(new com.ccp.decorators.CcpFieldName("name")));
		// only the skills of the resume itself (type skillsFromResume), by name; the excluded PYTHON is left out
		assertEquals(Arrays.asList(this.uniqueSkill, "SPRING"), child.getAsStringList(VisEntityResume.Fields.skill));
		System.out.println("DDD CHILD: " + child);
		assertFalse(child.toString(), child.containsField(VisEntityResume.Fields.email));
		assertFalse(child.toString(), child.containsField(VisEntityResume.Fields.desiredJob));

		// saved again with other ddds and without clt: leaves 21, enters 31 and the child of 11 is the new version
		VisEntityResume.ENTITY.save(this.resume(4).put(VisEntityResume.Fields.ddd, Arrays.asList(11, 31)));
		this.assertInNoDddGroup(Arrays.asList(21), resumeId);
		CcpJsonRepresentation newChild = this.assertOnlyInTheDddGroups(MAIN_GROUPER, Arrays.asList(11, 31), resumeId);
		assertFalse(newChild.toString(), newChild.containsField(VisEntityResume.Fields.clt));

		// deactivation and reactivation move the child between the sides
		VisEntityResume.ENTITY.delete(this.key());
		this.assertOnlyInTheDddGroups(TWIN_GROUPER, Arrays.asList(11, 31), resumeId);
		VisEntityResume.ENTITY.getTwinEntity().delete(this.key());
		this.assertOnlyInTheDddGroups(MAIN_GROUPER, Arrays.asList(11, 31), resumeId);

		// delete everywhere: out of the groups of both sides
		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
		this.assertInNoDddGroup(Arrays.asList(11, 21, 31), resumeId);
	}

	private static final String EXCLUDED_SKILL_PARENT_NAME = VisEntityVirtualExcludedSkillCounter.ENTITY.getEntityMetaData().entityName;

	/** How many active resumes exclude the skill, as its group of counters tells (0 when the group does not exist). */
	private int resumesExcluding(String skill) {
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation combination = CcpOtherConstants.EMPTY_JSON.put(VisEntityVirtualExcludedSkillCounter.Fields.skill, skill);
		String parentId = VisEntityVirtualExcludedSkillCounter.ENTITY.calculateId(combination);
		CcpJsonRepresentation counters = JnGroupers.getCounters(EXCLUDED_SKILL_PARENT_NAME, childName, parentId);
		boolean absent = false == counters.containsAllFields(VisResumeExcludedSkillCounterKeys.Keys.resumes);
		if (absent) {
			return 0;
		}
		return counters.getAsIntegerNumber(VisResumeExcludedSkillCounterKeys.Keys.resumes);
	}

	private CcpJsonRepresentation resumeExcluding(String... excludedSkills) {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"" + this.uniqueSkill + "\",\"word\":\"" + this.uniqueSkill + "\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		List<CcpJsonRepresentation> skills = new java.util.ArrayList<>(Arrays.asList(java));
		for (int index = 0; index < excludedSkills.length; index++) {
			String excludedSkill = excludedSkills[index];
			// the same skill may come from two words of the text
			skills.add(new CcpJsonRepresentation("{\"skill\":\"" + excludedSkill + "\",\"word\":\"W" + index + excludedSkill + "\",\"type\":\"excludedSkills\",\"category\":\"excludedSkills\"}"));
		}
		return this.resume(4).put(VisEntityResume.Fields.skill, skills);
	}

	/**
	 * 2026-11-12: the group of each skill the owner excluded counts how many active resumes exclude it now; a skill no
	 * longer excluded, the deactivation and the delete everywhere subtract, the reactivation adds back.
	 */
	@Test
	public void theExcludedSkillsAreCountedOnePerGroup() {
		String first = "EXCLA" + System.currentTimeMillis();
		String second = "EXCLB" + System.currentTimeMillis();
		String third = "EXCLC" + System.currentTimeMillis();

		VisEntityResume.ENTITY.save(this.resumeExcluding(first, second, second));
		assertEquals(1, this.resumesExcluding(first));
		assertEquals("the same skill twice counts once", 1, this.resumesExcluding(second));
		assertEquals("a skill found is not counted", 0, this.resumesExcluding(this.uniqueSkill));

		VisEntityResume.ENTITY.save(this.resumeExcluding(second, third));
		assertEquals(0, this.resumesExcluding(first));
		assertEquals(1, this.resumesExcluding(second));
		assertEquals(1, this.resumesExcluding(third));

		VisEntityResume.ENTITY.delete(this.key());
		assertEquals(0, this.resumesExcluding(second));
		assertEquals(0, this.resumesExcluding(third));

		VisEntityResume.ENTITY.getTwinEntity().delete(this.key());
		assertEquals(1, this.resumesExcluding(second));
		assertEquals(1, this.resumesExcluding(third));

		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
		assertEquals(0, this.resumesExcluding(second));
		assertEquals(0, this.resumesExcluding(third));
	}
	private CcpJsonRepresentation storedCount() {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(JnEntityRecordsCount.Fields.entityName, "vis_resume");
		return JnEntityRecordsCount.ENTITY.getOneById(key);
	}

	private void assertCounts(long expectedMain, long expectedTwin) {
		CcpJsonRepresentation count = this.storedCount();
		long realMain = CcpQueryOptions.INSTANCE.matchAll().selectFrom("vis_resume").total();
		long realTwin = CcpQueryOptions.INSTANCE.matchAll().selectFrom("vis_inactive_resume").total();
		assertEquals(expectedMain, realMain);
		assertEquals(expectedTwin, realTwin);
		assertEquals(realMain, count.getAsLongNumber(JnEntityRecordsCount.Fields.mainRecords).longValue());
		assertEquals(realTwin, count.getAsLongNumber(JnEntityRecordsCount.Fields.twinRecords).longValue());
		assertEquals("vis_inactive_resume", count.getAsString(JnEntityRecordsCount.Fields.twinEntityName));
	}

	@Test
	public void theActiveAndInactiveResumesAreCountedOnEveryWrite() {
		// a first save counts what is already in the database, whatever it is
		VisEntityResume.ENTITY.save(this.resume(3));
		long main = CcpQueryOptions.INSTANCE.matchAll().selectFrom("vis_resume").total();
		long twin = CcpQueryOptions.INSTANCE.matchAll().selectFrom("vis_inactive_resume").total();
		this.assertCounts(main, twin);

		VisEntityResume.ENTITY.save(this.resume(4));
		this.assertCounts(main, twin);

		VisEntityResume.ENTITY.delete(this.key());
		this.assertCounts(main - 1, twin + 1);

		VisEntityResume.ENTITY.getTwinEntity().delete(this.key());
		this.assertCounts(main, twin);

		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
		this.assertCounts(main - 1, twin);
	}
	private static int insertedThisMonth(String childName) {
		String parentName = VisEntityVirtualInsertionMonthCounter.ENTITY.getEntityMetaData().entityName;
		String parentId = VisEntityVirtualInsertionMonthCounter.getParentId();
		CcpJsonRepresentation counters = JnGroupers.getCounters(parentName, childName, parentId);
		String yearMonth = new CcpTimeDecorator().getFormattedDateTime("yyyyMM");
		CcpFieldName key = new CcpFieldName(yearMonth);
		return counters.containsAllFields(key) ? counters.getAsIntegerNumber(key) : 0;
	}

	@Test
	public void theResumesAreCountedByTheMonthOfTheirInsertion() {
		String childName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		int before = insertedThisMonth(childName);

		VisEntityResume.ENTITY.save(this.resume(3));
		assertEquals("the insertion adds 1", before + 1, insertedThisMonth(childName));
		String insertionDate = VisEntityResume.ENTITY.getOneById(this.key()).getAsString(JnJsonCommonsFields.insertionDate);
		assertEquals(19, insertionDate.length());

		// the input never sets the moment of the insertion, and an update keeps the stored one
		VisEntityResume.ENTITY.save(this.resume(4).put(JnJsonCommonsFields.insertionDate, "01/01/2016 10:00:00"));
		assertEquals(insertionDate, VisEntityResume.ENTITY.getOneById(this.key()).getAsString(JnJsonCommonsFields.insertionDate));
		assertEquals("an update counts nothing", before + 1, insertedThisMonth(childName));

		VisEntityResume.ENTITY.delete(this.key());
		assertEquals("the deactivation subtracts 1", before, insertedThisMonth(childName));
		assertEquals(insertionDate, VisEntityResume.ENTITY.getTwinEntity().getOneById(this.key()).getAsString(JnJsonCommonsFields.insertionDate));

		VisEntityResume.ENTITY.getTwinEntity().delete(this.key());
		assertEquals("the reactivation adds 1", before + 1, insertedThisMonth(childName));
		assertEquals(insertionDate, VisEntityResume.ENTITY.getOneById(this.key()).getAsString(JnJsonCommonsFields.insertionDate));

		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
		assertEquals("the delete subtracts 1", before, insertedThisMonth(childName));
	}
}
