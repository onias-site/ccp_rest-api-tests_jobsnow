package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
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
import com.ccp.especifications.db.utils.entity.decorators.CountingInstantMessenger;

/**
 * The resume that last saved each LinkedIn address ({@link VisEntityResumeLinkedin}): a resume saving the address of
 * another e-mail is saved anyway and reaches the support operator; a new address or the delete everywhere releases the
 * old address only while it still belongs to the e-mail. Each test uses e-mails and addresses of its own run.
 */
public class ResumeLinkedinOwnerTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> CountingInstantMessenger.INSTANCE;
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

	private final long run = System.currentTimeMillis();

	private String email(String who) {
		return "linkedin" + who + this.run + "@jobsnow.com";
	}

	private String address(String profile) {
		return "https://www.linkedin.com/in/" + profile + this.run;
	}

	private static CcpJsonRepresentation resume(String email, String linkedinAddress) {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\",\"parent\":[\"JVM\"]}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, linkedinAddress)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(java));
	}

	private static CcpJsonRepresentation key(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email);
	}

	private static CcpJsonRepresentation addressKey(String linkedinAddress) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResumeLinkedin.Fields.linkedinAddress, linkedinAddress);
	}

	private static boolean isOwned(String linkedinAddress) {
		CcpJsonRepresentation addressKey = addressKey(linkedinAddress);
		boolean owned = VisEntityResumeLinkedin.ENTITY.exists(addressKey);
		return owned;
	}

	/** Whether the e-mail is among the e-mails of the address, compared by the hash the entity keeps. */
	private static boolean isOwnedBy(String linkedinAddress, String email) {
		CcpJsonRepresentation addressKey = addressKey(linkedinAddress);
		CcpEntityMetaData metaData = VisEntityResumeLinkedin.ENTITY.getEntityMetaData();
		CcpJsonRepresentation addressRecord = metaData.getOneByIdOrHandleItIfThisIdWasNotFound(addressKey, notFound -> CcpOtherConstants.EMPTY_JSON);
		List<String> emails = addressRecord.getAsStringList(VisEntityResumeLinkedin.Fields.emails);
		String hashedEmail = new CcpStringDecorator(email).email().hash().asString(CcpHashAlgorithm.SHA1);
		return emails.contains(hashedEmail);
	}

	private static void deleteEverywhere(String... emails) {
		for (String email : emails) {
			CcpJsonRepresentation key = key(email);
			VisEntityResume.ENTITY.deleteAnyWhere(key);
		}
	}

	@Test
	public void aNewAddressBelongsToTheResumeThatSavedItWithoutReachingTheOperator() {
		String owner = this.email("owner");
		String address = this.address("newaddress");

		VisEntityResume.ENTITY.save(resume(owner, address));
		VisEntityResume.ENTITY.save(resume(owner, address));

		assertTrue(isOwnedBy(address, owner));
		assertEquals(0, CountingInstantMessenger.howManyMention(address));
		deleteEverywhere(owner);
	}

	@Test
	public void anAddressOfAnotherEmailIsSavedAnywayAndReachesTheOperator() {
		String owner = this.email("first");
		String another = this.email("second");
		String address = this.address("sharedaddress");

		VisEntityResume.ENTITY.save(resume(owner, address));
		VisEntityResume.ENTITY.save(resume(another, address));

		assertTrue(VisEntityResume.ENTITY.exists(key(another)));
		assertTrue(isOwnedBy(address, another));
		assertEquals(1, CountingInstantMessenger.howManyMention(address));
		assertEquals(1, CountingInstantMessenger.howManyMention(another));
		deleteEverywhere(owner, another);
	}

	@Test
	public void changingTheAddressReleasesTheOldOne() {
		String owner = this.email("changer");
		String another = this.email("follower");
		String oldAddress = this.address("oldaddress");
		String newAddress = this.address("newerprofile");

		VisEntityResume.ENTITY.save(resume(owner, oldAddress));
		VisEntityResume.ENTITY.save(resume(owner, newAddress));

		assertTrue(false == isOwned(oldAddress));
		assertTrue(isOwnedBy(newAddress, owner));

		// the old address is free: whoever uses it now does not reach the operator
		VisEntityResume.ENTITY.save(resume(another, oldAddress));
		assertEquals(0, CountingInstantMessenger.howManyMention(oldAddress));
		deleteEverywhere(owner, another);
	}

	@Test
	public void deactivatingKeepsTheAddressAndDeletingEverywhereReleasesIt() {
		String owner = this.email("leaver");
		String address = this.address("leavingaddress");

		VisEntityResume.ENTITY.save(resume(owner, address));
		VisEntityResume.ENTITY.delete(key(owner));
		assertTrue(isOwnedBy(address, owner));

		deleteEverywhere(owner);
		assertTrue(false == isOwned(address));
	}

	@Test
	public void aResumeLeavingDoesNotTakeTheAddressOfTheResumeThatSavedItLast() {
		String owner = this.email("former");
		String another = this.email("latter");
		String address = this.address("disputedaddress");

		VisEntityResume.ENTITY.save(resume(owner, address));
		VisEntityResume.ENTITY.save(resume(another, address));
		deleteEverywhere(owner);

		assertTrue(isOwnedBy(address, another));
		deleteEverywhere(another);
		assertTrue(false == isOwned(address));
	}

	/**
	 * Finding 24: until 2026-10-15 the address kept a single e-mail, so a resume that borrowed it and gave it back
	 * deleted the record, and the original owner, still using the address, was left without it (a third e-mail using
	 * it no longer reached the operator).
	 */
	@Test
	public void givingBackABorrowedAddressKeepsItWithTheOriginalOwner() {
		String owner = this.email("original");
		String borrower = this.email("borrower");
		String third = this.email("third");
		String address = this.address("borrowedaddress");
		String borrowerOwnAddress = this.address("borrowerown");

		VisEntityResume.ENTITY.save(resume(owner, address));
		VisEntityResume.ENTITY.save(resume(borrower, address));
		assertTrue(isOwnedBy(address, owner));
		assertTrue(isOwnedBy(address, borrower));
		assertEquals(1, CountingInstantMessenger.howManyMention(borrower));

		VisEntityResume.ENTITY.save(resume(borrower, borrowerOwnAddress));
		assertTrue(isOwnedBy(address, owner));
		assertTrue(false == isOwnedBy(address, borrower));

		VisEntityResume.ENTITY.save(resume(third, address));
		assertEquals(1, CountingInstantMessenger.howManyMention(third));
		deleteEverywhere(owner, borrower, third);
		assertTrue(false == isOwned(address));
		assertTrue(false == isOwned(borrowerOwnAddress));
	}
}
