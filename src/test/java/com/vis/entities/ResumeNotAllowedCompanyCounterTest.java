package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
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
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityGrouper;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnGroupers;
import com.vis.json.transformers.VisResumeNotAllowedCompanyCounterKeys;

/**
 * The single group of {@code JnEntityGrouper} that counts, for each domain of {@code notAllowedCompany}, how many active
 * resumes forbid it now. The scenario of the request (2026-11-11): candidate 1 forbids 1.com, 2.com and 3.com,
 * candidate 2 forbids 2.com and 4.com, candidate 3 forbids 5.com, so the group has {1.com: 1, 2.com: 2, 3.com: 1,
 * 4.com: 1, 5.com: 1}. The domains carry the run in their names, because the group is shared by every resume of the
 * database.
 */
public class ResumeNotAllowedCompanyCounterTest {

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

	private static final String PARENT_NAME = VisEntityVirtualNotAllowedCompanyCounter.ENTITY.getEntityMetaData().entityName;

	private static final String CHILD_NAME = VisEntityResume.ENTITY.getEntityMetaData().entityName;

	private final long run = System.currentTimeMillis();

	private final String consultancy1 = "1-" + this.run + ".com";

	private final String consultancy2 = "2-" + this.run + ".com";

	private final String consultancy3 = "3-" + this.run + ".com";

	private final String consultancy4 = "4-" + this.run + ".com";

	private final String consultancy5 = "5-" + this.run + ".com.br";

	private String email(int candidate) {
		return "counter" + candidate + "-" + this.run + "@jobsnow.com";
	}

