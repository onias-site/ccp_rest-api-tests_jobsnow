package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
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
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.business.resume.VisResumeGroupers;
import com.vis.json.transformers.VisResumeHashCombinations;

/**
 * Proves the life cycle of the resume end to end: the save fills the {@code hash} list and puts the resume in the group
 * of {@code JnEntityGrouper} of each hash; saving again moves it to the groups of the new hashes; the deactivation takes
 * it out of every group and the reactivation puts it back; the delete everywhere takes it out for good. Each of the
 * four steps (save, deactivation, reactivation, delete everywhere) e-mails the owner.
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

	private String storedEmail;

	private CcpJsonRepresentation groupKey(String hash) {
		int childPage = VisResumeGroupers.getChildPage(this.storedEmail);
		return VisResumeGroupers.getGroupKey(hash, childPage);
	}

	private List<CcpJsonRepresentation> childrenOfTheGroup(String hash) {
		CcpJsonRepresentation groupKey = this.groupKey(hash);
		boolean groupExists = JnEntityGrouper.ENTITY.exists(groupKey);
		if (false == groupExists) {
			return Arrays.asList();
		}
		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(groupKey);
		List<CcpJsonRepresentation> children = group.getAsJsonList(JnEntityGrouper.Fields.childJson);
		assertEquals(children.size(), group.getAsIntegerNumber(JnEntityGrouper.Fields.childCount).intValue());
		return children;
	}

	private boolean isInTheGroup(String hash, String storedEmail) {
		List<String> emails = this.childrenOfTheGroup(hash).stream()
				.map(child -> child.getAsString(VisEntityResume.Fields.email))
				.collect(Collectors.toList());
		return emails.contains(storedEmail);
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
		String storedEmail = stored.getAsString(VisEntityResume.Fields.email);
		this.storedEmail = storedEmail;
		CcpJsonRepresentation firstGroupKey = this.groupKey(stored.getAsStringList(VisEntityResume.Fields.hash).get(0));
		assertEquals(VisResumeGroupers.getChildPage(storedEmail), firstGroupKey.getAsIntegerNumber(JnEntityGrouper.Fields.childPage).intValue());
		List<String> hashes = stored.getAsStringList(VisEntityResume.Fields.hash);
		assertEquals(12, hashes.size());
		for (String hash : hashes) {
			assertTrue(hash, this.isInTheGroup(hash, storedEmail));
		}
		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(this.groupKey(hashes.get(0)));
		assertTrue(group.getInnerJson(JnEntityGrouper.Fields.parentJson).containsAllFields(VisEntityVirtualHashGrouper.Fields.skillName));
		CcpJsonRepresentation child = group.getAsJsonList(JnEntityGrouper.Fields.childJson).get(0);
		assertFalse("the child keeps no hash list", child.containsAllFields(VisEntityResume.Fields.hash));
		assertTrue(child.containsAllFields(VisEntityResume.Fields.desiredJob));
		assertEquals(1, this.noticesToTheOwner().size());

		// saved again with another ddd: leaves the old groups, enters the new ones
		VisEntityResume.ENTITY.save(this.resume(21));
		List<String> newHashes = VisEntityResume.ENTITY.getOneById(this.key()).getAsStringList(VisEntityResume.Fields.hash);
		for (String hash : hashes) {
			assertFalse(hash, this.isInTheGroup(hash, storedEmail));
		}
		for (String hash : newHashes) {
			assertTrue(hash, this.isInTheGroup(hash, storedEmail));
		}
		assertEquals(2, this.noticesToTheOwner().size());

		// deactivation: moves to the twin and leaves every group
		VisEntityResume.ENTITY.delete(this.key());
		assertFalse(VisEntityResume.ENTITY.exists(this.key()));
		assertTrue(VisEntityResume.ENTITY.getTwinEntity().exists(this.key()));
		for (String hash : newHashes) {
			assertFalse(hash, this.isInTheGroup(hash, storedEmail));
		}
		assertEquals(3, this.noticesToTheOwner().size());

		// reactivation: back from the twin, enters the groups again
		VisEntityResume.ENTITY.getTwinEntity().delete(this.key());
		assertTrue(VisEntityResume.ENTITY.exists(this.key()));
		for (String hash : newHashes) {
			assertTrue(hash, this.isInTheGroup(hash, storedEmail));
		}
		assertEquals(4, this.noticesToTheOwner().size());

		// delete everywhere: leaves every group for good
		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
		for (String hash : newHashes) {
			assertFalse(hash, this.isInTheGroup(hash, storedEmail));
		}
		assertEquals(5, this.noticesToTheOwner().size());
	}
}