	private CcpJsonRepresentation resume(int candidate, String... notAllowedCompanies) {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.email(candidate))
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/counter" + candidate + "-" + this.run)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(java))
				.put(VisEntityResume.Fields.notAllowedCompany, Arrays.asList(notAllowedCompanies));
	}

	private CcpJsonRepresentation key(int candidate) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.email(candidate));
	}

	/** The counter of a domain, 0 when it is not in the group. */
	private int count(String domain) {
		String parentId = VisEntityVirtualNotAllowedCompanyCounter.getParentId();
		CcpJsonRepresentation counters = JnGroupers.getCounters(PARENT_NAME, CHILD_NAME, parentId);
		CcpFieldName domainFieldName = new CcpFieldName(domain);
		boolean absent = false == counters.containsAllFields(domainFieldName);
		if (absent) {
			return 0;
		}
		return counters.getAsIntegerNumber(domainFieldName);
	}

	private void assertCounts(int count1, int count2, int count3, int count4, int count5) {
		assertEquals(this.consultancy1, count1, this.count(this.consultancy1));
		assertEquals(this.consultancy2, count2, this.count(this.consultancy2));
		assertEquals(this.consultancy3, count3, this.count(this.consultancy3));
		assertEquals(this.consultancy4, count4, this.count(this.consultancy4));
		assertEquals(this.consultancy5, count5, this.count(this.consultancy5));
	}

	@Test
	public void theKeysAreTheDomainsAsTypedEachOnceWithoutQuotes() {
		CcpJsonRepresentation resume = this.resume(1, "a.com", "a.com", "b\"c.com", "\"", "Itaú");
		List<String> keys = VisResumeNotAllowedCompanyCounterKeys.INSTANCE.apply(resume);
		assertEquals(Arrays.asList("a.com", "bc.com", "Itaú"), keys);
		CcpJsonRepresentation withoutDomains = resume.removeFields(VisEntityResume.Fields.notAllowedCompany);
		assertTrue(VisResumeNotAllowedCompanyCounterKeys.INSTANCE.apply(withoutDomains).isEmpty());
	}

	/** Since 2026-11-11 each item must be a valid domain: anything else refuses the whole resume, nothing is counted. */
	@Test
	public void anItemThatIsNotADomainRefusesTheResume() {
		List<String> invalidItems = Arrays.asList("Actdigital", "plan a", "nenhuma", "someone@gmail.com", "https://www.linkedin.com/in/x",
				"-consultoria.com", "consultoria-.com", "consultoria.c0m", "consultoria..com", ".com", "consultoria.com.");
		for (String invalidItem : invalidItems) {
			CcpJsonRepresentation resume = this.resume(1, this.consultancy1, invalidItem);
			try {
				VisEntityResume.ENTITY.save(resume);
				fail("accepted " + invalidItem);
			} catch (CcpJsonValidationError expected) {
				assertTrue(expected.getMessage(), expected.getMessage().contains(VisEntityResume.Fields.notAllowedCompany.name()));
			}
		}
		assertFalse(VisEntityResume.ENTITY.exists(this.key(1)));
		assertEquals(0, this.count(this.consultancy1));
	}

	@Test
	public void validDomainsAreAccepted() {
		VisEntityResume.ENTITY.save(this.resume(1, "actdigital.com", "ntconsult.com.br", "aml-group.co.uk", "G4F.COM.BR", "a1.io"));
		List<String> stored = VisEntityResume.ENTITY.getOneById(this.key(1)).getAsStringList(VisEntityResume.Fields.notAllowedCompany);
		assertEquals(5, stored.size());
		VisEntityResume.ENTITY.deleteAnyWhere(this.key(1));
	}

	@Test
	public void theCountersFollowTheActiveResumes() {
		// the scenario of the request
		VisEntityResume.ENTITY.save(this.resume(1, this.consultancy1, this.consultancy2, this.consultancy3));
		VisEntityResume.ENTITY.save(this.resume(2, this.consultancy2, this.consultancy4));
		VisEntityResume.ENTITY.save(this.resume(3, this.consultancy5));
		this.assertCounts(1, 2, 1, 1, 1);
		String parentId = VisEntityVirtualNotAllowedCompanyCounter.getParentId();
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, parentId);
		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(groupKey);
		assertEquals(1, group.getAsStringList(JnEntityGrouper.Fields.child).size());
		CcpJsonRepresentation parentJson = group.getInnerJson(JnEntityGrouper.Fields.parentJson);
		assertEquals(VisEntityResume.Fields.notAllowedCompany.getValue(), parentJson.getAsString(VisEntityVirtualNotAllowedCompanyCounter.Fields.counter));
		// the resume keeps the domains as typed
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(this.key(3));
		assertEquals(Arrays.asList(this.consultancy5), stored.getAsStringList(VisEntityResume.Fields.notAllowedCompany));

		// saving again with the same domains changes nothing
		VisEntityResume.ENTITY.save(this.resume(2, this.consultancy4, this.consultancy2));
		this.assertCounts(1, 2, 1, 1, 1);

		// candidate 2 drops 2.com: subtracts 1
		VisEntityResume.ENTITY.save(this.resume(2, this.consultancy4));
		this.assertCounts(1, 1, 1, 1, 1);

		// candidate 1 deactivated: its domains leave (2.com reaches 0 and leaves the group)
		VisEntityResume.ENTITY.delete(this.key(1));
		this.assertCounts(0, 0, 0, 1, 1);
		// saved while inactive: still not counted
		VisEntityResume.ENTITY.getTwinEntity().save(this.resume(1, this.consultancy1, this.consultancy2));
		this.assertCounts(0, 0, 0, 1, 1);

		// reactivated: counted again, with the domains of the inactive version
		VisEntityResume.ENTITY.getTwinEntity().delete(this.key(1));
		this.assertCounts(1, 1, 0, 1, 1);

		// delete everywhere of every candidate: no domain of this run is left
		VisEntityResume.ENTITY.deleteAnyWhere(this.key(1));
		VisEntityResume.ENTITY.deleteAnyWhere(this.key(2));
		VisEntityResume.ENTITY.deleteAnyWhere(this.key(3));
		this.assertCounts(0, 0, 0, 0, 0);
		assertFalse(VisEntityResume.ENTITY.exists(this.key(3)));
	}
}
